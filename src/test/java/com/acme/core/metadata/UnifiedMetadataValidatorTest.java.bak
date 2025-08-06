package com.acme.core.metadata;

import com.acme.core.metadata.converter.CreditExtInfoConverter;
import com.acme.core.metadata.converter.DefaultObjectConverter;
import com.acme.core.metadata.registry.impl.DefaultMetadataRegistryService;
import com.acme.core.metadata.rule.ValidationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一元数据验证器测试
 */
class UnifiedMetadataValidatorTest {
    
    private UnifiedMetadataValidator validator;
    private ValidationContext context;
    
    @BeforeEach
    void setUp() {
        DefaultMetadataRegistryService registryService = new DefaultMetadataRegistryService(Duration.ofMinutes(5));
        validator = new DefaultUnifiedMetadataValidator(registryService);
        
        // 注册转换器
        validator.registerConverter("creditExtInfo", new CreditExtInfoConverter());
        validator.registerConverter("default", new DefaultObjectConverter());
        
        context = new ValidationContext(MetadataGuard.Mode.MONITOR);
    }
    
    @Test
    void testValidateMapData() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "12345");
        data.put("system", "pccp");
        data.put("age", 30);
        
        // 应该不抛出异常
        assertDoesNotThrow(() -> validator.validate(data, context));
    }
    
    @Test
    void testValidateWithCreditExtInfo() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "12345");
        data.put("system", "pccp");
        
        // 模拟extInfo
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("age", 30);
        data.put("extInfo", extInfo);
        
        // 模拟creditExtInfo
        Map<String, Object> creditExtInfo = new HashMap<>();
        creditExtInfo.put("vipLevel", 3);
        creditExtInfo.put("creditScore", 750);
        data.put("creditExtInfo", creditExtInfo);
        
        // 应该不抛出异常
        assertDoesNotThrow(() -> validator.validate(data, "creditExtInfo", context));
    }
    
    @Test
    void testConverterRegistration() {
        // 测试转换器注册
        assertNotNull(validator.getConverter("creditExtInfo"));
        assertNotNull(validator.getConverter("default"));
        
        // 移除转换器
        validator.removeConverter("creditExtInfo");
        assertNull(validator.getConverter("creditExtInfo"));
    }
    
    @Test
    void testValidationModeResolution() {
        // 测试监控模式解析（接口级配置）
        ValidationContext interceptContext = new ValidationContext(MetadataGuard.Mode.INTERCEPT);
        interceptContext.setUserId("12345");
        
        Map<String, Object> data = new HashMap<>();
        data.put("test", "value");
        
        // 应该不抛出异常（MONITOR模式）
        assertDoesNotThrow(() -> validator.validate(data, context));
    }
}