package com.acme.core.metadata.extension;

import java.util.Map;

/**
 * 硬编码数据提取器接口
 * 用于处理历史代码中硬编码的数据类型（如dtoA.A=Str[0]这种形式）
 * 为后续监控值范围等需求预留扩展点
 */
public interface HardcodedDataExtractor {
    
    /**
     * 从硬编码数据中提取监控字段
     * @param data 原始数据（可能是数组、字符串等）
     * @param mappingConfig 硬编码映射配置（如A=Str[0], B=Str[1]等）
     * @return 提取的kv数据，key为字段名（如dtoA.A），value为对应值
     */
    Map<String, Object> extract(Object data, HardcodedMappingConfig mappingConfig);
    
    /**
     * 检查是否支持处理指定的数据类型
     * @param data 待检查的数据
     * @param mappingConfig 映射配置
     * @return true表示支持，false表示不支持
     */
    boolean supports(Object data, HardcodedMappingConfig mappingConfig);
    
    /**
     * 获取提取器的优先级
     * @return 优先级数值，越小优先级越高
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}