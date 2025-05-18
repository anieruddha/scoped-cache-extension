package io.github.anieruddha.scoped.cache.extension.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.Arrays;
import java.util.Optional;

@Interceptor
@UseCache(name = "", keyType = Object.class, valueType = Object.class)
public class UseCacheInterceptor {

    @Inject
    CacheStore cacheStore;

    @AroundInvoke
    public Object cacheResult(InvocationContext ctx) throws Exception {
        UseCache annotation = ctx.getMethod().getAnnotation(UseCache.class);
        if (annotation == null) return ctx.proceed();

        String name = annotation.name();
        if (!CacheScopedInterceptor.isScopeActive(name)) {
            return ctx.proceed();
        }

        Class<?> keyType = annotation.keyType();
        Class<?> valueType = annotation.valueType();

        @SuppressWarnings("unchecked")
        Cache<Object, Object> cache = (Cache<Object, Object>) cacheStore.getOrCreate(name, keyType, valueType);

        var key = Arrays.deepHashCode(ctx.getParameters());
        var optCacheValue = Optional.ofNullable(cache.getIfPresent(key));

        if (optCacheValue.isPresent()) return optCacheValue.get();

        var result = ctx.proceed();
        cache.put(key, result);
        return result;
    }
}