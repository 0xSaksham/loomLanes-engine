package com.loomlanes.engine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public boolean isAllowed(String clientId, int limit, int windowSeconds) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/rate_limiter.lua")));
        script.setResultType(Long.class);

        String key = "rate_limit:" + clientId;

        Long result = redisTemplate.execute(script,
                Collections.singletonList(key),
                String.valueOf(limit),
                String.valueOf(windowSeconds));

        return result != null && result == 1;
    }
}