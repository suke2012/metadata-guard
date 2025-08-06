package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;
import com.acme.core.metadata.converter.DataConverter;
import com.acme.core.metadata.rule.ValidationContext;

/**
 * 统一的元数据验证入口接口
 * 支持多种数据结构类型的验证，业务方可自定义转换逻辑
 */
public interface UnifiedMetadataValidator {
    
    /**
     * 验证任意数据结构（自动推断类型）
     * @param unit 待验证的监控单元
     * @throws MetaViolationException 验证失败时抛出
     */
    void validate(MetadataCollectionUnit unit) throws MetaViolationException;
    
    /**
     * 核心验证方法 - 验证键值对数据
     * @param unit 包含所有验证数据和上下文的监控单元
     * @throws MetaViolationException 业务规则验证失败时抛出
     */
    void validateKeyValues(MetadataCollectionUnit unit) throws MetaViolationException;

}