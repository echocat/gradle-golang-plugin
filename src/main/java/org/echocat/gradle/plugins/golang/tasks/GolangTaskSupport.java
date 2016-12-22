package org.echocat.gradle.plugins.golang.tasks;

import groovy.lang.Closure;
import org.echocat.gradle.plugins.golang.DependencyHandler;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.utils.ProjectEnabled;
import org.echocat.gradle.plugins.golang.utils.SettingsEnabled;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GolangTaskSupport extends DefaultTask implements SettingsEnabled, ProjectEnabled {

    @Nonnull
    private final Settings _globalSettings;
    @Nonnull
    private final Settings _taskSettings;
    @Nullable
    private Settings _mergedSettings;

    @Nullable
    private Closure<?> _before;
    @Nullable
    private Closure<?> _after;

    public GolangTaskSupport() {
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

    @Override
    @Nonnull
    public Settings getSettings() {
        final Settings result = _mergedSettings;
        if (result != null) {
            return result;
        }
        return _taskSettings;
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

    protected void runBefore() throws Exception {
        final Closure<?> before = _before;
        if (before != null) {
            before.call();
        }
    }

    protected void runAfter() throws Exception {
        final Closure<?> after = _after;
        if (after != null) {
            after.call();
        }
    }

    @TaskAction
    public final void runBare() throws Exception {
        _mergedSettings = _globalSettings.merge(_taskSettings);
        try {
            runBefore();
            try {
                run();
            } finally {
                runAfter();
            }
        } finally {
            _mergedSettings = null;
        }
    }

    @Nonnull
    protected DependencyHandler getDependencyHandler() {
        return new DependencyHandler(getServices(), getSettings());
    }

    @SuppressWarnings("NoopMethodInAbstractClass")
    public void run() throws Exception {}

    @Nullable
    public Closure<?> getBefore() {
        return _before;
    }

    public void setBefore(@Nullable Closure<?> before) {
        _before = before;
    }

    @Nullable
    public Closure<?> getAfter() {
        return _after;
    }

    public void setAfter(@Nullable Closure<?> after) {
        _after = after;
    }

    public void before(@Nullable Closure<?> before) {
        setBefore(before);
    }

    public void after(@Nullable Closure<?> after) {
        setAfter(after);
    }

    @Nonnull
    protected ProgressLogger startProgress(@Nonnull String description) {
        final ProgressLogger progressLogger = getServices().get(ProgressLoggerFactory.class).newOperation(getClass());
        progressLogger.setDescription(description);
        progressLogger.started();
        return progressLogger;
    }

}
