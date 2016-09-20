package org.echocat.gradle.plugins.golang.tasks;

public class Validate extends GolangTaskSupport {

    public Validate() {
        setGroup("verification");
        setDescription("Validate the whole golang setup and the project and resolve missing properties (if required).");
        dependsOn(
            "baseValidate"
        );
    }

}
