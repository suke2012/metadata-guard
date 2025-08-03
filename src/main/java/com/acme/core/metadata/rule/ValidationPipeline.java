
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.model.MetaDefinition;

import java.util.*;

public class ValidationPipeline {
    private static final List<MetaValidationRule> RULES;
    static{
        ServiceLoader<MetaValidationRule> loader = ServiceLoader.load(MetaValidationRule.class);
        List<MetaValidationRule> ls = new ArrayList<>();
        loader.forEach(ls::add);
        ls.sort(Comparator.comparingInt(MetaValidationRule::order));
        RULES = Collections.unmodifiableList(ls);
    }
    public void validate(String k,Object v, MetaDefinition def, ValidationContext ctx) throws MetaViolationException{
        for(MetaValidationRule r: RULES){
            r.validate(k,v,def,ctx);
        }
    }
    private ValidationPipeline(){}
    private static class Holder{ private static final ValidationPipeline INST = new ValidationPipeline();}
    public static ValidationPipeline instance(){ return Holder.INST; }
}
