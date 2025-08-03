package com.acme.core.metadata.extension;

import java.util.Map;

/**
 * 硬编码数据映射配置
 * 定义如何将原始数据映射到监控字段
 */
public class HardcodedMappingConfig {
    
    private String dataType; // 数据类型标识（如"dtoA"）
    private Map<String, String> fieldMappings; // 字段映射（如A -> Str[0], B -> Str[1]）
    private Map<String, Object> metadata; // 额外的元数据配置
    
    public HardcodedMappingConfig() {}
    
    public HardcodedMappingConfig(String dataType, Map<String, String> fieldMappings) {
        this.dataType = dataType;
        this.fieldMappings = fieldMappings;
    }
    
    /**
     * 获取完整的字段名（包含数据类型前缀）
     * @param fieldName 字段名（如"A"）
     * @return 完整字段名（如"dtoA.A"）
     */
    public String getFullFieldName(String fieldName) {
        return dataType != null ? dataType + "." + fieldName : fieldName;
    }
    
    /**
     * 获取字段的映射表达式
     * @param fieldName 字段名
     * @return 映射表达式（如"Str[0]"）
     */
    public String getFieldMapping(String fieldName) {
        return fieldMappings != null ? fieldMappings.get(fieldName) : null;
    }
    
    // Getters and Setters
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    public Map<String, String> getFieldMappings() {
        return fieldMappings;
    }
    
    public void setFieldMappings(Map<String, String> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}