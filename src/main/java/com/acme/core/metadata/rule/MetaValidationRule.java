
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;
public interface MetaValidationRule {
    int order();
    void validate(ValidationUnit unit) throws MetaViolationException;
}
