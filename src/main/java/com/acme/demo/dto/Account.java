
package com.acme.demo.dto;

import com.acme.core.metadata.annotation.MetaField;

import java.util.Map;

public class Account {
    @MetaField("extInfo")
    private Map<String,Object> extInfo;
    @MetaField("system")
    private String system;
    @MetaField("time")
    private long time;
    @MetaField("userId")
    private String userId;
    private Map<String,CreditAccount> creditMap;
    // getters setters
    public Map<String,Object> getExtInfo(){return extInfo;}
    public void setExtInfo(Map<String,Object> m){ this.extInfo = m;}
    public String getSystem(){return system;}
    public void setSystem(String s){ this.system = s;}
    public long getTime(){return time;}
    public void setTime(long t){ this.time = t;}
    public String getUserId(){return userId;}
    public void setUserId(String u){ this.userId = u;}
    public Map<String,CreditAccount> getCreditMap(){return creditMap;}
    public void setCreditMap(Map<String,CreditAccount> c){ this.creditMap=c;}
}
