package org.echocat.gradle.plugins.golang.tasks;

public class GetTools extends GolangTaskSupport {

    public GetTools() {
        setGroup("tools");
        setDescription("Download and build required tools.");
        dependsOn(
            "baseGetTools"
        );
    }

}
