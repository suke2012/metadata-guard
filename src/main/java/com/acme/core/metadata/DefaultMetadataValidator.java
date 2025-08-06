package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;

/**
 * 默认元数据验证器实现（简化版本）
 * <p>
 * 流程：DTO列表 -> Converter -> CollectionUnit -> Processor -> 直接验证
 * <p>
 * 核心改进：
 * - 支持监控模式从上游传递（MONITOR告警模式/INTERCEPT拦截模式）
 * - 简化验证逻辑，直接使用处理后的数据进行验证
 * - 减少中间层复杂度，提升性能和可维护性
 */
public class DefaultMetadataValidator implements MetadataValidator {

    private static final Logger log = LoggerFactory.getLogger(DefaultMetadataValidator.class);

    private final ConverterFactory converterFactory;
    private final UnitProcessorChain processorChain;
    private final UnifiedMetadataValidationFacade validationFacade;

    @Autowired
    private UnifiedMetadataValidator validator;
    
    // 异步配置
    @Value("${meta.guard.async.enabled:false}")
    private boolean asyncEnabled;
    
    @Value("${meta.guard.async.core-pool-size:2}")
    private int corePoolSize;
    
    @Value("${meta.guard.async.max-pool-size:8}")
    private int maxPoolSize;
    
    @Value("${meta.guard.async.queue-capacity:1000}")
    private int queueCapacity;
    
    @Value("${meta.guard.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    // 线程池
    private ThreadPoolExecutor asyncExecutor;

    public DefaultMetadataValidator(UnifiedMetadataValidationFacade validationFacade) {
        this.validationFacade = validationFacade;
        this.converterFactory = new ConverterFactory();
        this.processorChain = new UnitProcessorChain();
    }
    
    /**
     * 初始化异步线程池
     */
    @PostConstruct
    public void initAsyncExecutor() {
        if (asyncEnabled) {
            ThreadFactory threadFactory = new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "meta-guard-async-" + (++counter));
                    thread.setDaemon(true);  // 设置为守护线程
                    return thread;
                }
            };
            
