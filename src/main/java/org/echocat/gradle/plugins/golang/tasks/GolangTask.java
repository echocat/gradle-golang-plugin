package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.model.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GolangTask extends DefaultTask {

    @Nonnull
    private final Settings _globalSettings;
    @Nonnull
    private final Settings _taskSettings;
    @Nullable
    private Settings _mergedSettings;

    public GolangTask() {
        final Project project = getProject();

        _globalSettings = new Settings(project, project.getExtensions());
        _taskSettings = new Settings(project, getExtensions(), false);
    }

    @Nonnull
    public Settings getGlobalSettings() {
        return _globalSettings;
    }

    @Nonnull
    public Settings getTaskSettings() {
        return _taskSettings;
    }

    @Nonnull
    public Settings getSettings() {
        final Settings result = _mergedSettings;
        if (result == null) {
            throw new IllegalStateException("Not called within runBare().");
        }
        return result;
    }

    @Nonnull
    protected GolangSettings getGolang() {
        return getSettings().getGolang();
    }

    @Nonnull
    protected BuildSettings getBuild() {
        return getSettings().getBuild();
    }

    @Nonnull
    protected DependenciesSettings getDependencies() {
        return getSettings().getDependencies();
    }

    @Nonnull
    protected ToolchainSettings getToolchain() {
        return getSettings().getToolchain();
    }

    @Nonnull
    protected TestingSettings getTesting() {
        return getSettings().getTesting();
    }

    @TaskAction
    public final void runBare() throws Exception {
        _mergedSettings = _globalSettings.merge(_taskSettings);
        try {
            run();
        } finally {
            _mergedSettings = null;
        }
    }

    public abstract void run() throws Exception;

}
