
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.MetadataGuard;
import com.acme.core.metadata.metric.MetaViolationCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ValidationContext {
    private static final Logger LOG = LoggerFactory.getLogger("MetaViolation");
    private final MetadataGuard.Mode mode;
    private final Map<String,Object> env = new HashMap<>();
    
    public ValidationContext(MetadataGuard.Mode mode){ this.mode = mode; }
    public MetadataGuard.Mode mode(){ return mode;}
    public void putEnv(String k,Object v){ env.put(k,v);}
    public <T> T env(String k){ return (T)env.get(k);}
    public int envSize(){ return env.size();}
    
    /**
     * 复制环境变量到当前上下文
     * @param source 源上下文
     */
    public void copyEnvFrom(ValidationContext source) {
        if (source != null && source.env != null) {
            this.env.putAll(source.env);
        }
    }
    
    public void violate(String msg) throws MetaViolationException{
        if(mode==MetadataGuard.Mode.INTERCEPT){
            throw new MetaViolationException(msg);
        }else{
            LOG.warn(msg);
            MetaViolationCounter.violation();
        }
    }
}
