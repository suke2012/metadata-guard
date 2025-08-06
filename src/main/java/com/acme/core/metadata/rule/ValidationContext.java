
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.MetadataGuard;
import com.acme.core.metadata.collection.MetadataCollectionUnit;
import com.acme.core.metadata.metric.MetaViolationCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationContext {
    private static final Logger LOG = LoggerFactory.getLogger("MetaViolation");
    private final MetadataGuard.Mode mode;
    private String userId;
    private String operateSystem;
    private String prodId;

    public ValidationContext(MetadataGuard.Mode mode){ this.mode = mode; }
    public MetadataGuard.Mode mode(){ return mode;}

    public String userId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String operateSystem(){ return operateSystem; }
    public void setOperateSystem(String operateSystem){ this.operateSystem = operateSystem; }
    public String prodId(){ return prodId; }
    public void setProdId(String prodId){ this.prodId = prodId; }

    /**
     * 复制环境变量到当前上下文
     * @param source 源上下文
     */
    public void copyEnvFrom(MetadataCollectionUnit source) {
        if (source != null) {
            if(source.getUserId() != null) this.userId = source.getUserId();
            if(source.getOperateSystem() != null) this.operateSystem = source.getOperateSystem();
            if(source.getProdId() != null) this.prodId = source.getProdId();
        }
    }
    
    /**
     * 从ValidationContext复制环境变量
     * @param source 源ValidationContext
     */
    public void copyEnvFrom(ValidationContext source) {
        if (source != null) {
            if(source.userId() != null) this.userId = source.userId();
            if(source.operateSystem() != null) this.operateSystem = source.operateSystem();
            if(source.prodId() != null) this.prodId = source.prodId();
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
