
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;

import java.util.*;
import java.util.stream.Collectors;

public class ValidationPipeline {
    private static final List<MetaValidationRule> RULES;
    private static final Map<String,List<MetaValidationRule>> FIELD_RULES;
    static{
        ServiceLoader<MetaValidationRule> loader = ServiceLoader.load(MetaValidationRule.class);
        List<MetaValidationRule> ls = new ArrayList<>();
        Map<String,List<MetaValidationRule>> map = new HashMap<>();
        loader.forEach(r->{
            ls.add(r);
            for(String f: r.fields()){
                map.computeIfAbsent(f,k->new ArrayList<>()).add(r);
            }
        });
        ls.sort(Comparator.comparingInt(MetaValidationRule::order));
        RULES = Collections.unmodifiableList(ls);
        FIELD_RULES = Collections.unmodifiableMap(map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e->Collections.unmodifiableList(e.getValue()))));
    }
    public void validate(ValidationUnit unit) throws MetaViolationException{
        for(MetaValidationRule r: RULES){
            r.validate(unit);
        }
    }
    public List<MetaValidationRule> rulesForField(String field){
        return FIELD_RULES.getOrDefault(field, Collections.emptyList());
    }
    private ValidationPipeline(){}
    private static class Holder{ private static final ValidationPipeline INST = new ValidationPipeline();}
    public static ValidationPipeline instance(){ return Holder.INST; }
}
