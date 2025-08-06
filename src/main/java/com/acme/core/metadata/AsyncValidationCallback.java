package com.acme.core.metadata;

/**
 * 异步验证回调接口
 * 用于处理异步验证的结果
 */
public interface AsyncValidationCallback {
    
    /**
     * 验证成功回调
     * 
     * @param validatedCount 成功验证的对象数量
     */
    void onSuccess(int validatedCount);
    
    /**
     * 验证失败回调
     * 
     * @param exception 验证异常
     * @param failedCount 失败时已处理的对象数量
     */
    void onFailure(Exception exception, int failedCount);
    
    /**
     * 默认的空回调实现
     */
    AsyncValidationCallback EMPTY = new AsyncValidationCallback() {
        @Override
        public void onSuccess(int validatedCount) {
            // 空实现
        }
        
        @Override
        public void onFailure(Exception exception, int failedCount) {
            // 空实现
        }
    };
}