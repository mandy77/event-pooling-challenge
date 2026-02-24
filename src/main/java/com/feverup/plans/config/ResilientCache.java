package com.feverup.plans.config;

import java.util.concurrent.Callable;

import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;

public class ResilientCache implements Cache {
    private final Cache primary;
    private final Cache fallback;

    public ResilientCache(Cache primary, Cache fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @NonNull
    @Override
    public String getName() {
        return primary.getName();
    }

    @NonNull
    @Override
    public Object getNativeCache() {
        return primary.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        try {
            ValueWrapper value = primary.get(key);
            return value != null ? value : fallback.get(key);
        } catch (RedisConnectionFailureException ex) {
            return fallback.get(key);
        }
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        try {
            T value = primary.get(key, type);
            return value != null ? value : fallback.get(key, type);
        } catch (RedisConnectionFailureException ex) {
            return fallback.get(key, type);
        }
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        try {
            return primary.get(key, valueLoader);
        } catch (RedisConnectionFailureException ex) {
            try {
                return fallback.get(key, valueLoader);
            } catch (Exception inner) {
                throw new ValueRetrievalException(key, valueLoader, inner);
            }
        }
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        try {
            primary.put(key, value);
        } catch (RedisConnectionFailureException ex) {
            fallback.put(key, value);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(@NonNull Object key, Object value) {
        try {
            ValueWrapper existing = primary.putIfAbsent(key, value);
            return existing != null ? existing : fallback.putIfAbsent(key, value);
        } catch (RedisConnectionFailureException ex) {
            return fallback.putIfAbsent(key, value);
        }
    }

    @Override
    public void evict(@NonNull Object key) {
        try {
            primary.evict(key);
        } catch (RedisConnectionFailureException ex) {
            fallback.evict(key);
        }
    }

    @Override
    public void clear() {
        try {
            primary.clear();
        } catch (RedisConnectionFailureException ex) {
            fallback.clear();
        }
    }
}
