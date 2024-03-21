package com.been.catego.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.UUID;

@Slf4j(topic = "YOUTUBE_API_LOG")
@Aspect
public class YoutubeApiTraceAspect {

    @Around("execution(* com.been.catego.service.YouTubeApiDataService..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        Long startTimeMs = System.currentTimeMillis();
        String signature = joinPoint.getSignature().toShortString();

        log.info("[{}] {}", traceId, signature);

        try {
            Object result = joinPoint.proceed();

            log.info("[{}] {} time={}ms", traceId, signature, getResultTimeMs(startTimeMs));
            return result;
        } catch (Exception e) {
            log.error("[{}] {} time={}ms", traceId, signature, getResultTimeMs(startTimeMs), e);
            throw e;
        }
    }

    private static long getResultTimeMs(Long startTimeMs) {
        return System.currentTimeMillis() - startTimeMs;
    }
}
