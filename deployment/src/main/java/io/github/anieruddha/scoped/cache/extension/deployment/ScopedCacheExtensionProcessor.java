package io.github.anieruddha.scoped.cache.extension.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ScopedCacheExtensionProcessor {
    private static final String FEATURE = "scoped-cache-extension";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
