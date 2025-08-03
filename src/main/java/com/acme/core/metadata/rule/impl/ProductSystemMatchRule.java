package com.acme.core.metadata.rule.impl;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.rule.MetaValidationRule;
import com.acme.core.metadata.rule.ValidationUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 检查产品码和变更系统是否与参数中心配置一致
 */
public class ProductSystemMatchRule implements MetaValidationRule {
    @Override
    public int order() { return 20; }
    @Override public Set<String> fields(){ return new HashSet<>(Arrays.asList("prodId","operateSystem")); }

    @Override
    public void validate(ValidationUnit unit) throws MetaViolationException {
        if(unit.definition()==null) return;
        String expectedProd = unit.definition().getProductCode();
        if(expectedProd!=null){
            String actual = unit.context().prodId();
            if(actual!=null && !expectedProd.equals(actual)){
                unit.context().violate("产品码不匹配:"+unit.key());
            }
        }
        String expectedSys = unit.definition().getChangeSystem();
        if(expectedSys!=null){
            String actualSys = unit.context().operateSystem();
            if(actualSys!=null && !expectedSys.equals(actualSys)){
                unit.context().violate("变更系统不匹配:"+unit.key());
            }
        }
    }
}
