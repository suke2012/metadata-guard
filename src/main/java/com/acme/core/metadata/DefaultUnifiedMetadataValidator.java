package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;
import com.acme.core.metadata.converter.DataConverter;
import com.acme.core.metadata.model.MetaDefinition;
import com.acme.core.metadata.registry.MetadataRegistryService;
import com.acme.core.metadata.rule.ValidationContext;
import com.acme.core.metadata.rule.ValidationPipeline;
import com.acme.core.metadata.rule.ValidationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一元数据验证器的默认实现
 */
public class DefaultUnifiedMetadataValidator implements UnifiedMetadataValidator {

    private static final Logger log = LoggerFactory.getLogger("MetaViolation");

    private final MetadataRegistryService registry;
    private final Map<String, DataConverter> converters = new ConcurrentHashMap<>();

    public DefaultUnifiedMetadataValidator(MetadataRegistryService registry) {
        this.registry = registry;
    }

    @Override
    public void validate(MetadataCollectionUnit unit) throws MetaViolationException {
        if (unit == null) return;

        // 没有找到专用转换器，使用默认处理逻辑
        validateWithDefaultLogic(unit);

    }

    /**
     * 使用指定转换器进行验证（简化版本）
     */
    private void validate(Object data, DataConverter converter, ValidationContext context) throws MetaViolationException {
        Map<String, Object> kvs = converter.convert(data);
        if (kvs != null && !kvs.isEmpty()) {
            // 创建MetadataCollectionUnit来调用新的validateKeyValues方法
            MetadataCollectionUnit unit = new MetadataCollectionUnit(context.mode());
            unit.setUserId(context.userId());
            unit.setOperateSystem(context.operateSystem());
            unit.setProdId(context.prodId());
            unit.addMetadataFields(kvs);
            validateKeyValues(unit);
        }
    }

    /**
     * 默认验证逻辑（重构版本）
     * 监控模式判断已移到validateKeyValues层面
     */
    private void validateWithDefaultLogic(MetadataCollectionUnit unit) throws MetaViolationException {
        // 直接调用核心验证，监控模式判断在validateKeyValues内部处理
        validateKeyValues(unit);
    }


    /**
     * 核心验证方法 - 公开接口，供外部调用
     * 简化版本，异常处理交给调用方统一处理
     */
    public void validateKeyValues(MetadataCollectionUnit unit) throws MetaViolationException {
        Map<String, MetaDefinition> defs = registry.getAll();
        ValidationPipeline pipe = ValidationPipeline.instance();

        for (Map.Entry<String, Object> entry : unit.getAllValidationData().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            MetaDefinition def = defs.get(key);

            // 检查元数据中心的监控模式配置，优先级高于接口级配置
            // 这里是真正解析最终监控模式的地方
            MetadataCollectionUnit actualContext = resolveValidationMode(unit, def);

            ValidationUnit validationUnit = new ValidationUnit(key, value, def, actualContext);
            pipe.validate(validationUnit);
        }
    }


    /**
     * 解析实际的验证模式
     * 优先级：元数据中心配置 > 接口级配置
     */
    private MetadataCollectionUnit resolveValidationMode(MetadataCollectionUnit unit, MetaDefinition def) {
        if (def != null && def.getValidationMode() != null) {
            // 元数据中心有配置，使用元数据中心的配置
            MetadataCollectionUnit metaCenterContext = new MetadataCollectionUnit(def.getValidationMode());
            // 保留接口级的其他环境变量
            metaCenterContext.copyEnvFrom(unit);
            return metaCenterContext;
        } else {
            // 元数据中心没有配置，使用接口级配置
            return unit;
        }
    }

}