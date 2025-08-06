package com.acme.core.metadata.collection;

import com.acme.core.metadata.MetadataGuard;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 元数据采集单元
 * 包含固定的元数据配置字段和动态的元数据kv对
 */
public class MetadataCollectionUnit {
    
    // 固定的元数据配置字段
    private String userId;        // 用户ID
    private String operateSystem; // 操作系统/来源系统
    private String prodId;        // 产品ID
    private MetadataGuard.Mode mode = MetadataGuard.Mode.MONITOR;
    
    // 动态的元数据kv对
    private Map<String, Object> metadataFields;
    
    public MetadataCollectionUnit() {
        this.metadataFields = new HashMap<>();
    }

    public MetadataCollectionUnit(MetadataGuard.Mode mode) {
        this.mode = mode;
    }
    
    public MetadataCollectionUnit(String userId, String operateSystem, String prodId) {
        this();
        this.userId = userId;
        this.operateSystem = operateSystem;
        this.prodId = prodId;
    }
    
    /**
     * 添加元数据字段
     */
    public void addMetadataField(String key, Object value) {
        if (key != null && value != null) {
            metadataFields.put(key, value);
        }
    }
    
    /**
     * 批量添加元数据字段
     */
    public void addMetadataFields(Map<String, Object> fields) {
        if (fields != null) {
            metadataFields.putAll(fields);
        }
    }
    
    /**
     * 获取所有需要验证的数据（固定字段 + 动态字段）
     * 用于传递给验证管道
     */
    public Map<String, Object> getAllValidationData() {
        Map<String, Object> allData = new HashMap<>(metadataFields);
        
        // 添加固定字段到验证数据中
        if (userId != null) allData.put("userId", userId);
        if (operateSystem != null) allData.put("operateSystem", operateSystem);
        if (prodId != null) allData.put("prodId", prodId);
        
        return allData;
    }
    
    /**
     * 检查采集单元是否有效（至少有userId）
     */
    public boolean isValid() {
        return userId != null && !userId.trim().isEmpty();
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getOperateSystem() {
        return operateSystem;
    }
    
    public void setOperateSystem(String operateSystem) {
        this.operateSystem = operateSystem;
    }
    
    public String getProdId() {
        return prodId;
    }
    
    public void setProdId(String prodId) {
        this.prodId = prodId;
    }
    
    public Map<String, Object> getMetadataFields() {
        return new HashMap<>(metadataFields);
    }

    public MetadataGuard.Mode getMode() {
        return mode;
    }

    public void setMode(MetadataGuard.Mode mode) {
        this.mode = mode;
    }

    public void setMetadataFields(Map<String, Object> metadataFields) {
        this.metadataFields = metadataFields != null ? new HashMap<>(metadataFields) : new HashMap<>();
    }
    
    /**
     * 复制环境变量从另一个单元
     */
    public void copyEnvFrom(MetadataCollectionUnit other) {
        if (other != null) {
            this.userId = other.userId;
            this.operateSystem = other.operateSystem;
            this.prodId = other.prodId;
            // 不复制mode，因为mode可能需要被覆盖
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataCollectionUnit that = (MetadataCollectionUnit) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(operateSystem, that.operateSystem) &&
               Objects.equals(prodId, that.prodId) &&
               Objects.equals(metadataFields, that.metadataFields);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, operateSystem, prodId, metadataFields);
    }
    
    @Override
    public String toString() {
        return "MetadataCollectionUnit{" +
               "userId='" + userId + '\'' +
               ", operateSystem='" + operateSystem + '\'' +
               ", prodId='" + prodId + '\'' +
               ", metadataFields=" + metadataFields +
               '}';
    }
}