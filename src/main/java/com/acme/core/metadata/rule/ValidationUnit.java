package com.acme.core.metadata.rule;

import com.acme.core.metadata.model.MetaDefinition;

/**
 * 封装单次校验所需的全部信息
 */
public class ValidationUnit {
    private final String key;
    private final Object value;
    private final MetaDefinition definition;
    private final ValidationContext context;

    public ValidationUnit(String key, Object value, MetaDefinition definition, ValidationContext context) {
        this.key = key;
        this.value = value;
        this.definition = definition;
        this.context = context;
    }

    public String key() { return key; }
    public Object value() { return value; }
    public MetaDefinition definition() { return definition; }
    public ValidationContext context() { return context; }
}
