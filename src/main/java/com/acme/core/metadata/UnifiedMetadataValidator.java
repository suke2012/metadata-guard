package com.acme.core.metadata;

import com.acme.core.metadata.converter.DataConverter;
import com.acme.core.metadata.rule.ValidationContext;

/**
 * 统一的元数据验证入口接口
 * 支持多种数据结构类型的验证，业务方可自定义转换逻辑
 */
public interface UnifiedMetadataValidator {
    
    /**
     * 验证任意数据结构（自动推断类型）
     * @param data 待验证的数据，可以是Object、Map、String等任意类型
     * @param context 验证上下文（包含接口级监控模式等）
     * @throws MetaViolationException 验证失败时抛出
     */
    void validate(Object data, ValidationContext context) throws MetaViolationException;
    
    /**
     * 验证指定类型的数据结构
     * @param data 待验证的数据
     * @param dataType 数据类型标识，用于匹配对应的转换器
     * @param context 验证上下文
     * @throws MetaViolationException 验证失败时抛出
     */
    void validate(Object data, String dataType, ValidationContext context) throws MetaViolationException;
    
    /**
     * 注册数据转换器（由业务方提供具体转换逻辑）
     * @param dataType 数据类型标识
     * @param converter 对应的转换器，将业务数据转换为标准kv格式
     */
    void registerConverter(String dataType, DataConverter converter);
    
    /**
     * 移除数据转换器
     * @param dataType 数据类型标识
     */
    void removeConverter(String dataType);
    
    /**
     * 获取已注册的转换器
     * @param dataType 数据类型标识
     * @return 对应的转换器，如果不存在返回null
     */
    DataConverter getConverter(String dataType);
}