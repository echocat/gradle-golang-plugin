package org.echocat.gradle.plugins.golang.tasks;

public class BaseTest extends TestTask {

    public BaseTest() {
        setGroup("verification");
        setDescription("Runs the tests of base artifacts.");
        dependsOn(
            "validate",
            "prepareToolchain",
            "prepareSources",
            "getTools"
        );
    }
}

