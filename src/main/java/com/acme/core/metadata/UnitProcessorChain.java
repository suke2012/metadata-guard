package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 监控单元处理器链
 * 按优先级顺序执行多个处理器
 * Spring管理的单例Bean
 */
@Component
public class UnitProcessorChain {
    
    private static final Logger log = LoggerFactory.getLogger(UnitProcessorChain.class);
    
    private final List<UnitProcessor> processors = new CopyOnWriteArrayList<>();
    private volatile boolean sorted = true;
    
    /**
     * 注册处理器
     * 
     * @param processor 处理器实例
     */
    public void registerProcessor(UnitProcessor processor) {
        if (processor == null) {
            throw new IllegalArgumentException("Processor cannot be null");
        }
        
        processors.add(processor);
        sorted = false; // 标记需要重新排序
        log.info("Registered unit processor: {} with order {}", 
                processor.getDescription(), processor.getOrder());
    }
    
    /**
     * 处理监控单元
     * 按处理器优先级顺序依次执行，每个processor负责统一的KV合并
     * 
     * @param unit 原始监控单元
     * @return 处理后的监控单元
     */
    public MetadataCollectionUnit process(MetadataCollectionUnit unit) {
        if (unit == null) {
            return null;
        }
        
        ensureSorted();
        
        MetadataCollectionUnit currentUnit = unit;
        
        for (UnitProcessor processor : processors) {
            try {
                if (processor.supports(currentUnit)) {
                    log.debug("Processing unit with processor: {}", processor.getDescription());
                    MetadataCollectionUnit processedUnit = processor.process(currentUnit);
                    
                    if (processedUnit != null) {
                        currentUnit = processedUnit;
                        log.debug("Unit processed successfully by: {}", processor.getDescription());
                    } else {
                        log.warn("Processor {} returned null, skipping", processor.getDescription());
                    }
                } else {
                    log.debug("Processor {} does not support this unit, skipping", processor.getDescription());
                }
            } catch (Exception e) {
                log.error("Error processing unit with processor {}: {}", 
                         processor.getDescription(), e.getMessage(), e);
                // 继续执行其他处理器，不中断整个链
            }
        }
        
        return currentUnit;
    }
    
    /**
     * 批量处理监控单元
     * 
     * @param units 原始监控单元列表
     * @return 处理后的监控单元列表
     */
    public List<MetadataCollectionUnit> processAll(List<MetadataCollectionUnit> units) {
        if (units == null || units.isEmpty()) {
            return units;
        }
        
        List<MetadataCollectionUnit> processedUnits = new ArrayList<>();
        
        for (MetadataCollectionUnit unit : units) {
            MetadataCollectionUnit processedUnit = process(unit);
            if (processedUnit != null) {
                processedUnits.add(processedUnit);
            }
        }
        
        return processedUnits;
    }
    
    /**
     * 移除处理器
     */
    public boolean removeProcessor(UnitProcessor processor) {
        boolean removed = processors.remove(processor);
        if (removed) {
            log.info("Removed unit processor: {}", processor.getDescription());
        }
        return removed;
    }
    
    /**
     * 获取已注册的处理器数量
     */
    public int getProcessorCount() {
        return processors.size();
    }
    
    /**
     * 获取所有已注册的处理器
     */
    public List<UnitProcessor> getAllProcessors() {
        ensureSorted();
        return new ArrayList<>(processors);
    }
    
    /**
     * 清空所有处理器
     */
    public void clear() {
        int count = processors.size();
        processors.clear();
        sorted = true;
        log.info("Cleared {} unit processors", count);
    }
    
    /**
     * 确保处理器按优先级排序
     */
    private void ensureSorted() {
        if (!sorted) {
            synchronized (this) {
                if (!sorted) {
                    processors.sort(Comparator.comparingInt(UnitProcessor::getOrder));
                    sorted = true;
                    log.debug("Sorted {} processors by order", processors.size());
                }
            }
        }
    }
    
}