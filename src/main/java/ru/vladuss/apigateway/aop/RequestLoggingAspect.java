package ru.vladuss.apigateway.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingAspect.class);

    @Around("execution(* ru.vladuss.apigateway.controller..*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {

        long start = System.currentTimeMillis();
        log.debug("ENTER  {} args={}", pjp.getSignature(), pjp.getArgs());

        Object result = pjp.proceed();

        long took = System.currentTimeMillis() - start;
        log.info("EXIT   {}  duration={} ms", pjp.getSignature(), took);
        return result;
    }
}

