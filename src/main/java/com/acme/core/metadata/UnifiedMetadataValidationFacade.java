package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;
import com.acme.core.metadata.converter.DataConverter;
import com.acme.core.metadata.extension.HardcodedDataProcessor;
import com.acme.core.metadata.rule.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * 统一元数据验证门面类
 * 提供统一入口，同时为后续注解模式回归预留兼容性
 */
public class UnifiedMetadataValidationFacade {

    private static final Logger log = LoggerFactory.getLogger(UnifiedMetadataValidationFacade.class);

    @Autowired
    private  UnifiedMetadataValidator unifiedValidator;

    @Autowired
    private  MetadataGuard legacyValidator; // 保留原有验证器，为注解模式回归做准备


    private  HardcodedDataProcessor hardcodedProcessor;

    private boolean enableLegacyMode = false; // 是否启用legacy模式（注解模式）

    public UnifiedMetadataValidationFacade(UnifiedMetadataValidator unifiedValidator,
                                           MetadataGuard legacyValidator,
                                           HardcodedDataProcessor hardcodedProcessor) {
        this.unifiedValidator = unifiedValidator;
        this.legacyValidator = legacyValidator;
        this.hardcodedProcessor = hardcodedProcessor;
    }

    /**
     * 统一验证入口
     * 根据配置选择使用新的统一模式或legacy注解模式
     */
    public void validate(MetadataCollectionUnit unit) throws MetaViolationException {
        // 使用新的统一模式
        unifiedValidator.validate(unit);
    }


}