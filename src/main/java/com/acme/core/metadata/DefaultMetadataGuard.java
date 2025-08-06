
package com.acme.core.metadata;

import com.acme.core.metadata.annotation.MetaField;
import com.acme.core.metadata.collection.MetadataCollectionUnit;
import com.acme.core.metadata.model.MetaDefinition;
import com.acme.core.metadata.registry.MetadataRegistryService;
import com.acme.core.metadata.rule.ValidationContext;
import com.acme.core.metadata.rule.ValidationPipeline;
import com.acme.core.metadata.rule.ValidationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultMetadataGuard implements MetadataGuard {

    private static final Logger log = LoggerFactory.getLogger("MetaViolation");

    private final MetadataRegistryService registry;
    private final Map<Class<?>,Field[]> fieldCache = new ConcurrentHashMap<>();

    public DefaultMetadataGuard(MetadataRegistryService registry){ this.registry = registry; }

    @Override public void validate(Object target, ValidationContext ctx) throws MetaViolationException{
        if(target==null) return;
        if(ctx.mode()==Mode.MONITOR){
            try{ doValidate(target,ctx);}catch(Throwable t){ log.warn("MetaMonitorFailed {}",t.getMessage(),t);}
        }else{ doValidate(target,ctx);}
    }

    private void doValidate(Object target, ValidationContext ctx) throws MetaViolationException{
        Map<String,Object> kvs = new HashMap<>();
        collect(target,kvs,ctx);
        Map<String,MetaDefinition> defs = registry.getAll();
        ValidationPipeline pipe = ValidationPipeline.instance();
        for(Map.Entry<String,Object> e: kvs.entrySet()){
            MetaDefinition def = defs.get(e.getKey());
            ValidationContext actual = resolveValidationMode(ctx, def);
            // 创建MetadataCollectionUnit来适配新的ValidationUnit构造函数
            MetadataCollectionUnit unitContext = new MetadataCollectionUnit(actual.mode());
            unitContext.setUserId(actual.userId());
            unitContext.setOperateSystem(actual.operateSystem());
            unitContext.setProdId(actual.prodId());
            ValidationUnit unit = new ValidationUnit(e.getKey(), e.getValue(), def, unitContext);
            pipe.validate(unit);
        }
    }

    private ValidationContext resolveValidationMode(ValidationContext ctx, MetaDefinition def){
        if(def!=null && def.getValidationMode()!=null){
            ValidationContext metaCtx = new ValidationContext(def.getValidationMode());
            metaCtx.copyEnvFrom(ctx);
            return metaCtx;
        }
        return ctx;
    }

    private static boolean isTerminalType(Object val){
        if(val==null) return true;
        Class<?> c=val.getClass();
        if(c.isArray()||c.isPrimitive()) return true;
        Package p=c.getPackage();
        return p!=null && p.getName().startsWith("java.");
    }

    private void collect(Object root, Map<String,Object> kvs, ValidationContext ctx){
        if(root==null) return;
        ArrayDeque<Object> stack = new ArrayDeque<>(32);
        stack.push(root);
        while(!stack.isEmpty()){
            Object obj = stack.pop();
            if(obj instanceof Map){
                ((Map<?,?>)obj).forEach((k,v)->{ if(k instanceof String) kvs.put((String)k,v);});
                continue;
            }
            Class<?> cl = obj.getClass();
            Field[] fields = fieldCache.computeIfAbsent(cl, c-> c.getDeclaredFields());
            for(Field f: fields){
                f.setAccessible(true);
                Object val;
                try{ val = f.get(obj);}catch(IllegalAccessException e){ continue;}
                if(val==null) continue;
                MetaField mf = f.getAnnotation(MetaField.class);
                if(mf!=null){
                    if(val instanceof Map){ stack.push(val);}
                    else{
                        String name = mf.value();
                        if("userId".equals(name)){
                            ctx.setUserId(String.valueOf(val));
                        }else if("operateSystem".equals(name) || "system".equals(name)){
                            ctx.setOperateSystem(String.valueOf(val));
                        }else if("prodId".equals(name) || "productCode".equals(name)){
                            ctx.setProdId(String.valueOf(val));
                        }
                        if(!isTerminalType(val)) stack.push(val);
                    }
                }else{
                    if(!(val instanceof Map) && !isTerminalType(val)) stack.push(val);
                }
            }
        }
    }
}
