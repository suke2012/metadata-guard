package com.acme.core.metadata;

import com.acme.core.metadata.converter.DataConverter;
import com.acme.core.metadata.extension.HardcodedDataProcessor;
import com.acme.core.metadata.rule.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 统一元数据验证门面类
 * 提供统一入口，同时为后续注解模式回归预留兼容性
 */
public class UnifiedMetadataValidationFacade {

    private static final Logger log = LoggerFactory.getLogger(UnifiedMetadataValidationFacade.class);

    private final UnifiedMetadataValidator unifiedValidator;
    private final MetadataGuard legacyValidator; // 保留原有验证器，为注解模式回归做准备
    private final HardcodedDataProcessor hardcodedProcessor;

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
    public void validate(Object data, ValidationContext context) throws MetaViolationException {
        // 使用新的统一模式
        log.debug("Using unified validation mode");
        unifiedValidator.validate(data, context);
    }

    /**
     * 带数据类型的验证入口
     */
    public void validate(Object data, String dataType, ValidationContext context) throws MetaViolationException {
        if (enableLegacyMode && legacyValidator != null) {
            // Legacy模式下忽略dataType参数
            legacyValidator.validate(data, context);
        } else {
            unifiedValidator.validate(data, dataType, context);
        }
    }

    /**
     * 处理硬编码数据的验证（预留扩展点）
     */
    public void validateHardcodedData(Object data, String dataType, ValidationContext context) throws MetaViolationException {
        if (hardcodedProcessor.supportsHardcodedData(dataType)) {
            Map<String, Object> extractedKvs = hardcodedProcessor.processHardcodedData(data, dataType);
            if (!extractedKvs.isEmpty()) {
                // 将提取的硬编码数据作为Map进行验证
                validate(extractedKvs, context);
            }
        } else {
            log.warn("Hardcoded data type not supported: {}", dataType);
        }
    }

    /**
     * 注册数据转换器
     */
    public void registerConverter(String dataType, DataConverter converter) {
        unifiedValidator.registerConverter(dataType, converter);
    }

    /**
     * 切换到legacy模式（注解模式）
     * 为后续回归注解模式提供支持
     */
    public void enableLegacyMode(boolean enable) {
        this.enableLegacyMode = enable;
        log.info("Legacy annotation mode {}", enable ? "enabled" : "disabled");
    }

    /**
     * 检查当前是否为legacy模式
     */
    public boolean isLegacyModeEnabled() {
        return enableLegacyMode;
    }

    /**
     * 获取硬编码数据处理器（为扩展提供访问）
     */
    public HardcodedDataProcessor getHardcodedProcessor() {
        return hardcodedProcessor;
    }
}