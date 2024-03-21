package com.been.catego.config;

import com.been.catego.aop.YoutubeApiTraceAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {

    @Bean
    public YoutubeApiTraceAspect traceAspect() {
        return new YoutubeApiTraceAspect();
    }
}
