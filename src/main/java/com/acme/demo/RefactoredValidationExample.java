package com.acme.demo;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.MetadataGuard;
import com.acme.core.metadata.MetadataValidator;
import com.acme.demo.converters.UserDataConverter;
import com.acme.demo.converters.UserProfileConverter;
import com.acme.demo.processors.CreditInfoProcessor;
import com.acme.demo.processors.NestedFieldProcessor;
import com.acme.demo.dto.Account;
import com.acme.demo.dto.CreditAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重构后的验证使用示例 - 极简版
 * 展示新的简化流程：List<DTO> + ConverterClass -> Converter -> CollectionUnit -> Processor -> Validator
 */
@Component
public class RefactoredValidationExample {
    
    @Autowired
    private MetadataValidator metadataValidator;
    
    /**
     * 初始化转换器和处理器（通常在应用启动时完成）
     */
    public void initialize() {
        // 注册数据转换器
        metadataValidator.registerConverter(new UserDataConverter());
        metadataValidator.registerConverter(new UserProfileConverter());
        
        // 注册监控单元处理器（按优先级顺序，优先级越小越先执行）
        metadataValidator.registerUnitProcessor(new CreditInfoProcessor());  // 优先级50，先执行creditExtInfo解码
        metadataValidator.registerUnitProcessor(new NestedFieldProcessor()); // 优先级100，后执行嵌套数据处理
        
        System.out.println("转换器和处理器注册完成");
    }
    
    /**
     * 演示重构后的使用方式（支持监控模式）
     */
    public void demonstrateUsage() {
        try {
            // 场景1：默认告警模式验证
            demonstrateMonitorMode();
            
            // 场景2：拦截模式验证  
            demonstrateInterceptMode();
            
            // 场景3：批量处理同类型对象
            demonstrateBatchProcessing();
            
            // 场景4：带特殊处理的验证
            demonstrateSpecialProcessing();
            
        } catch (MetaViolationException e) {
            System.err.println("验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 场景1：默认告警模式验证
     */
    private void demonstrateMonitorMode() throws MetaViolationException {
        System.out.println("\n=== 场景1：默认告警模式验证 ===");
        
        Account account = createSampleAccount();
        List<Object> accountList = Arrays.asList(account);
        
        // 默认告警模式：验证失败只记录日志，不抛出异常
        metadataValidator.validate(accountList, UserDataConverter.class);
        
        System.out.println("✓ 告警模式验证完成（验证失败只记录告警）");
    }
    
    /**
     * 场景2：拦截模式验证
     */
    private void demonstrateInterceptMode() throws MetaViolationException {
        System.out.println("\n=== 场景2：拦截模式验证 ===");
        
        Account account = createSampleAccount();
        List<Object> accountList = Arrays.asList(account);
        
        // 拦截模式：验证失败会抛出异常
        metadataValidator.validate(accountList, UserDataConverter.class, MetadataGuard.Mode.INTERCEPT);
        
        System.out.println("✓ 拦截模式验证完成（验证失败会抛出异常）");
    }
    
    /**
     * 场景3：带特殊处理的验证
     */
    private void demonstrateSpecialProcessing() throws MetaViolationException {
        System.out.println("\n=== 场景3：特殊map处理验证 ===");
        
        Account account = createSampleAccount();
        CreditAccount creditAccount = createComplexCreditAccount();
        
        // 演示处理多个同类型对象的场景
        List<Object> creditAccountList = Arrays.asList(creditAccount);
        metadataValidator.validate(creditAccountList, UserDataConverter.class);
        
        System.out.println("✓ 特殊处理验证完成（UserProfileConverter + CreditInfoProcessor）");
    }
    
    /**
     * 业务方法示例：A.a(X x, B b)
     * 现在只需要一行代码
     */
    public void businessMethodA(Account account, CreditAccount creditAccount) {
        try {
            // 新的极简调用方式：只能传入同一类型的列表
            List<Object> accountList = Arrays.asList(account);
            metadataValidator.validate(accountList, UserDataConverter.class);
            
            System.out.println("业务方法A验证完成");
        } catch (MetaViolationException e) {
            System.err.println("业务方法A验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 业务方法示例：带用户ID的场景
     */
    public void businessMethodB(String userId, Account account) {
        try {
            // 新接口不再支持userId参数，需要在DTO中包含userId信息
            List<Object> accountList = Arrays.asList(account);
            metadataValidator.validate(accountList, UserDataConverter.class);
            
            System.out.println("业务方法B验证完成");
        } catch (MetaViolationException e) {
            System.err.println("业务方法B验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建示例Account对象
     */
    private Account createSampleAccount() {
        Account account = new Account();
        account.setUserId("user123");
        account.setSystem("businessSystem");
        account.setTime(System.currentTimeMillis());
        
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("age", 28);
        extInfo.put("region", "CN");
        account.setExtInfo(extInfo);
        
        return account;
    }
    
    /**
     * 创建简单的CreditAccount对象
     */
    private CreditAccount createSampleCreditAccount() {
        CreditAccount creditAccount = new CreditAccount();
        
        Map<String, Object> creditExtInfo = new HashMap<>();
        creditExtInfo.put("vipLevel", 2);
        creditExtInfo.put("creditScore", 720);
        creditAccount.setExtInfo(creditExtInfo);
        
        return creditAccount;
    }
    
    /**
     * 创建包含复杂数据的CreditAccount对象（需要特殊处理）
     */
    private CreditAccount createComplexCreditAccount() {
        CreditAccount creditAccount = new CreditAccount();
        
        Map<String, Object> creditExtInfo = new HashMap<>();
        // 模拟需要特殊处理的复杂数据
        creditExtInfo.put("vipLevel", 3);
        creditExtInfo.put("creditScore", 750);
        creditExtInfo.put("riskProfile", "{riskLevel=LOW,score=85,factors=[income,asset]}");
        creditAccount.setExtInfo(creditExtInfo);
        
        return creditAccount;
    }
    
    /**
     * 场景4：批量处理同类型对象
     */
    private void demonstrateBatchProcessing() throws MetaViolationException {
        System.out.println("\n=== 场景4：批量处理同类型对象 ===");
        
        // 创建多个Account对象
        Account account1 = createSampleAccount();
        account1.setUserId("user001");
        
        Account account2 = createSampleAccount();
        account2.setUserId("user002");
        
        Account account3 = createSampleAccount();
        account3.setUserId("user003");
        
        // 批量验证多个同类型对象
        List<Object> accountList = Arrays.asList(account1, account2, account3);
        metadataValidator.validate(accountList, UserDataConverter.class);
        
        System.out.println("✓ 批量验证完成，处理了" + accountList.size() + "个Account对象");
    }
}