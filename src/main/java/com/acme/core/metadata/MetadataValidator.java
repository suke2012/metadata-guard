package com.acme.core.metadata;

import java.util.List;

/**
 * 元数据验证器 - 简化统一入口
 * 支持监控模式配置和同类型对象的批量验证
 */
public interface MetadataValidator {
    
    /**
     * 验证入口（使用默认告警模式）
     * 
     * @param dtoList 同一种结构体的列表（可以是单个元素的列表）
     * @param converterClass 转换器类型（必须显式指定）
     * @throws MetaViolationException 验证失败时抛出（仅拦截模式）
     */
    void validate(List<Object> dtoList, Class<? extends DataConverter> converterClass) throws MetaViolationException;
    
    /**
     * 验证入口（指定监控模式）
     * 
     * @param dtoList 同一种结构体的列表（可以是单个元素的列表）
     * @param converterClass 转换器类型（必须显式指定）
     * @param mode 监控模式：MONITOR（告警模式）或 INTERCEPT（拦截模式）
     * @throws MetaViolationException 验证失败时抛出（仅拦截模式）
     */
    void validate(List<Object> dtoList, Class<? extends DataConverter> converterClass, MetadataGuard.Mode mode) throws MetaViolationException;
    
    /**
     * 注册数据转换器
     */
    void registerConverter(DataConverter converter);
    
    /**
     * 注册单元处理器
     */  
    void registerUnitProcessor(UnitProcessor processor);
    
    /**
     * 异步验证入口（显式异步调用）
     * 
     * @param dtoList 同一种结构体的列表
     * @param converterClass 转换器类型
     * @param callback 异步回调（可选）
     */
    void validateAsync(List<Object> dtoList, Class<? extends DataConverter> converterClass, AsyncValidationCallback callback);
}