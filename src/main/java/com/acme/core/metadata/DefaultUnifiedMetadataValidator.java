package com.acme.core.metadata;

import com.acme.core.metadata.converter.DataConverter;
import com.acme.core.metadata.model.MetaDefinition;
import com.acme.core.metadata.registry.MetadataRegistryService;
import com.acme.core.metadata.rule.ValidationContext;
import com.acme.core.metadata.rule.ValidationPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一元数据验证器的默认实现
 */
public class DefaultUnifiedMetadataValidator implements UnifiedMetadataValidator {
    
    private static final Logger log = LoggerFactory.getLogger("MetaViolation");
    
    private final MetadataRegistryService registry;
    private final Map<String, DataConverter> converters = new ConcurrentHashMap<>();
    
    public DefaultUnifiedMetadataValidator(MetadataRegistryService registry) {
        this.registry = registry;
    }
    
    @Override
    public void validate(Object data, ValidationContext context) throws MetaViolationException {
        if (data == null) return;
        
        // 自动推断数据类型并选择合适的转换器
        DataConverter converter = findBestConverter(data);
        if (converter != null) {
            validate(data, converter, context);
        } else {
            // 没有找到专用转换器，使用默认处理逻辑
            validateWithDefaultLogic(data, context);
        }
    }
    
    @Override
    public void validate(Object data, String dataType, ValidationContext context) throws MetaViolationException {
        if (data == null) return;
        
        DataConverter converter = converters.get(dataType);
        if (converter != null) {
            validate(data, converter, context);
        } else {
            log.warn("No converter found for dataType: {}, falling back to default logic", dataType);
            validateWithDefaultLogic(data, context);
        }
    }
    
    /**
     * 使用指定转换器进行验证
     */
    private void validate(Object data, DataConverter converter, ValidationContext context) throws MetaViolationException {
        try {
            Map<String, Object> kvs = converter.convert(data);
            if (kvs != null && !kvs.isEmpty()) {
                validateKeyValues(kvs, context);
            }
        } catch (Exception e) {
            if (context.mode() == MetadataGuard.Mode.MONITOR) {
                log.warn("Converter failed in MONITOR mode: {}", e.getMessage(), e);
            } else {
                throw new MetaViolationException("Data conversion failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * 默认验证逻辑（兼容原有逻辑）
     */
    private void validateWithDefaultLogic(Object data, ValidationContext context) throws MetaViolationException {
        if (context.mode() == MetadataGuard.Mode.MONITOR) {
            try {
                doDefaultValidate(data, context);
            } catch (Throwable t) {
                log.warn("MetaMonitorFailed {}", t.getMessage(), t);
            }
        } else {
            doDefaultValidate(data, context);
        }
    }
    
    /**
     * 执行默认验证（保持与原DefaultMetadataGuard兼容）
     */
    private void doDefaultValidate(Object data, ValidationContext context) throws MetaViolationException {
        Map<String, Object> kvs = new HashMap<>();
        
        if (data instanceof Map) {
            // 直接处理Map类型数据
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            kvs.putAll(map);
        } else {
            // 使用反射收集对象字段（保持原有逻辑）
            // 这里可以复用原来DefaultMetadataGuard的collect方法逻辑
            collectFromObject(data, kvs, context);
        }
        
        validateKeyValues(kvs, context);
    }
    
    /**
     * 验证kv数据
     */
    private void validateKeyValues(Map<String, Object> kvs, ValidationContext context) throws MetaViolationException {
        Map<String, MetaDefinition> defs = registry.getAll();
        ValidationPipeline pipe = ValidationPipeline.instance();
        
        for (Map.Entry<String, Object> entry : kvs.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            MetaDefinition def = defs.get(key);
            
            // 检查元数据中心的监控模式配置，优先级高于接口级配置
            ValidationContext actualContext = resolveValidationMode(context, def);
            
            pipe.validate(key, value, def, actualContext);
        }
    }
    
    /**
     * 解析实际的验证模式
     * 优先级：元数据中心配置 > 接口级配置
     */
    private ValidationContext resolveValidationMode(ValidationContext interfaceContext, MetaDefinition def) {
        if (def != null && def.getValidationMode() != null) {
            // 元数据中心有配置，使用元数据中心的配置
            ValidationContext metaCenterContext = new ValidationContext(def.getValidationMode());
            // 保留接口级的其他环境变量
            metaCenterContext.copyEnvFrom(interfaceContext);
            return metaCenterContext;
        } else {
            // 元数据中心没有配置，使用接口级配置
            return interfaceContext;
        }
    }
    
    /**
     * 从对象中收集字段数据（简化版本，后续可以完善）
     */
    private void collectFromObject(Object obj, Map<String, Object> kvs, ValidationContext context) {
        // 暂时简化实现，后续可以复用原有的反射逻辑
        // 这里可以根据需要实现对象字段的收集
    }
    
    /**
     * 寻找最佳的转换器
     */
    private DataConverter findBestConverter(Object data) {
        return converters.values().stream()
                .filter(converter -> converter.supports(data))
                .min(Comparator.comparingInt(DataConverter::getOrder))
                .orElse(null);
    }
    
    @Override
    public void registerConverter(String dataType, DataConverter converter) {
        converters.put(dataType, converter);
        log.info("Registered data converter for type: {}", dataType);
    }
    
    @Override
    public void removeConverter(String dataType) {
        DataConverter removed = converters.remove(dataType);
        if (removed != null) {
            log.info("Removed data converter for type: {}", dataType);
        }
    }
    
    @Override
    public DataConverter getConverter(String dataType) {
        return converters.get(dataType);
    }
}