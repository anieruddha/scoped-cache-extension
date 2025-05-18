package io.github.anieruddha.scoped.cache.extension.runtime;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.HashSet;
import java.util.Set;

@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@CacheScoped(name = "", keyType = Object.class, valueType = Object.class)
public class CacheScopedInterceptor {

    private static final Set<String> activeScopes = new HashSet<>();
    @Inject
    CacheStore cacheStore;

    public static boolean isScopeActive(String name) {
        return activeScopes.contains(name);
    }

    @AroundInvoke
    public Object manageScope(InvocationContext ctx) throws Exception {
        CacheScoped annotation = ctx.getMethod().getAnnotation(CacheScoped.class);
        if (annotation == null) {
            return ctx.proceed();
        }

        activeScopes.add(annotation.name());

        try {
            return ctx.proceed();
        } finally {
            if (annotation != null) {
                activeScopes.remove(annotation.name());
            }

            cacheStore.clearAll();
        }
    }
}