package org.echocat.gradle.plugins.golang;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.tasks.*;
import org.echocat.gradle.plugins.golang.utils.DependencyHandlerExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Map.Entry;

public class GolangPlugin implements Plugin<Project> {

    @Nonnull
    private final DefaultDependencyHandler _dependencyHandler;
    @Nonnull
    private final ConfigurationContainer _configurationContainer;

    @Inject
    public GolangPlugin(@Nonnull DependencyHandler dependencyHandler, @Nonnull ConfigurationContainer configurationContainer) {
        _dependencyHandler = (DefaultDependencyHandler) dependencyHandler;
        _configurationContainer = configurationContainer;
    }

    @Override
    public void apply(Project project) {
        final DependencyHandlerExtension dependencyHandlerExtension = new DependencyHandlerExtension(_configurationContainer);
        final GroovyObject ext = (GroovyObject) _dependencyHandler.getProperty("ext");
        for (final Entry<String, Closure<Dependency>> entry : dependencyHandlerExtension.getDependencyMethodsAsClosures().entrySet()) {
            ext.setProperty(entry.getKey(), entry.getValue());
        }

        final ConfigurationContainer configurations = project.getConfigurations();
        configurations.maybeCreate("test");
        configurations.maybeCreate("build");

        final ExtensionContainer extensions = project.getExtensions();
        final ExtensionAware golang = (ExtensionAware) extensions.create("golang", GolangSettings.class, true, project);
        golang.getExtensions().create("build", BuildSettings.class, true, project);
        golang.getExtensions().create("toolchain", ToolchainSettings.class, true, project);
        golang.getExtensions().create("dependencies", DependenciesSettings.class, true, project);
        golang.getExtensions().create("testing", TestingSettings.class, true, project);

        final TaskContainer tasks = project.getTasks();
        tasks.create("validate", Validate.class);
        tasks.create("prepare-toolchain", PrepareToolchain.class);
        tasks.create("prepare-sources", PrepareSources.class);
        tasks.create("get", Get.class);
        tasks.create("build", Build.class);
        tasks.create("clean", Clean.class);

    }

}
