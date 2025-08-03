
package com.acme.core.metadata.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class MetaViolationCounter {
    private static final AtomicLong VIOL = new AtomicLong(0);
    private static final AtomicLong BUG  = new AtomicLong(0);
    private static final Logger LOG = LoggerFactory.getLogger("MetaMetric");
    static {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1, r->{
            Thread t = new Thread(r,"meta-metric-flush");
            t.setDaemon(true);
            return t;
        });
        exec.scheduleAtFixedRate(MetaViolationCounter::flush, 60, 60, TimeUnit.SECONDS);
    }
    public static void violation(){ VIOL.incrementAndGet(); }
    public static void bug(){ BUG.incrementAndGet(); }
    private static void flush(){
        long v = VIOL.getAndSet(0);
        long b = BUG.getAndSet(0);
        if (v>0 || b>0){
            LOG.warn("meta_violation_count={} meta_rule_bug_count={}", v, b);
        }
    }
}
