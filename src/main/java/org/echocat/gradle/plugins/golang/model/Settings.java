package org.echocat.gradle.plugins.golang.model;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

import javax.annotation.Nonnull;

import static org.echocat.gradle.plugins.golang.utils.BeanUtils.copyNonNulls;

public class Settings {

    @Nonnull
    private final Project _project;
    @Nonnull
    private final GolangSettings _golang;
    @Nonnull
    private final BuildSettings _build;
    @Nonnull
    private final ToolchainSettings _toolchain;
    @Nonnull
    private final DependenciesSettings _dependencies;
    @Nonnull
    private final TestingSettings _testing;

    public Settings(@Nonnull Project project, @Nonnull ExtensionContainer container) {
        _project = project;
        _golang = container.getByType(GolangSettings.class);
        if (!(_golang instanceof ExtensionAware)) {
            throw new IllegalStateException("golang instance (" + _golang + ") of provided extension container (" + container + ") is not an instance of " + ExtensionAware.class.getName() + ".");
        }
        final ExtensionContainer globalExtensions = ((ExtensionAware) _golang).getExtensions();
        _build = globalExtensions.getByType(BuildSettings.class);
        _toolchain = globalExtensions.getByType(ToolchainSettings.class);
        _dependencies = globalExtensions.getByType(DependenciesSettings.class);
        _testing = globalExtensions.getByType(TestingSettings.class);
    }

    public Settings(@Nonnull Project project, @Nonnull ExtensionContainer container, boolean init) {
        _project = project;
        _golang = container.create("golang", GolangSettings.class, init, project);
        final ExtensionContainer taskExtensions = ((ExtensionAware) _golang).getExtensions();
        _build = taskExtensions.create("build", BuildSettings.class, init, project);
        _toolchain = taskExtensions.create("toolchain", ToolchainSettings.class, init, project);
        _dependencies = taskExtensions.create("dependencies", DependenciesSettings.class, init, project);
        _testing = taskExtensions.create("testing", TestingSettings.class, init, project);
    }

    public Settings(@Nonnull Project project, @Nonnull GolangSettings golang, @Nonnull BuildSettings build, @Nonnull ToolchainSettings toolchain, @Nonnull DependenciesSettings dependencies, @Nonnull TestingSettings testing) {
        _project = project;
        _golang = golang;
        _build = build;
        _toolchain = toolchain;
        _dependencies = dependencies;
        _testing = testing;
    }

    @Nonnull
    public GolangSettings getGolang() {
        return _golang;
    }

    @Nonnull
    public BuildSettings getBuild() {
        return _build;
    }

    @Nonnull
    public ToolchainSettings getToolchain() {
        return _toolchain;
    }

    @Nonnull
    public DependenciesSettings getDependencies() {
        return _dependencies;
    }

    @Nonnull
    public TestingSettings getTesting() {
        return _testing;
    }

    @Nonnull
    public Project getProject() {
        return _project;
    }

    @Nonnull
    public Settings merge(@Nonnull Settings... with) {
        final GolangSettings golang = new GolangSettings(false, _project);
        final BuildSettings build = new BuildSettings(false, _project);
        final ToolchainSettings toolchain = new ToolchainSettings(false, _project);
        final DependenciesSettings dependencies = new DependenciesSettings(false, _project);
        final TestingSettings testing = new TestingSettings(false, _project);

        copyNonNulls(GolangSettings.class, _golang, golang);
        copyNonNulls(BuildSettings.class, _build, build);
        copyNonNulls(ToolchainSettings.class, _toolchain, toolchain);
        copyNonNulls(DependenciesSettings.class, _dependencies, dependencies);
        copyNonNulls(TestingSettings.class, _testing, testing);

        for (final Settings source : with) {
            copyNonNulls(GolangSettings.class, source.getGolang(), golang);
            copyNonNulls(BuildSettings.class, source.getBuild(), build);
            copyNonNulls(ToolchainSettings.class, source.getToolchain(), toolchain);
            copyNonNulls(DependenciesSettings.class, source.getDependencies(), dependencies);
            copyNonNulls(TestingSettings.class, source.getTesting(), testing);
        }

        return new Settings(_project, golang, build, toolchain, dependencies, testing);
    }
}
