
package com.acme.core.metadata.rule.impl;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.rule.MetaValidationRule;
import com.acme.core.metadata.rule.ValidationUnit;

public class GrayRule implements MetaValidationRule {
    @Override public int order(){ return 24;}
    @Override public void validate(ValidationUnit unit) throws MetaViolationException{
        if(unit.definition()==null || !Boolean.TRUE.equals(unit.definition().getGrayEnabled())) return;
        Integer ratio = unit.definition().getGrayRatio(); if(ratio==null||ratio<=0) return;
        String uid = String.valueOf(unit.context().env("userId"));
        if(uid==null||uid.length()<2) return;
        int bucket = Integer.parseInt(uid.substring(uid.length()-2));
        if(bucket>=ratio) return; // 未命中灰度
    }
}
