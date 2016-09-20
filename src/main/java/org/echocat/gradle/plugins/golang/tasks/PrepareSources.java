package org.echocat.gradle.plugins.golang.tasks;

public class PrepareSources extends GolangTaskSupport {

    public PrepareSources() {
        setGroup("build");
        setDescription("Process sources and place it at right locations if required.");
        dependsOn(
            "basePrepareSources"
        );
    }

}
