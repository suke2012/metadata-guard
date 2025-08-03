package com.acme.core.metadata.converter;

import java.util.Map;

/**
 * 数据转换器接口
 * 业务方实现此接口来定义特殊数据类型的转换逻辑
 */
public interface DataConverter {
    
    /**
     * 将业务数据转换为标准的kv格式
     * @param data 原始业务数据
     * @return 转换后的kv数据，key为元数据字段名，value为对应值
     */
    Map<String, Object> convert(Object data);
    
    /**
     * 检查是否支持转换指定类型的数据
     * @param data 待检查的数据
     * @return true表示支持转换，false表示不支持
     */
    boolean supports(Object data);
    
    /**
     * 获取转换器的优先级，数值越小优先级越高
     * 当多个转换器都支持同一类型数据时，优先级高的会被选择
     * @return 优先级数值
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}