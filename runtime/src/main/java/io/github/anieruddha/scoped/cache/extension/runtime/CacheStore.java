package io.github.anieruddha.scoped.cache.extension.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.Config;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class CacheStore {
    private final Map<String, Cache<?, ?>> cacheMap = new ConcurrentHashMap<>();
    @Inject
    Config config;

    public <K, V> Cache<K, V> getOrCreate(String name, Class<K> keyType, Class<V> valueType) {

        long maxSize = config.getOptionalValue("scoped.cache.%s.max-size".formatted(name), Long.class).orElse(10L);
        long expireAfter = config.getOptionalValue("scoped.cache.%s.expire-in-seconds-after-write".formatted(name), Long.class).orElse(30L);

        return (Cache<K, V>) cacheMap.computeIfAbsent(name, n ->
                Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(Duration.ofSeconds(expireAfter))
                        .build()
        );
    }

    public void clearAll() {
        cacheMap.clear();
    }
}