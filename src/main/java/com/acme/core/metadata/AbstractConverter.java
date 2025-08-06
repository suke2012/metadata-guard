package com.acme.core.metadata;

import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public abstract class AbstractConverter implements DataConverter{

    @Autowired
    private ConverterFactory converterFactory;

    @PostConstruct
    protected void registerConverter(DataConverter converter) {
        converterFactory.registerConverter(converter);
    }
}
