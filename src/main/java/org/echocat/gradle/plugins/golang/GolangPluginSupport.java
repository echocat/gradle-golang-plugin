package org.echocat.gradle.plugins.golang;

import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.tasks.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

import javax.annotation.Nonnull;

public abstract class GolangPluginSupport implements Plugin<Project> {

    protected static final String INSTANCE_PROPERTY_NAME = "org.echocat.gradle.plugins.golang.plugin";

    @Override
    public void apply(Project project) {
        final ConfigurationContainer configurations = project.getConfigurations();
        configurations.maybeCreate("test");
        configurations.maybeCreate("build");
        configurations.maybeCreate("tool");

        final ExtensionContainer extensions = project.getExtensions();
        final ExtensionAware golang = (ExtensionAware) extensions.create("golang", GolangSettings.class, true, project);
        golang.getExtensions().create("build", BuildSettings.class, true, project);
        golang.getExtensions().create("toolchain", ToolchainSettings.class, true, project);
        golang.getExtensions().create("dependencies", DependenciesSettings.class, true, project);
        golang.getExtensions().create("testing", TestingSettings.class, true, project);

        extensions.create(INSTANCE_PROPERTY_NAME, Reference.class, this);

        final TaskContainer tasks = project.getTasks();
        addTasks(tasks);
    }

    protected void addTasks(@Nonnull TaskContainer tasks) {
        tasks.replace(realTaskNameFor("clean"), Clean.class);
        tasks.replace(realTaskNameFor("baseClean"), BaseClean.class);
        tasks.replace(realTaskNameFor("validate"), Validate.class);
        tasks.replace(realTaskNameFor("baseValidate"), BaseValidate.class);
        tasks.replace(realTaskNameFor("prepareToolchain"), PrepareToolchain.class);
        tasks.replace(realTaskNameFor("prepareSources"), PrepareSources.class);
        tasks.replace(realTaskNameFor("basePrepareSources"), BasePrepareSources.class);
        tasks.replace(realTaskNameFor("getTools"), GetTools.class);
        tasks.replace(realTaskNameFor("baseGetTools"), BaseGetTools.class);
        tasks.replace(realTaskNameFor("test"), Test.class);
        tasks.replace(realTaskNameFor("baseTest"), BaseTest.class);
        tasks.replace(realTaskNameFor("build"), Build.class);
        tasks.replace(realTaskNameFor("baseBuild"), BaseBuild.class);
    }

    @Nonnull
    protected String realTaskNameFor(@Nonnull String simpleTaskName) {
        return simpleTaskName;
    }

    @Nonnull
    public static GolangPluginSupport pluginFor(@Nonnull Project project) {
        final Reference reference = (Reference) project.getExtensions().getByName(INSTANCE_PROPERTY_NAME);
        return reference.getReference();
    }

    @Nonnull
    public static String realTaskNameFor(@Nonnull Project project, @Nonnull String simpleTaskName) {
        return pluginFor(project).realTaskNameFor(simpleTaskName);
    }

    public static class Reference {

        @Nonnull
        private final GolangPluginSupport _reference;

        public Reference(@Nonnull GolangPluginSupport reference) {
            _reference = reference;
        }

        @Nonnull
        public GolangPluginSupport getReference() {
            return _reference;
        }

    }

}
