
package com.acme.core.metadata.model;

import com.acme.core.metadata.MetadataGuard;
import java.time.Instant;
import java.util.Set;

public class MetaDefinition {
    private String key;
    private String valuePattern;
    private Set<String> allowedSources;
    private Instant afterTime;
    private Boolean grayEnabled;
    private Integer grayRatio;
    private MetadataGuard.Mode validationMode; // 元数据中心级的监控模式配置
    // getters and setters
    public String getKey(){return key;}
    public void setKey(String k){ this.key = k;}
    public String getValuePattern(){return valuePattern;}
    public void setValuePattern(String v){ this.valuePattern = v;}
    public Set<String> getAllowedSources(){return allowedSources;}
    public void setAllowedSources(Set<String> s){ this.allowedSources = s;}
    public Instant getAfterTime(){return afterTime;}
    public void setAfterTime(Instant t){ this.afterTime = t;}
    public Boolean getGrayEnabled(){return grayEnabled;}
    public void setGrayEnabled(Boolean g){ this.grayEnabled = g;}
    public Integer getGrayRatio(){return grayRatio;}
    public void setGrayRatio(Integer r){ this.grayRatio = r;}
    public MetadataGuard.Mode getValidationMode(){return validationMode;}
    public void setValidationMode(MetadataGuard.Mode mode){ this.validationMode = mode;}
}
