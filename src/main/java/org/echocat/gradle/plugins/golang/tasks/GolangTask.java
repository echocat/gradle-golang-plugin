package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.DependenciesSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.echocat.gradle.plugins.golang.model.ToolchainSettings;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nonnull;

public abstract class GolangTask extends DefaultTask {

    @Nonnull
    private final GolangSettings _settings;

    public GolangTask() {
        _settings = getProject().getExtensions().getByType(GolangSettings.class);
        setGroup("build");
    }

    @Nonnull
    protected GolangSettings settings() {
        return _settings;
    }

    @Nonnull
    protected BuildSettings build() {
        return settings().build();
    }

    @Nonnull
    protected DependenciesSettings dependencies() {
        return settings().dependencies();
    }

    @Nonnull
    protected ToolchainSettings toolchain() {
        return settings().toolchain();
    }

    @TaskAction
    public abstract void run() throws Exception;

}
