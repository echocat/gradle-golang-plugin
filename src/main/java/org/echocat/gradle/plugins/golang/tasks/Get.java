package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.DependencyHandler;
import org.echocat.gradle.plugins.golang.model.GolangDependency;
import org.echocat.gradle.plugins.golang.vcs.CombinedVcsRepositoryProvider;
import org.echocat.gradle.plugins.golang.vcs.VcsRepositoryProvider;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;

public class Get extends GolangTask {

    public Get() {
        setGroup("build");
        dependsOn("validate", "prepare-toolchain");
    }

    @Nonnull
    private final VcsRepositoryProvider _vcsRepositoryProvider = new CombinedVcsRepositoryProvider();

    @Override
    public void run() throws Exception {
        final DependencyHandler dependencyHandler = new DependencyHandler(getSettings());
        final Collection<GolangDependency> handledDependencies = dependencyHandler.get(null);
        final Collection<Path> deletedDependencies = dependencyHandler.deleteUnknownDependenciesIfRequired();
        if (!handledDependencies.isEmpty() || !deletedDependencies.isEmpty()) {
            getState().upToDate();
        }
    }

}
