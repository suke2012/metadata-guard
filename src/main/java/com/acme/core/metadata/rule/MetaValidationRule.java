
package com.acme.core.metadata.rule;

import com.acme.core.metadata.MetaViolationException;
import java.util.Collections;
import java.util.Set;

public interface MetaValidationRule {
    int order();
    void validate(ValidationUnit unit) throws MetaViolationException;
    default Set<String> fields(){ return Collections.emptySet(); }
}
