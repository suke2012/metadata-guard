package com.acme.demo.processors;

import com.acme.core.metadata.AbstractUnitProcessor;
import com.acme.core.metadata.collection.MetadataCollectionUnit;

import java.util.HashMap;
import java.util.Map;

/**
 * 信用信息处理器
 * 专注处理creditExtInfo的解码转换
 * 将编码的creditExtInfo字符串重新转换为map格式
 * 路径前缀：extInfo.creditExtInfo，避免与其他字段的键冲突
 * 优先级：50（较高优先级，需要在嵌套处理器之前执行）
 */
public class CreditInfoProcessor extends AbstractUnitProcessor {
    
    private static final String FIELD_PATH_PREFIX = "extInfo.creditExtInfo";

    @Override
    protected Map<String, Object> processSpecialFields(MetadataCollectionUnit unit) {
        Map<String, Object> result = new HashMap<>();
        
        // 提取需要处理的creditExtInfo字段
        Map<String, Object> targetFields = extractTargetFields(unit.getMetadataFields());
        
        for (Map.Entry<String, Object> entry : targetFields.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            log.debug("Converting creditExtInfo field: {} = {}", key, value);
            
            // 将编码的creditExtInfo字符串重新转换为map
            Map<String, Object> convertedData = decodeCreditExtInfo(value);
            result.putAll(convertedData);
        }
        
        return result;
    }
    
    @Override
    protected boolean isTargetField(String fieldKey, Object fieldValue) {
        // 检查是否是creditExtInfo相关字段且需要解码处理
        return isCreditExtInfoField(fieldKey) && needsSpecialProcessing(fieldValue);
    }
    
    @Override
    public String getFieldPathPrefix() {
        return FIELD_PATH_PREFIX;
    }
    
    @Override
    public int getOrder() {
        return 50; // 较高优先级，需要在嵌套处理器之前执行
    }
    
    @Override
    public String getDescription() {
        return "CreditInfoProcessor: 解码creditExtInfo字符串为map格式";
    }
    
    /**
     * 检查是否是creditExtInfo相关字段
     */
    private boolean isCreditExtInfoField(String key) {
        return key != null && (key.contains("creditExtInfo") || 
                              key.startsWith("extInfo.") || 
                              key.equals("creditExtInfo"));
    }
    
    /**
     * 将编码的creditExtInfo重新解码为map
     */
    private Map<String, Object> decodeCreditExtInfo(Object creditExtInfo) {
        Map<String, Object> result = new HashMap<>();

        if (creditExtInfo instanceof String) {
            // 解析字符串格式的creditExtInfo（已被编码的数据）
            String str = (String) creditExtInfo;
            if (str.contains("vipLevel")) {
                result.put("vipLevel", extractValue(str, "vipLevel"));
            }
            if (str.contains("creditScore")) {
                result.put("creditScore", extractValue(str, "creditScore"));
            }
            if (str.contains("riskLevel")) {
                result.put("riskLevel", extractValue(str, "riskLevel"));
            }
            if (str.contains("creditLimit")) {
                result.put("creditLimit", extractValue(str, "creditLimit"));
            }
        } else if (creditExtInfo instanceof Map) {
            // 直接处理Map类型的creditExtInfo
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) creditExtInfo;
            result.putAll(map);
        } else {
            // 其他类型，作为原始数据处理
            result.put("rawData", creditExtInfo.toString());
        }

        return result;
    }


    /**
     * 检查值是否需要特殊处理
     */
    private boolean needsSpecialProcessing(Object value) {
        if (value == null) {
            return false;
        }

        // 这里可以根据实际情况判断
        // 比如：字符串格式的复杂数据、特殊的对象类型等
        if (value instanceof String) {
            String str = (String) value;
            return str.contains("{") || str.contains("=") || str.length() > 50;
        }

        // 或者其他特殊类型的判断
        return value.getClass().getName().contains("CreditExtInfo");
    }


    /**
     * Mock的字符串值提取
     */
    private Object extractValue(String str, String key) {
        // 简单的mock实现，实际会更复杂
        String pattern = key + "=";
        int startIndex = str.indexOf(pattern);
        if (startIndex != -1) {
            startIndex += pattern.length();
            int endIndex = str.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = str.indexOf("}", startIndex);
            }
            if (endIndex == -1) {
                endIndex = str.length();
            }

            String valueStr = str.substring(startIndex, endIndex).trim();

            // 尝试转换为数字
            try {
                return Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                return valueStr;
            }
        }

        return null;
    }

}