package org.echocat.gradle.plugins.golang.tasks;

public class BaseBuild extends BuildTask {

    public BaseBuild() {
        setGroup("build");
        setDescription("Assembles and tests of base artifacts.");
        dependsOn(
            "test",
            "getTools"
        );
    }

}

