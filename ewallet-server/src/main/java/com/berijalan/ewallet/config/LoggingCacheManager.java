package com.berijalan.ewallet.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Wrapper CacheManager untuk mencatat hit, miss, put, evict, dan clear tanpa mengubah implementasi Redis cache.
 */
@RequiredArgsConstructor
public class LoggingCacheManager implements CacheManager {

    private final CacheManager delegate;

    @Override
    public Cache getCache(String name) {
        Cache cache = delegate.getCache(name);
        return cache != null ? new LoggingCache(cache) : null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }

    @Slf4j
    @RequiredArgsConstructor
    static class LoggingCache implements Cache {

        private final Cache delegate;

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper value = delegate.get(key);
            if (value != null) {
                log.debug("Cache hit: {}::{}", delegate.getName(), key);
            } else {
                log.debug("Cache miss: {}::{}", delegate.getName(), key);
            }
            return value;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            return delegate.get(key, type);
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            return delegate.get(key, valueLoader);
        }

        @Override
        public void put(Object key, Object value) {
            log.debug("Cache put: {}::{}", delegate.getName(), key);
            delegate.put(key, value);
        }

        @Override
        public void evict(Object key) {
            log.debug("Cache evict: {}::{}", delegate.getName(), key);
            delegate.evict(key);
        }

        @Override
        public void clear() {
            log.debug("Cache clear: {}", delegate.getName());
            delegate.clear();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }
    }
}
