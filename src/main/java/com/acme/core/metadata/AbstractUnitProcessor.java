package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 单元处理器抽象基类
 * 提供统一的KV合并逻辑，业务子类只需专注数据转换
 * 支持优先级排序和嵌套数据的分层处理
 */
public abstract class AbstractUnitProcessor implements UnitProcessor {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public final MetadataCollectionUnit process(MetadataCollectionUnit unit) {
        if (unit == null || !supports(unit)) {
            return unit;
        }
        
        // 创建副本避免修改原数据
        MetadataCollectionUnit result = createCopy(unit);
        Map<String, Object> currentFields = result.getMetadataFields();
        
        log.debug("Before processing - fields count: {}", currentFields.size());
        currentFields.forEach((k, v) -> log.debug("  Before: {} = {}", k, v));
        
        // 执行业务特定的数据转换
        Map<String, Object> convertedData = processSpecialFields(unit);
        
        if (convertedData != null && !convertedData.isEmpty()) {
            String pathPrefix = getFieldPathPrefix();
            
            // 统一合并转换后的数据到metadata中
            convertedData.forEach((key, value) -> {
                String finalKey = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
                currentFields.put(finalKey, value);
                log.debug("Added converted field: {} = {}", finalKey, value);
            });
            
            log.info("Processor {} converted {} fields with prefix: {}", 
                    getDescription(), convertedData.size(), pathPrefix);
                    
            log.debug("After processing - fields count: {}", currentFields.size());
            currentFields.forEach((k, v) -> log.debug("  After: {} = {}", k, v));
        }
        
        return result;
    }
    
    /**
     * 业务子类实现具体的数据转换逻辑
     * 
     * @param unit 原始监控单元
     * @return 转换后的键值对，key不需要包含路径前缀
     */
    protected abstract Map<String, Object> processSpecialFields(MetadataCollectionUnit unit);
    
    /**
     * 检查给定的字段是否是当前处理器负责的字段
     * 
     * @param fieldKey 字段名
     * @param fieldValue 字段值
     * @return true表示需要处理
     */
    protected abstract boolean isTargetField(String fieldKey, Object fieldValue);
    
    @Override
    public boolean supports(MetadataCollectionUnit unit) {
        if (unit == null || unit.getMetadataFields() == null) {
            return false;
        }
        
        // 检查是否包含当前处理器负责的字段
        return unit.getMetadataFields().entrySet().stream()
                .anyMatch(entry -> isTargetField(entry.getKey(), entry.getValue()));
    }
    
    /**
     * 创建监控单元的副本
     */
    public final MetadataCollectionUnit createCopy(MetadataCollectionUnit original) {
        MetadataCollectionUnit copy = new MetadataCollectionUnit();
        copy.setUserId(original.getUserId());
        copy.setOperateSystem(original.getOperateSystem());
        copy.setProdId(original.getProdId());
        copy.setMetadataFields(new HashMap<>(original.getMetadataFields()));
        return copy;
    }
    
    /**
     * 从字段中提取目标数据进行转换
     * 
     * @param fields 所有字段
     * @return 需要转换的字段数据
     */
    protected final Map<String, Object> extractTargetFields(Map<String, Object> fields) {
        Map<String, Object> targetFields = new HashMap<>();
        
        fields.entrySet().stream()
                .filter(entry -> isTargetField(entry.getKey(), entry.getValue()))
                .forEach(entry -> targetFields.put(entry.getKey(), entry.getValue()));
        
        return targetFields;
    }
}