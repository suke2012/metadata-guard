
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.model.MetaDefinition;

public interface MetaValidationRule {
    int order();
    void validate(String key,Object value, MetaDefinition def, ValidationContext ctx) throws MetaViolationException;
}
