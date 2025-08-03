package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;
import java.util.Map;

/**
 * 监控单元处理器接口
 * 负责对收集到的监控单元进行额外处理，专门处理特殊KV数据
 * 处理结果统一合并到metadata的KV对中，支持路径前缀避免键冲突
 */
public interface UnitProcessor {

    /**
     * 检查是否支持处理给定的监控单元
     * 
     * @param unit 待检查的监控单元
     * @return true表示支持处理，false表示跳过
     */
    boolean supports(MetadataCollectionUnit unit);
    
    /**
     * 获取处理器负责的字段路径前缀
     * 例如：extInfo.creditExtInfo
     * 
     * @return 字段路径前缀
     */
    String getFieldPathPrefix();
    
    /**
     * 获取处理器的优先级
     * 数值越小优先级越高，多个处理器会按优先级顺序执行
     * 
     * @return 优先级数值
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
    
    /**
     * 获取处理器描述
     * 
     * @return 描述信息
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 处理监控单元（核心方法）
     * 
     * @param unit 原始监控单元
     * @return 处理后的监控单元
     */
    MetadataCollectionUnit process(MetadataCollectionUnit unit);
    
}