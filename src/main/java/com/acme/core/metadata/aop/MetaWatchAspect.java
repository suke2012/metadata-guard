
package com.acme.core.metadata.aop;

import com.acme.core.metadata.MetaViolationException;
import com.acme.core.metadata.MetadataGuard;
import com.acme.core.metadata.annotation.MetaWatch;
import com.acme.core.metadata.rule.ValidationContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Aspect
@Component
public class MetaWatchAspect {

    @Value("${meta.guard.enabled:true}")
    private boolean enabled;

    @Value("${meta.guard.sample:1.0}")
    private double sample;

    private final MetadataGuard guard;
    private final Random rnd = new Random();

    public MetaWatchAspect(MetadataGuard guard){ this.guard = guard; }

    @Around("@annotation(watch)")
    public Object around(ProceedingJoinPoint pjp, MetaWatch watch) throws Throwable{
        if(!enabled || rnd.nextDouble() >= sample){
            return pjp.proceed();
        }
        ValidationContext ctx = new ValidationContext(watch.mode());
        for(Object arg: pjp.getArgs()){
            guard.validate(arg, ctx);
        }
        return pjp.proceed();
    }
}
