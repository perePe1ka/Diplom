package ru.vladuss.apigateway.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingAspect.class);

    @Around("execution(* ru.vladuss.apigateway.controller..*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {

        long start = System.nanoTime();
        log.debug("ENTER {} args=[{}]",
                pjp.getSignature(),
                formatArgs(pjp.getArgs()));

        Object result = pjp.proceed();

        long took = (System.nanoTime() - start) / 1_000_000;
        log.info("EXIT  {} duration={} ms", pjp.getSignature(), took);

        Stats.record(pjp.getSignature().toShortString(), took);
        return result;
    }

    private String formatArgs(Object[] arr) {
        return Arrays.stream(arr)
                .map(a -> a == null ? "null" : a.getClass().getSimpleName())
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    private static final class Stats {
        private static final ConcurrentHashMap<String, LongAdder> calls = new ConcurrentHashMap<>();
        private static final ConcurrentHashMap<String, LongAdder> time  = new ConcurrentHashMap<>();
        static void record(String key, long ms) {
            calls.computeIfAbsent(key, k -> new LongAdder()).increment();
            time .computeIfAbsent(key, k -> new LongAdder()).add(ms);
        }
    }
}
