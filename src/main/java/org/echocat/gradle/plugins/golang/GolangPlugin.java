package org.echocat.gradle.plugins.golang;

import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.tasks.*;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskContainer;

public class GolangPlugin implements Plugin<Project> {

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

        final TaskContainer tasks = project.getTasks();
        tasks.replace("clean", Clean.class);
        tasks.replace("baseClean", BaseClean.class);
        tasks.replace("validate", Validate.class);
        tasks.replace("baseValidate", BaseValidate.class);
        tasks.replace("prepareToolchain", PrepareToolchain.class);
        tasks.replace("prepareSources", PrepareSources.class);
        tasks.replace("basePrepareSources", BasePrepareSources.class);
        tasks.replace("getTools", GetTools.class);
        tasks.replace("baseGetTools", BaseGetTools.class);
        tasks.replace("test", Test.class);
        tasks.replace("baseTest", BaseTest.class);
        tasks.replace("build", Build.class);
        tasks.replace("baseBuild", BaseBuild.class);
    }

}
