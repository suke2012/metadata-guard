
package com.acme.core.metadata.registry.impl;

import com.acme.core.metadata.model.MetaDefinition;
import com.acme.core.metadata.registry.MetadataRegistryService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultMetadataRegistryService implements MetadataRegistryService {

    private volatile Map<String, MetaDefinition> cache = Collections.emptyMap();
    private final AtomicLong last = new AtomicLong(0);
    private final long ttl;

    public DefaultMetadataRegistryService(Duration ttl){ this.ttl = ttl.toMillis(); refresh(); }

    @Override public Map<String, MetaDefinition> getAll(){
        if(System.currentTimeMillis()-last.get()>ttl){ refresh();}
        return cache;
    }
    @Override public synchronized void refresh(){
        List<MetaDefinition> list = mock();
        Map<String, MetaDefinition> m = new HashMap<>();
        list.forEach(d-> m.put(d.getKey(),d));
        cache = Collections.unmodifiableMap(m);
        last.set(System.currentTimeMillis());
    }
    private List<MetaDefinition> mock(){
        MetaDefinition age = new MetaDefinition(); age.setKey("age"); age.setValuePattern("0-120"); age.setProductCode("P1"); age.setChangeSystem("pccp");
        MetaDefinition vip = new MetaDefinition(); vip.setKey("vipLevel"); vip.setValuePattern("1,2,3,4,5"); vip.setProductCode("P1"); vip.setChangeSystem("pccp");
        MetaDefinition news = new MetaDefinition(); news.setKey("newsFlag"); news.setAfterTime(Instant.now().minus(Duration.ofDays(1)));
        MetaDefinition gray = new MetaDefinition(); gray.setKey("brokerId"); gray.setGrayEnabled(true); gray.setGrayRatio(20);
        return Arrays.asList(age,vip,news,gray);
    }
}
