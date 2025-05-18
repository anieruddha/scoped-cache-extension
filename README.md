# quarkus-scope-cache-extension

A custom CDI-scoped caching extension for Quarkus that allows per-method and per-scope caching using @CacheScoped and @UseCache annotations. Built on top of Caffeine, the cache is type-safe, configurable, and avoids unnecessary database access within a single execution scope.

#### Motivation

In a project I’ve been working on, we had a clever mechanism in place to cache data during execution:
lookup maps were created and passed around to avoid repeated database hits.

Honestly, it worked quite well — especially in the early stages. The approach was efficient and avoided unnecessary DB calls by storing and reusing intermediate results.

But as the execution flows grew more complex, the pattern began to stretch a bit thin.
Methods started needing additional cache maps — sometimes multiple — and those had to be pass through several methods.

What began as a smart optimization slowly became harder to manage:
• It made method signatures noisy
• It scattered caching logic across the codebase
• It became tricky to track what was cached and what not

At this point, I started exploring application-level caching [link](https://quarkus.io/guides/cache) — keeping shared cache maps around for the duration of the app. But in practice, that introduced a new problem…

Our app runs on multiple ECS containers, each with its own isolated memory space. That meant:
•	Cache contents diverged across containers
•	Updates in one ECS weren’t visible to another
•	We had inconsistent behavior and surprising bugs

```

+------------------------++----------------------+
| Container#1            | Container #2          |
| (UserCache)            | (UserCache)           |
+------------------------++----------------------+
|                        |                       |
|   GET /user/123        |   GET /user/123       |
|----------------------->|-----------------------+
|                        |                       |
|  Cached user outdated  |  User recently updated|
|                        |  (in ECS #2 only)     |
|                        |                       |
| Different result from same service!            |

```

So, I needed a cache that was:
•	Scoped to the execution flow
•	Configurable without polluting method signatures
•	reusable 

This extension was born from that need — a scoped caching mechanism.

#### Features
```
- Scoped Caching: Cache is active only within the annotated method's scope.
- Type Safety: Enforced via annotation parameters.
- Custom Config: Supports per-cache configuration via application.properties.
- Built with Caffeine - So I can limit no. of records in cache.
```

### Installation

Add the dependency to your Quarkus app:

```xml
<dependency>
  <groupId>io.github.anieruddha</groupId>
  <artifactId>quarkus-scope-cache-extension</artifactId>
  <version>1.0.0</version>
</dependency>

```

Usage

Step 1: Annotate your method with @CacheScoped
```Java
@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @CacheScoped(name = "UserCache", keyType = String.class, valueType = User.class)
    public void processUsers() {
        userRepository.findById("123"); // Cached
        userRepository.findById("123"); // From cache
    }
}
```

Step 2: Annotate your repository methods with @UseCache

```
@ApplicationScoped
public class UserRepository {

    @UseCache(name = "UserCache", keyType = String.class, valueType = User.class)
    public User findById(String id) {
        // Simulate DB access
        return new User(id, "John");
    }
}
```


> If a @UseCache is used without an active @CacheScoped, cache will be bypassed (no error thrown).

Configuration

Add cache settings in application.properties:
```properties
 # default 10 items
scope.cache.<name>.max-size=10
# default 30 seconds
scoped.cache.<name>.expire-in-seconds-after-write=30
```

### Contributing

PRs welcome! Please fork the repo and follow conventional Quarkus extension structure.

### License

[MIT](https://mit-license.org/)