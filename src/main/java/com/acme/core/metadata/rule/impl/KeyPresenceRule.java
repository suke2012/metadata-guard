
package com.acme.core.metadata.rule.impl;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.rule.MetaValidationRule;
import com.acme.core.metadata.rule.ValidationUnit;

public class KeyPresenceRule implements MetaValidationRule {
    @Override public int order(){ return 10;}
    @Override public void validate(ValidationUnit unit) throws MetaViolationException{
        if(unit.definition()==null){ unit.context().violate("未知元数据键:"+unit.key());}
    }
}
