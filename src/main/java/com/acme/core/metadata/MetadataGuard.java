
package com.acme.core.metadata;

import com.acme.core.metadata.rule.ValidationContext;

public interface MetadataGuard {
    void validate(Object target, ValidationContext ctx) throws MetaViolationException;
    enum Mode { MONITOR, INTERCEPT }
}
