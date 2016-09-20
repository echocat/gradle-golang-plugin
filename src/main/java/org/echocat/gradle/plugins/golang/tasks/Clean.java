package org.echocat.gradle.plugins.golang.tasks;

public class Clean extends GolangTaskSupport {

    public Clean() {
        setGroup("build");
        setDescription("Deletes the build directory and dependencies if required.");
        dependsOn(
            "baseClean"
        );
    }

}

