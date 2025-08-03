package com.acme.demo.processors;

import com.acme.core.metadata.AbstractUnitProcessor;
import com.acme.core.metadata.collection.MetadataCollectionUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * 嵌套字段处理器示例
 * 处理已被CreditInfoProcessor解析后的深层嵌套数据
 * 路径前缀：extInfo.creditExtInfo.nested，进一步处理creditExtInfo中的嵌套数据
 * 优先级：100（较低优先级，在CreditInfoProcessor之后执行）
 */
public class NestedFieldProcessor extends AbstractUnitProcessor {
    
    private static final String FIELD_PATH_PREFIX = "extInfo.creditExtInfo.nested";
    
    @Override
    protected Map<String, Object> processSpecialFields(MetadataCollectionUnit unit) {
        Map<String, Object> result = new HashMap<>();
        
        // 提取需要处理的嵌套字段（已经被前面的processor处理过）
        Map<String, Object> targetFields = extractTargetFields(unit.getMetadataFields());
        
        for (Map.Entry<String, Object> entry : targetFields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            log.debug("Processing nested field: {} = {}", key, value);
            
            // 处理特定的嵌套逻辑，例如风险等级细分
            if (key.contains("riskLevel") && value instanceof String) {
                Map<String, Object> riskDetails = parseRiskLevel((String) value);
                result.putAll(riskDetails);
            }
            
            // 处理VIP等级的详细信息
            if (key.contains("vipLevel") && value instanceof Integer) {
                Map<String, Object> vipDetails = parseVipLevel((Integer) value);
                result.putAll(vipDetails);
            }
        }
        
        return result;
    }
    
    @Override
    protected boolean isTargetField(String fieldKey, Object fieldValue) {
        // 检查是否是已经解析的creditExtInfo字段中需要进一步处理的嵌套数据
        return fieldKey != null && fieldValue != null &&
               (fieldKey.equals("extInfo.creditExtInfo.riskLevel") || 
                fieldKey.equals("extInfo.creditExtInfo.vipLevel"));
    }
    
    @Override
    public String getFieldPathPrefix() {
        return FIELD_PATH_PREFIX;
    }
    
    @Override
    public int getOrder() {
        return 100; // 较低优先级，在CreditInfoProcessor之后执行
    }
    
    @Override
    public String getDescription() {
        return "NestedFieldProcessor: 处理creditExtInfo中的嵌套数据";
    }
    
    /**
     * 解析风险等级详细信息
     */
    private Map<String, Object> parseRiskLevel(String riskLevel) {
        Map<String, Object> details = new HashMap<>();
        
        switch (riskLevel.toLowerCase()) {
            case "low":
                details.put("riskScore", 1);
                details.put("riskCategory", "安全");
                break;
            case "medium":
                details.put("riskScore", 5);
                details.put("riskCategory", "中等");
                break;
            case "high":
                details.put("riskScore", 9);
                details.put("riskCategory", "高风险");
                break;
            default:
                details.put("riskScore", 0);
                details.put("riskCategory", "未知");
        }
        
        return details;
    }
    
    /**
     * 解析VIP等级详细信息
     */
    private Map<String, Object> parseVipLevel(Integer vipLevel) {
        Map<String, Object> details = new HashMap<>();
        
        if (vipLevel >= 8) {
            details.put("vipCategory", "钻石");
            details.put("privilegeLevel", "最高");
        } else if (vipLevel >= 5) {
            details.put("vipCategory", "黄金");
            details.put("privilegeLevel", "高");
        } else if (vipLevel >= 2) {
            details.put("vipCategory", "白银");
            details.put("privilegeLevel", "中");
        } else {
            details.put("vipCategory", "普通");
            details.put("privilegeLevel", "基础");
        }
        
        return details;
    }
}