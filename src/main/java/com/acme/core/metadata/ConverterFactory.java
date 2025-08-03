package com.acme.core.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 转换器工厂 - 简化版
 * 基于类型进行1对1映射，不支持自动匹配
 */
public class ConverterFactory {
    
    private static final Logger log = LoggerFactory.getLogger(ConverterFactory.class);
    
    private final Map<Class<? extends DataConverter>, DataConverter> converterInstances = new ConcurrentHashMap<>();
    
    /**
     * 注册转换器
     * 
     * @param converter 转换器实例
     */
    public void registerConverter(DataConverter converter) {
        if (converter == null) {
            throw new IllegalArgumentException("Converter cannot be null");
        }
        
        Class<? extends DataConverter> converterClass = converter.getClass();
        
        // 检查是否已经注册过相同类型
        if (converterInstances.containsKey(converterClass)) {
            throw new IllegalArgumentException(
                "Converter type already registered: " + converterClass.getSimpleName() + 
                ". Each converter type can only be registered once.");
        }
        
        converterInstances.put(converterClass, converter);
        log.info("Registered converter: {} -> {}", converterClass.getSimpleName(), converter.getDescription());
    }
    
    /**
     * 根据转换器类型获取转换器实例
     * 
     * @param converterClass 转换器类型
     * @return 转换器实例，如果没有注册返回null
     */
    public DataConverter getConverter(Class<? extends DataConverter> converterClass) {
        if (converterClass == null) {
            log.debug("Converter class is null");
            return null;
        }
        
        DataConverter converter = converterInstances.get(converterClass);
        if (converter != null) {
            log.debug("Found registered converter: {} -> {}", converterClass.getSimpleName(), converter.getDescription());
        } else {
            log.warn("Converter not found for type: {}. Available types: {}", 
                    converterClass.getSimpleName(), converterInstances.keySet());
        }
        
        return converter;
    }
    
    /**
     * 获取已注册的转换器数量
     */
    public int getConverterCount() {
        return converterInstances.size();
    }
    
    /**
     * 检查转换器类型是否已注册
     */
    public boolean isConverterRegistered(Class<? extends DataConverter> converterClass) {
        return converterInstances.containsKey(converterClass);
    }
    
    /**
     * 移除转换器
     */
    public boolean removeConverter(Class<? extends DataConverter> converterClass) {
        DataConverter removed = converterInstances.remove(converterClass);
        if (removed != null) {
            log.info("Removed converter: {} -> {}", converterClass.getSimpleName(), removed.getDescription());
            return true;
        }
        return false;
    }
    
    /**
     * 获取所有已注册的转换器类型
     */
    public java.util.Set<Class<? extends DataConverter>> getRegisteredConverterTypes() {
        return converterInstances.keySet();
    }
    
    /**
     * 清空所有转换器
     */
    public void clear() {
        int count = converterInstances.size();
        converterInstances.clear();
        log.info("Cleared {} converters", count);
    }
}