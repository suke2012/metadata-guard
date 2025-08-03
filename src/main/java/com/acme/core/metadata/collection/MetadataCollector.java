package com.acme.core.metadata.collection;

import java.util.List;

/**
 * 元数据收集器接口
 * 由接入方实现，定义如何从原始数据中收集元数据采集单元
 * 
 * 使用场景：
 * 1. 单个对象收集：A.a(User user) -> 收集user的元数据
 * 2. 多个对象收集：A.a(User user, Profile profile) -> 收集user和profile的元数据
 * 3. 列表对象收集：A.a(List<Order> orders) -> 收集每个order的元数据
 */
public interface MetadataCollector {
    
    /**
     * 从原始数据中收集元数据采集单元
     * 
     * @param args 原始数据参数（可变参数）
     *             例如：A.a(User user, Profile profile) 调用时，args = [user对象, profile对象]
     *             例如：A.a(List<Order> orders) 调用时，args = [orders列表]
     * @return 收集到的采集单元列表
     *         - 单个用户场景：返回包含1个单元的列表
     *         - 批量用户场景：返回包含多个单元的列表
     *         - 无效数据场景：返回空列表
     * 
     * 实现示例：
     * ```java
     * public List<MetadataCollectionUnit> collect(Object... args) {
     *     List<MetadataCollectionUnit> units = new ArrayList<>();
     *     
     *     if (args.length >= 1 && args[0] instanceof User) {
     *         User user = (User) args[0];
     *         
     *         MetadataCollectionUnit unit = new MetadataCollectionUnit();
     *         unit.setUserId(user.getId());
     *         unit.setOperateSystem(user.getSystem());
     *         unit.setProdId(user.getProdId());
     *         
     *         // 收集动态字段
     *         unit.addMetadataField("age", user.getAge());
     *         unit.addMetadataField("region", user.getRegion());
     *         
     *         // 处理extInfo
     *         if (user.getExtInfo() != null) {
     *             user.getExtInfo().forEach((k, v) -> 
     *                 unit.addMetadataField("ext." + k, v));
     *         }
     *         
     *         units.add(unit);
     *     }
     *     
     *     return units;
     * }
     * ```
     */
    List<MetadataCollectionUnit> collect(Object... args);
    
    /**
     * 获取收集器的描述信息
     * 用于日志记录和调试，建议包含支持的参数类型信息
     * 
     * @return 描述信息，如 "UserProfileCollector: collect(User, Profile)"
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 检查是否支持给定的参数类型
     * 可选实现，用于参数类型校验
     * 
     * @param args 待检查的参数
     * @return true表示支持，false表示不支持
     */
    default boolean supports(Object... args) {
        return true; // 默认支持所有类型，具体实现可以覆盖此方法
    }
}