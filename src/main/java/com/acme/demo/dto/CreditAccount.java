
package com.acme.demo.dto;

import com.acme.core.metadata.annotation.MetaField;
import java.util.Map;

public class CreditAccount {
    @MetaField("creditExtInfo")
    private Map<String,Object> extInfo;
    public Map<String,Object> getExtInfo(){return extInfo;}
    public void setExtInfo(Map<String,Object> m){ this.extInfo = m;}
}
