
package com.acme.core.metadata;

import com.acme.core.metadata.registry.impl.DefaultMetadataRegistryService;
import com.acme.core.metadata.rule.ValidationContext;
import com.acme.demo.dto.Account;
import com.acme.demo.dto.CreditAccount;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMetadataGuardImmutabilityTest {

    private final MetadataGuard guard =
            new DefaultMetadataGuard(new DefaultMetadataRegistryService(Duration.ofMinutes(5)));

    @Test
    void validate_shouldNotMutate() {
        Account acc = new Account();
        Map<String,Object> ext=new HashMap<>();
        ext.put("age",30);
        acc.setExtInfo(ext);
        acc.setSystem("pccp");
        acc.setTime(System.currentTimeMillis());
        acc.setUserId("12345");

        CreditAccount ca=new CreditAccount();
        Map<String,Object> cext=new HashMap<>(); cext.put("vipLevel",3);
        ca.setExtInfo(cext);
        acc.setCreditMap(Collections.singletonMap("c",ca));

        Map<String,Object> snapExt = acc.getExtInfo();
        Map<String,Object> snapCext = ca.getExtInfo();

        ValidationContext ctx = new ValidationContext(MetadataGuard.Mode.MONITOR);
        assertDoesNotThrow(()-> guard.validate(acc,ctx));

        assertSame(snapExt, acc.getExtInfo());
        assertSame(snapCext, ca.getExtInfo());
        assertEquals(30, acc.getExtInfo().get("age"));
        assertEquals(3, ca.getExtInfo().get("vipLevel"));
    }
}
