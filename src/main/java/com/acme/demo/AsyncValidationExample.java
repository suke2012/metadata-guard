package com.acme.demo;

import com.acme.core.metadata.AsyncValidationCallback;
import com.acme.core.metadata.MetadataValidator;
import com.acme.demo.converters.UserDataConverter;
import com.acme.demo.dto.Account;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.List;

/**
 * 异步验证功能使用示例
 * 
 * 展示如何使用异步验证功能，包括：
 * 1. 配置异步开关
 * 2. 使用回调处理异步结果
 * 3. 异步与同步的选择策略
 */
public class AsyncValidationExample {
    
    public static void main(String[] args) throws Exception {
        // 初始化Spring上下文
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        MetadataValidator validator = context.getBean(MetadataValidator.class);
        
        // 准备测试数据
        Account account1 = new Account();
        account1.setUserId("user001");
        account1.setSystem("mobile-app");
        
        Account account2 = new Account();
        account2.setUserId("user002");
        account2.setSystem("web-app");
        
        List<Object> accounts = Arrays.asList(account1, account2);
        
        System.out.println("=== 异步验证功能演示 ===");
        System.out.println();
        
        // 1. 显式异步验证（推荐用法）
        System.out.println("1. 显式异步验证:");
        validator.validateAsync(accounts, UserDataConverter.class, new AsyncValidationCallback() {
            @Override
            public void onSuccess(int validatedCount) {
                System.out.println("  ✓ 异步验证成功，处理了 " + validatedCount + " 个对象");
            }
            
            @Override
            public void onFailure(Exception exception, int failedCount) {
                System.out.println("  ✗ 异步验证失败: " + exception.getMessage());
                System.out.println("    已处理对象数量: " + failedCount);
            }
        });
        
        // 2. 同步验证（阻塞等待结果）
        System.out.println("2. 同步验证:");
        try {
            // 使用MONITOR模式 - 在配置的异步开关下可能会异步执行，但不影响业务流程
            validator.validate(accounts, UserDataConverter.class);
            System.out.println("  ✓ 同步验证完成");
        } catch (Exception e) {
            System.out.println("  ✗ 同步验证失败: " + e.getMessage());
        }
        
        // 等待异步任务完成
        Thread.sleep(2000);
        
        System.out.println();
        System.out.println("=== 配置说明 ===");
        System.out.println("异步功能由以下配置控制：");
        System.out.println("  meta.guard.async.enabled=true/false           # 是否启用异步");
        System.out.println("  meta.guard.async.core-pool-size=2              # 核心线程数");
        System.out.println("  meta.guard.async.max-pool-size=8               # 最大线程数");
        System.out.println("  meta.guard.async.queue-capacity=1000           # 队列容量");
        System.out.println("  meta.guard.async.keep-alive-seconds=60         # 线程存活时间");
        System.out.println();
        System.out.println("=== 使用策略 ===");
        System.out.println("1. MONITOR模式 + 异步开关开启 → 自动异步执行（不影响业务逻辑）");
        System.out.println("2. INTERCEPT模式 → 始终同步执行（需要等待验证结果）");
        System.out.println("3. 显式调用validateAsync() → 强制异步执行（推荐）");
        
        context.close();
    }
}