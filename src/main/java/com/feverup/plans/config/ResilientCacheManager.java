package com.feverup.plans.config;

import java.util.Collection;
import java.util.Collections;
import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class ResilientCacheManager implements CacheManager {
    private final CacheManager primary;
    private final CacheManager fallback;

    public ResilientCacheManager(CacheManager primary, CacheManager fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    @Override
    public Cache getCache(@NonNull String name) {
        Cache primaryCache = primary != null ? primary.getCache(name) : null;
        Cache fallbackCache = fallback != null ? fallback.getCache(name) : null;
        if (primaryCache == null) {
            return fallbackCache;
        }
        if (fallbackCache == null) {
            return primaryCache;
        }
        return new ResilientCache(primaryCache, fallbackCache);
    }

    @NonNull
    @Override
    public Collection<String> getCacheNames() {
        if (primary == null) {
            return fallback != null ? fallback.getCacheNames() : Collections.emptyList();
        }
        return primary.getCacheNames();
    }
}
