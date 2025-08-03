
package com.acme.core.metadata.annotation;

import com.acme.core.metadata.MetadataGuard;
import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MetaWatch {
    MetadataGuard.Mode mode() default MetadataGuard.Mode.MONITOR;
}
