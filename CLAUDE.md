# CLAUDE.md

此文件为 Claude Code (claude.ai/code) 在此代码库中工作提供指导。

## 构建命令

这是一个基于Maven的Java项目。常用命令：

- **构建**: `mvn compile`
- **运行测试**: `mvn test`  
- **清理构建**: `mvn clean compile`
- **打包**: `mvn package`
- **运行指定测试**: `mvn test -Dtest=DefaultMetadataGuardImmutabilityTest`

## 架构概览

这是一个名为"元数据守卫"的元数据验证框架，提供统一入口验证和基于AOP注解的传统验证方式。框架已重构为支持统一验证方法，同时保持向后兼容性。

### 核心组件

**统一验证系统（主要方式）**
- `UnifiedMetadataValidator`: 新的统一验证接口，支持多种数据结构类型
- `DefaultUnifiedMetadataValidator`: 主要实现，支持可插拔数据转换器
- `UnifiedMetadataValidationFacade`: 门面模式，提供统一入口并兼容传统模式
- `DataConverter`: 业务特定数据转换逻辑的接口

**传统MetadataGuard（向后兼容）**
- `MetadataGuard`: 传统验证接口，支持两种模式（MONITOR, INTERCEPT）
- `DefaultMetadataGuard`: 使用反射和`@MetaField`注解的传统实现

**AOP集成**
- `@MetaWatch`: 方法级注解，触发对方法参数的验证
- `MetaWatchAspect`: Spring AspectJ切面，拦截带`@MetaWatch`注解的方法
- 支持采样率（`meta.guard.sample`）和启用/禁用标志（`meta.guard.enabled`）

**验证管道**
- `ValidationPipeline`: 单例模式，使用Java ServiceLoader编排验证规则
- 规则从`META-INF/services/com.acme.core.metadata.rule.MetaValidationRule`加载
- 规则按照其`order()`方法的返回值顺序执行

**内置验证规则**
- `KeyPresenceRule`: 验证必需的元数据键
- `ValueRangeRule`: 验证值范围和约束  
- `GrayRule`: 基于用户ID分桶实现灰度发布逻辑

**数据转换系统**
- `CreditExtInfoConverter`: 处理creditExtInfo的示例转换器，包含mock的toString/toMap方法
- `DefaultObjectConverter`: 使用反射处理@MetaField注解对象的兜底转换器
- 可插拔的转换器注册系统，支持业务特定的数据转换

**硬编码数据扩展点**
- `HardcodedDataExtractor`: 从硬编码数据结构提取监控字段的接口
- `HardcodedMappingConfig`: 硬编码数据到监控字段的映射配置
- `HardcodedDataProcessor`: 硬编码数据处理管理器（为将来的dtoA.A=Str[0]场景预留）

**注册服务**
- `MetadataRegistryService`: 带缓存的元数据定义管理
- `DefaultMetadataRegistryService`: 可配置缓存时长的实现（默认PT5M）
- `MetaDefinition`: 增强了双重验证模式配置

### Spring配置

框架通过以下方式与Spring集成：
- `com.acme`包的组件扫描
- 使用CGLIB代理的AspectJ自动代理
- `applicationContext.xml`中核心服务的Bean定义

### 示例包

`com.acme.demo`包包含示例DTO（`Account`, `CreditAccount`），展示了`@MetaField`注解的使用模式。

## 核心设计模式

- **门面模式**: UnifiedMetadataValidationFacade提供统一入口
- **策略模式**: 不同数据类型的可插拔DataConverter实现
- **ServiceLoader模式**: 可插拔验证规则
- **模板方法模式**: 可扩展的硬编码数据处理框架
- **桥接模式**: 统一接口连接传统注解模式和新统一模式
- **单例模式**: ValidationPipeline实例
- **AOP模式**: 横切关注点验证（传统模式）
- **缓存**: 按类缓存字段反射结果以提高性能

## 对外入口接口

**推荐使用的统一入口**：`MetadataValidator`

这是极简版的类型安全接口，只提供一个对外方法：

### 核心流程
```
List<同类型DTO> + 显式转换器类型 -> DataConverter -> CollectionUnit -> UnitProcessor -> Validator
```

### 接口定义
```java
public interface MetadataValidator {
    void validate(List<Object> dtoList, Class<? extends DataConverter> converterClass) throws MetaViolationException;
    void registerConverter(DataConverter converter);
    void registerUnitProcessor(UnitProcessor processor);
}
```

### 使用方式
```java
@Autowired
private MetadataValidator metadataValidator;

// 单个对象验证
List<Object> accountList = Arrays.asList(account);
metadataValidator.validate(accountList, UserDataConverter.class);

// 批量同类型对象验证
List<Object> accounts = Arrays.asList(account1, account2, account3);
metadataValidator.validate(accounts, UserDataConverter.class);

// 业务方法中的使用
public void businessMethod(List<Account> accounts) {
    // 转换为Object列表，显式指定转换器
    List<Object> dtoList = new ArrayList<>(accounts);
    metadataValidator.validate(dtoList, UserDataConverter.class);
}
```

### 扩展方式
```java
// 1. 注册数据转换器（业务方实现）
metadataValidator.registerConverter(new UserDataConverter());

// 2. 注册单元处理器（处理特殊map等）
metadataValidator.registerUnitProcessor(new CreditInfoProcessor());
```

**架构优势**：
- **极简接口**：只有一个对外方法，使用简单
- **类型统一**：强制要求列表中的对象为同一类型，避免混乱
- **编码感知**：显式指定转换器，开发时就有明确感知
- **批量处理**：天然支持同类型对象的批量验证
- **类型安全**：编译时就能发现转换器类型错误

### 方式1：业务方自己组装数据
```java
@Autowired
private MetadataValidationService validationService;

// 业务方自己收集数据
Map<String, Object> assembledData = new HashMap<>();
assembledData.put("userId", user.getId());
assembledData.put("system", user.getSystem());
assembledData.put("age", profile.getAge());

validationService.validate(assembledData, context);
```

### 方式2：传入方法参数，使用转换器
```java
// 在业务方法中：A.a(X x, B b)
public void businessMethod(X x, B b) {
    Object[] methodArgs = {x, b};
    
    // 方式2a：使用预注册的转换器
    validationService.validate(methodArgs, "myConverter", context);
    
    // 方式2b：直接传入转换器实例
    MethodArgsConverter converter = new MyMethodArgsConverter();
    validationService.validate(methodArgs, converter, context);
}

// 实现转换器
class MyMethodArgsConverter implements MethodArgsConverter {
    public Map<String, Object> convert(Object[] methodArgs) {
        X x = (X) methodArgs[0];
        B b = (B) methodArgs[1];
        
        Map<String, Object> result = new HashMap<>();
        result.put("userId", x.getUserId());
        result.put("system", x.getSystem());
        result.put("age", b.getAge());
        return result;
    }
}
```

**双重验证模式配置**:
- 接口级：在ValidationContext中设置（兜底）
- 元数据中心级：在MetaDefinition中设置（优先级）
- 优先级：元数据中心配置 > 接口配置

**业务集成点**:
- 实现DataConverter进行自定义数据转换
- 通过UnifiedMetadataValidator.registerConverter()注册转换器
- 使用HardcodedDataProcessor处理遗留硬编码数据场景