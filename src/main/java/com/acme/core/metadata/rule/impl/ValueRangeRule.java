
package com.acme.core.metadata.rule.impl;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.rule.MetaValidationRule;
import com.acme.core.metadata.rule.ValidationUnit;
import java.util.Collections;
import java.util.Set;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ValueRangeRule implements MetaValidationRule {
    @Override public int order(){ return 30;}
    @Override public Set<String> fields(){ return Collections.singleton("*"); }
    @Override public void validate(ValidationUnit unit) throws MetaViolationException{
        if(unit.definition()==null || unit.definition().getValuePattern()==null || unit.value()==null) return;
        String pat = unit.definition().getValuePattern();
        String val = String.valueOf(unit.value());
        if(pat.contains(",")){
            if(!Arrays.asList(pat.split(",")).contains(val)){ unit.context().violate("值["+val+"]不在集合:"+pat);}
        }else if(pat.contains("-")){
            String[] p=pat.split("-"); try{
                long low=Long.parseLong(p[0].trim()), hi=Long.parseLong(p[1].trim()), num=Long.parseLong(val);
                if(num<low||num>hi){ unit.context().violate("值["+val+"]不在区间:"+pat);}
            }catch(NumberFormatException ignore){}
        }else if(pat.startsWith("/")&&pat.endsWith("/")){
            Pattern r = Pattern.compile(pat.substring(1,pat.length()-1));
            if(!r.matcher(val).matches()){ unit.context().violate("值["+val+"]不匹配正则:"+pat);}
        }
    }
}
