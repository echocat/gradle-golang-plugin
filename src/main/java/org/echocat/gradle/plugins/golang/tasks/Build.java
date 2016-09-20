package org.echocat.gradle.plugins.golang.tasks;

public class Build extends GolangTaskSupport {

    public Build() {
        setGroup("build");
        setDescription("Assembles and tests this project.");
        dependsOn(
            "baseBuild"
        );
    }

}

