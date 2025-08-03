package com.acme.core.metadata.rule.impl;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.rule.MetaValidationRule;
import com.acme.core.metadata.rule.ValidationUnit;

/**
 * 检查产品码和变更系统是否与参数中心配置一致
 */
public class ProductSystemMatchRule implements MetaValidationRule {
    @Override
    public int order() { return 20; }

    @Override
    public void validate(ValidationUnit unit) throws MetaViolationException {
        if(unit.definition()==null) return;
        String expectedProd = unit.definition().getProductCode();
        if(expectedProd!=null){
            String actual = unit.context().env("prodId");
            if(actual==null){ actual = unit.context().env("productCode"); }
            if(actual!=null && !expectedProd.equals(actual)){
                unit.context().violate("产品码不匹配:"+unit.key());
            }
        }
        String expectedSys = unit.definition().getChangeSystem();
        if(expectedSys!=null){
            String actualSys = unit.context().env("operateSystem");
            if(actualSys==null){ actualSys = unit.context().env("system"); }
            if(actualSys!=null && !expectedSys.equals(actualSys)){
                unit.context().violate("变更系统不匹配:"+unit.key());
            }
        }
    }
}
