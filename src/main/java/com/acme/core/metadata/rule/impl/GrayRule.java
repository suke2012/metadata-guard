
package com.acme.core.metadata.rule.impl;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.model.MetaDefinition;
import com.acme.core.metadata.rule.MetaValidationRule;
import com.acme.core.metadata.rule.ValidationContext;

public class GrayRule implements MetaValidationRule {
    @Override public int order(){ return 24;}
    @Override public void validate(String k,Object v, MetaDefinition d, ValidationContext ctx) throws MetaViolationException{
        if(d==null || !Boolean.TRUE.equals(d.getGrayEnabled())) return;
        Integer ratio = d.getGrayRatio(); if(ratio==null||ratio<=0) return;
        String uid = String.valueOf(ctx.env("userId"));
        if(uid==null||uid.length()<2) return;
        int bucket = Integer.parseInt(uid.substring(uid.length()-2));
        if(bucket>=ratio) return; // 未命中灰度
    }
}
