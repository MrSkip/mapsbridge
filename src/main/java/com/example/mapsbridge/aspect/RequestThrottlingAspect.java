package com.example.mapsbridge.aspect;

import com.example.mapsbridge.config.logging.LoggingContext;
import com.example.mapsbridge.service.ratelimit.RequestThrottlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Aspect to apply throttling to map converter controller endpoints.
 * Uses IP-based rate limiters to throttle requests from each client IP address
 * according to the configured rate limit.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestThrottlingAspect {

    private final RequestThrottlingService requestThrottlingService;

    /**
     * Pointcut that matches convert methods in all map converter controllers.
     */
    @Pointcut("execution(* com.example.mapsbridge.controller.ShortcutMapConverterController.convert(..)) || " +
            "execution(* com.example.mapsbridge.controller.WebMapConverterController.convert(..))")
    public void mapConverterControllerMethods() {
    }

    /**
     * Advice that applies throttling to all map converter controller methods.
     * Uses IP-based rate limiters to throttle requests from each client IP address.
     */
    @Around("mapConverterControllerMethods()")
    public Object throttleMapControllers(ProceedingJoinPoint joinPoint) throws Throwable {
        requestThrottlingService.checkThrottlingForIp(LoggingContext.getIpAddress());
        return joinPoint.proceed();
    }
}