
package com.acme.core.metadata.rule.impl;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.model.MetaDefinition;
import com.acme.core.metadata.rule.MetaValidationRule;
import com.acme.core.metadata.rule.ValidationContext;

public class KeyPresenceRule implements MetaValidationRule {
    @Override public int order(){ return 10;}
    @Override public void validate(String k,Object v, MetaDefinition d, ValidationContext ctx) throws MetaViolationException{
        if(d==null){ ctx.violate("未知元数据键:"+k);}
    }
}
