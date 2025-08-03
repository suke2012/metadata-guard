package com.acme.core.metadata.extension;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 硬编码数据处理器
 * 管理和协调多个硬编码数据提取器
 * 为后续扩展硬编码数据监控提供统一接口
 */
public class HardcodedDataProcessor {
    
    private final Map<String, HardcodedDataExtractor> extractors = new ConcurrentHashMap<>();
    private final Map<String, HardcodedMappingConfig> mappingConfigs = new ConcurrentHashMap<>();
    
    /**
     * 注册硬编码数据提取器
     * @param dataType 数据类型标识
     * @param extractor 提取器实现
     */
    public void registerExtractor(String dataType, HardcodedDataExtractor extractor) {
        extractors.put(dataType, extractor);
    }
    
    /**
     * 注册硬编码映射配置
     * @param dataType 数据类型标识
     * @param config 映射配置
     */
    public void registerMappingConfig(String dataType, HardcodedMappingConfig config) {
        mappingConfigs.put(dataType, config);
    }
    
    /**
     * 处理硬编码数据并提取监控字段
     * @param data 原始数据
     * @param dataType 数据类型标识
     * @return 提取的监控字段kv数据
     */
    public Map<String, Object> processHardcodedData(Object data, String dataType) {
        HardcodedDataExtractor extractor = extractors.get(dataType);
        HardcodedMappingConfig config = mappingConfigs.get(dataType);
        
        if (extractor == null || config == null) {
            return new HashMap<>();
        }
        
        if (extractor.supports(data, config)) {
            return extractor.extract(data, config);
        }
        
        return new HashMap<>();
    }
    
    /**
     * 检查是否支持处理指定类型的硬编码数据
     * @param dataType 数据类型标识
     * @return true表示支持，false表示不支持
     */
    public boolean supportsHardcodedData(String dataType) {
        return extractors.containsKey(dataType) && mappingConfigs.containsKey(dataType);
    }
    
    /**
     * 获取所有支持的硬编码数据类型
     * @return 支持的数据类型集合
     */
    public Set<String> getSupportedDataTypes() {
        Set<String> supported = new HashSet<>(extractors.keySet());
        supported.retainAll(mappingConfigs.keySet());
        return supported;
    }
    
    /**
     * 移除硬编码数据处理器
     * @param dataType 数据类型标识
     */
    public void removeHardcodedDataSupport(String dataType) {
        extractors.remove(dataType);
        mappingConfigs.remove(dataType);
    }
}