package com.acme.core.metadata;

import com.acme.core.metadata.collection.MetadataCollectionUnit;

import java.util.List;

/**
 * 数据转换器接口 - 简化版
 * 负责将上游参数转换为监控单元
 * 不再支持自动匹配，需要显式指定转换器类型
 */
public interface DataConverter {
    
    /**
     * 从上游参数中收集监控单元
     * 
     * @param args 上游传入的原始参数
     * @return 收集到的监控单元列表
     */
    List<MetadataCollectionUnit> convert(Object... args);
    
    /**
     * 获取转换器描述
     * 
     * @return 转换器描述信息
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
}