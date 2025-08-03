package com.acme.core.metadata.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * CreditExtInfo转换器示例（Mock实现）
 * 在实际项目中，业务方会提供真实的toString/toMap实现
 */
public class CreditExtInfoConverter implements DataConverter {
    
    @Override
    public Map<String, Object> convert(Object data) {
        if (data == null) {
            return new HashMap<>();
        }
        
        // Mock实现：假设data是某种包含creditExtInfo的对象
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;
            return processCreditExtInfo(dataMap);
        }
        
        return new HashMap<>();
    }
    
    /**
     * 处理creditExtInfo的转换逻辑（Mock实现）
     */
    private Map<String, Object> processCreditExtInfo(Map<String, Object> dataMap) {
        Map<String, Object> result = new HashMap<>();
        
        // 处理普通的extInfo字段
        Object extInfo = dataMap.get("extInfo");
        if (extInfo instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> extMap = (Map<String, Object>) extInfo;
            result.putAll(extMap);
        }
        
        // 处理嵌套的creditExtInfo
        Object creditExtInfo = dataMap.get("creditExtInfo");
        if (creditExtInfo != null) {
            // Mock实现：假设creditExtInfo有toString和toMap方法
            Map<String, Object> creditKvs = mockToMap(creditExtInfo);
            // 添加前缀区分来源
            creditKvs.forEach((k, v) -> result.put("credit." + k, v));
        }
        
        return result;
    }
    
    /**
     * Mock的toMap方法实现
     * 实际项目中，业务方会提供真实的util.toMap方法
     */
    private Map<String, Object> mockToMap(Object creditExtInfo) {
        Map<String, Object> result = new HashMap<>();
        
        // Mock实现：模拟从creditExtInfo中提取kv数据
        if (creditExtInfo instanceof String) {
            // 假设toString后的格式可以解析为kv
            String str = (String) creditExtInfo;
            result.put("vipLevel", "3");
            result.put("creditScore", "750");
        } else if (creditExtInfo instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) creditExtInfo;
            result.putAll(map);
        }
        
        return result;
    }
    
    /**
     * Mock的toString方法实现
     * 实际项目中，业务方会提供真实的util.toString方法
     */
    private String mockToString(Object creditExtInfo) {
        // Mock实现：简单的字符串表示
        return creditExtInfo != null ? creditExtInfo.toString() : "";
    }
    
    @Override
    public boolean supports(Object data) {
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            // 检查是否包含creditExtInfo字段
            return map.containsKey("creditExtInfo") || map.containsKey("extInfo");
        }
        return false;
    }
    
    @Override
    public int getOrder() {
        return 100; // 中等优先级
    }
}