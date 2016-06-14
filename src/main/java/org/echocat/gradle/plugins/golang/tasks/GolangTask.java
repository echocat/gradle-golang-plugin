package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.model.*;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import javax.annotation.Nonnull;

public abstract class GolangTask extends DefaultTask {

    @Nonnull
    private final GolangSettings _globalGolang;
    @Nonnull
    private final BuildSettings _globalBuild;
    @Nonnull
    private final ToolchainSettings _globalToolchain;
    @Nonnull
    private final DependenciesSettings _globalDependencies;
    @Nonnull
    private final TestingSettings _globalTesting;

    @Nonnull
    private final GolangSettings _taskGolang;
    @Nonnull
    private final BuildSettings _taskBuild;
    @Nonnull
    private final ToolchainSettings _taskToolchain;
    @Nonnull
    private final DependenciesSettings _taskDependencies;
    @Nonnull
    private final TestingSettings _taskTesting;

    private GolangSettings _mergedGolang;
    private BuildSettings _mergedBuild;
    private ToolchainSettings _mergedToolchain;
    private DependenciesSettings _mergedDependencies;
    private TestingSettings _mergedTesting;

    public GolangTask() {
        setGroup("build");
        final Project project = getProject();

        _globalGolang = project.getExtensions().getByType(GolangSettings.class);
        final ExtensionContainer globalExtensions = ((ExtensionAware) _globalGolang).getExtensions();
        _globalBuild = globalExtensions.getByType(BuildSettings.class);
        _globalToolchain = globalExtensions.getByType(ToolchainSettings.class);
        _globalDependencies = globalExtensions.getByType(DependenciesSettings.class);
        _globalTesting = globalExtensions.getByType(TestingSettings.class);

        _taskGolang =  getExtensions().create("golang", GolangSettings.class, false, project);
        final ExtensionContainer taskExtensions = ((ExtensionAware) _taskGolang).getExtensions();
        _taskBuild = taskExtensions.create("build", BuildSettings.class, false, project);
        _taskToolchain = taskExtensions.create("toolchain", ToolchainSettings.class, false, project);
        _taskDependencies = taskExtensions.create("dependencies", DependenciesSettings.class, false, project);
        _taskTesting = taskExtensions.create("testing", TestingSettings.class, false, project);
    }

    @Nonnull
    protected GolangSettings getGolang() {
        final GolangSettings result = _mergedGolang;
        if (result == null) {
            throw new IllegalStateException("Not called within runBare().");
        }
        return result;
    }

    @Nonnull
    protected BuildSettings getBuild() {
        final BuildSettings result = _mergedBuild;
        if (result == null) {
            throw new IllegalStateException("Not called within runBare().");
        }
        return result;
    }

    @Nonnull
    protected DependenciesSettings getDependencies() {
        final DependenciesSettings result = _mergedDependencies;
        if (result == null) {
            throw new IllegalStateException("Not called within runBare().");
        }
        return result;
    }

    @Nonnull
    protected ToolchainSettings getToolchain() {
        final ToolchainSettings result = _mergedToolchain;
        if (result == null) {
            throw new IllegalStateException("Not called within runBare().");
        }
        return result;
    }

    @Nonnull
    protected TestingSettings getTesting() {
        final TestingSettings result = _mergedTesting;
        if (result == null) {
            throw new IllegalStateException("Not called within runBare().");
        }
        return result;
    }

    @Nonnull
    public GolangSettings getGlobalGolang() {
        return _globalGolang;
    }

    @Nonnull
    public BuildSettings getGlobalBuild() {
        return _globalBuild;
    }

    @Nonnull
    public ToolchainSettings getGlobalToolchain() {
        return _globalToolchain;
    }

    @Nonnull
    public DependenciesSettings getGlobalDependencies() {
        return _globalDependencies;
    }

    @Nonnull
    public TestingSettings getGlobalTesting() {
        return _globalTesting;
    }

    @TaskAction
    public final void runBare() throws Exception {
        _mergedGolang = _globalGolang.merge(_taskGolang);
        _mergedBuild = _globalBuild.merge(_taskBuild);
        _mergedToolchain = _globalToolchain.merge(_taskToolchain);
        _mergedDependencies = _globalDependencies.merge(_taskDependencies);
        _mergedTesting = _globalTesting.merge(_taskTesting);
        try {
            run();
        } finally {
            _mergedGolang = null;
            _mergedBuild = null;
            _mergedToolchain = null;
            _mergedDependencies = null;
            _mergedTesting = null;
        }
    }

    public abstract void run() throws Exception;

}
