
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;

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
    public void validate(ValidationUnit unit) throws MetaViolationException{
        for(MetaValidationRule r: RULES){
            r.validate(unit);
        }
    }
    private ValidationPipeline(){}
    private static class Holder{ private static final ValidationPipeline INST = new ValidationPipeline();}
    public static ValidationPipeline instance(){ return Holder.INST; }
}
