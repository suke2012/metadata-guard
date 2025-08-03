package com.acme.core.metadata.converter;

import com.acme.core.metadata.annotation.MetaField;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认对象转换器
 * 使用反射从对象中提取@MetaField注解的字段
 */
public class DefaultObjectConverter implements DataConverter {
    
    private final Map<Class<?>, Field[]> fieldCache = new ConcurrentHashMap<>();
    
    @Override
    public Map<String, Object> convert(Object data) {
        if (data == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> result = new HashMap<>();
        
        if (data instanceof Map) {
            // 直接处理Map类型
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            result.putAll(map);
        } else {
            // 使用反射处理对象
            extractFromObject(data, result);
        }
        
        return result;
    }
    
    /**
     * 从对象中提取带@MetaField注解的字段
     */
    private void extractFromObject(Object obj, Map<String, Object> result) {
        Class<?> clazz = obj.getClass();
        Field[] fields = fieldCache.computeIfAbsent(clazz, c -> c.getDeclaredFields());
        
        for (Field field : fields) {
            MetaField metaField = field.getAnnotation(MetaField.class);
            if (metaField != null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(obj);
                    if (value != null) {
                        String key = metaField.value().isEmpty() ? field.getName() : metaField.value();
                        result.put(key, value);
                    }
                } catch (IllegalAccessException e) {
                    // 忽略访问异常
                }
            }
        }
    }
    
    @Override
    public boolean supports(Object data) {
        if (data == null) return false;
        
        if (data instanceof Map) {
            return true; // 支持所有Map类型
        }
        
        // 检查对象是否有@MetaField注解的字段
        Class<?> clazz = data.getClass();
        Field[] fields = fieldCache.computeIfAbsent(clazz, c -> c.getDeclaredFields());
        
        for (Field field : fields) {
            if (field.getAnnotation(MetaField.class) != null) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE; // 最低优先级，作为兜底转换器
    }
}