            this.asyncExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()  // 队列满时调用者线程执行
            );
            
            log.info("Initialized async thread pool: core={}, max={}, queue={}, keepAlive={}s", 
                    corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds);
        } else {
            log.info("Async validation is disabled");
        }
    }
    
    /**
     * 销毁异步线程池
     */
    @PreDestroy
    public void destroyAsyncExecutor() {
        if (asyncExecutor != null) {
            log.info("Shutting down async thread pool...");
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("Async thread pool did not terminate gracefully, forcing shutdown");
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for async thread pool termination", e);
                asyncExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void validate(List<Object> dtoList, Class<? extends DataConverter> converterClass) throws MetaViolationException {
        // 使用默认告警模式，默认同步
        validate(dtoList, converterClass, MetadataGuard.Mode.MONITOR, false);
    }

    @Override
    public void validate(List<Object> dtoList, Class<? extends DataConverter> converterClass, MetadataGuard.Mode mode) throws MetaViolationException {
        // 默认同步执行
        validate(dtoList, converterClass, mode, false);
    }

    @Override
    public void validate(List<Object> dtoList, Class<? extends DataConverter> converterClass,
                         MetadataGuard.Mode mode, boolean async) throws MetaViolationException {
        if (dtoList == null || dtoList.isEmpty()) {
            log.debug("No DTOs provided for validation");
            return;
        }

        // 根据异步参数和配置选择执行方式
        if (async) {
            validateAsync(dtoList, converterClass, mode);
        } else if (asyncEnabled && mode == MetadataGuard.Mode.MONITOR) {
            validateAsync(dtoList, converterClass, mode);
        } else {
            validateSync(dtoList, converterClass, mode);
        }
    }
    
    /**
     * 同步验证
     */
    private void validateSync(List<Object> dtoList, Class<? extends DataConverter> converterClass, MetadataGuard.Mode mode) throws MetaViolationException {
        try {
            validateSameType(dtoList);
            doValidate(dtoList, converterClass, mode);
        } catch (Exception e) {
            handleException(e);
        }
    }
    
    /**
     * 异步验证（模式无关）
     */
    private void validateAsync(List<Object> dtoList, Class<? extends DataConverter> converterClass, MetadataGuard.Mode mode) {
        if (asyncExecutor == null) {
            log.warn("Async executor not initialized, falling back to sync validation");
            try {
                validateSync(dtoList, converterClass, mode);
            } catch (MetaViolationException e) {
                // 异步模式下不抛出异常，只记录日志
                log.warn("Async validation failed: {}", e.getMessage());
            }
            return;
        }

        // 提交异步任务
        asyncExecutor.submit(() -> {
            try {
                validateSameType(dtoList);
                log.debug("Executing async validation for {} DTOs", dtoList.size());
                doValidate(dtoList, converterClass, mode);
                log.debug("Async validation completed successfully for {} DTOs", dtoList.size());
            } catch (Exception e) {
                // 异步模式下所有异常都记录日志，不抛出
                if (e instanceof MetaViolationException) {
                    log.warn("Async validation rule violation: {}", e.getMessage());
                } else {
                    log.error("Async validation technical error: {}", e.getMessage(), e);
                }
            }
        });
        
        log.debug("Submitted async validation task for {} DTOs", dtoList.size());
    }

    /**
     * 执行完整的验证流程（内部方法，不处理异常）
     */
    private void doValidate(List<Object> dtoList, Class<? extends DataConverter> converterClass, MetadataGuard.Mode mode) throws Exception {

        // 步骤1: 获取转换器并转换数据
        DataConverter converter = converterFactory.getConverter(converterClass);
        if (converter == null) {
            log.warn("No converter found for class: {}", converterClass.getSimpleName());
            return;
        }

        log.debug("Using converter: {} for {} DTOs in {} mode",
                converter.getDescription(), dtoList.size(), mode);

        // 步骤2: 转换为监控单元
        List<MetadataCollectionUnit> units = converter.convert(dtoList.toArray());
        if (units == null || units.isEmpty()) {
            log.debug("No collection units generated by converter: {}", converter.getDescription());
            return;
        }

        // 步骤3: 处理器链处理（解析特殊字段）
        // 这里保留try-catch是合理的，因为处理器失败不应该中断主流程
        List<MetadataCollectionUnit> processedUnits = safeProcessUnits(units);

        // 步骤4: 核心验证逻辑
        performCoreValidation(processedUnits, mode);
    }

    /**
     * 执行核心验证逻辑（简化版，移除内部异常处理）
     */
    private void performCoreValidation(List<MetadataCollectionUnit> processedUnits, MetadataGuard.Mode mode) throws Exception {
        for (MetadataCollectionUnit unit : processedUnits) {
            unit.setMode(mode);
            validator.validateKeyValues(unit);
        }
    }

    /**
     * 安全地处理监控单元（处理器失败不影响主流程）
     */
    private List<MetadataCollectionUnit> safeProcessUnits(List<MetadataCollectionUnit> units) {
        try {
            return processorChain.processAll(units);
        } catch (Exception e) {
            log.error("Unit processor chain failed, using original units: {}", e.getMessage());
            return units; // 处理失败，使用原始units继续验证
        }
    }

    /**
     * 统一的异常处理收口 - 极简版本
     * 只有业务规则异常才抛出，其他异常都吃掉
     */
    private void handleException(Exception e) throws MetaViolationException {
        if (e instanceof MetaViolationException) {
            // 业务规则异常，直接抛出（监控模式的判断在规则层处理）
            throw (MetaViolationException) e;
        } else {
            // 所有其他技术异常都记录日志并吃掉，确保链路健壮性
            log.error("Validation process encountered technical error: {}", e.getMessage(), e);
        }
    }

    /**
     * 验证列表中的对象是否为同一类型
     */
    private void validateSameType(List<Object> dtoList) {
        if (dtoList.size() <= 1) {
            return; // 单个或空列表无需检查
        }

        Class<?> firstType = null;
        for (int i = 0; i < dtoList.size(); i++) {
            Object dto = dtoList.get(i);
            if (dto == null) {
                throw new IllegalArgumentException("DTO list contains null element at index " + i);
            }

            if (firstType == null) {
                firstType = dto.getClass();
            } else if (!firstType.equals(dto.getClass())) {
                throw new IllegalArgumentException(
                        "All DTOs must be of the same type. Expected: " + firstType.getSimpleName() +
                                ", but found: " + dto.getClass().getSimpleName() + " at index " + i);
            }
        }

        log.debug("Validated {} DTOs of type: {}", dtoList.size(), firstType.getSimpleName());
    }


    @Override
    public void registerConverter(DataConverter converter) {
        if (converter != null) {
            converterFactory.registerConverter(converter);
            log.info("Registered converter: {}", converter.getDescription());
        }
    }

    @Override
    public void registerUnitProcessor(UnitProcessor processor) {
        if (processor != null) {
            processorChain.registerProcessor(processor);
            log.info("Registered unit processor: {}", processor.getDescription());
        }
    }
    
    @Override
    public void validateAsync(List<Object> dtoList, Class<? extends DataConverter> converterClass, AsyncValidationCallback callback) {
        if (callback == null) {
            callback = AsyncValidationCallback.EMPTY;
        }
        
        if (dtoList == null || dtoList.isEmpty()) {
            log.debug("No DTOs provided for async validation");
            callback.onSuccess(0);
            return;
        }
        
        // 检查线程池是否可用
        if (asyncExecutor == null) {
            log.warn("Async executor not initialized for explicit async validation");
            callback.onFailure(new IllegalStateException("Async validation not available"), 0);
            return;
        }
        
        final AsyncValidationCallback finalCallback = callback;
        final int dtoCount = dtoList.size();
        
        // 提交异步任务
        asyncExecutor.submit(() -> {
            try {
                // 验证列表中的对象是否为同一类型
                validateSameType(dtoList);
                
                // 执行完整的验证流程（使用MONITOR模式，因为异步验证不抛出异常）
                doValidate(dtoList, converterClass, MetadataGuard.Mode.MONITOR);
                
                log.debug("Explicit async validation completed successfully for {} DTOs", dtoCount);
                finalCallback.onSuccess(dtoCount);
                
            } catch (Exception e) {
                log.error("Explicit async validation failed for {} DTOs: {}", dtoCount, e.getMessage(), e);
                finalCallback.onFailure(e, dtoCount);
            }
        });
        
        log.debug("Submitted explicit async validation task for {} DTOs", dtoCount);
    }

}