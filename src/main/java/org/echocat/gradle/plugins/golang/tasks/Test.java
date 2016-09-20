package org.echocat.gradle.plugins.golang.tasks;

public class Test extends GolangTaskSupport {

    public Test() {
        setGroup("verification");
        setDescription("Runs the tests.");
        dependsOn(
            "baseTest"
        );
    }

}

