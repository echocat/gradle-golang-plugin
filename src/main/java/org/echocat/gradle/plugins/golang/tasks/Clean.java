package org.echocat.gradle.plugins.golang.tasks;

import static org.apache.commons.io.FileUtils.forceDelete;

public class Clean extends GolangTask {

    public Clean() {
        setGroup("build");
    }

    @Override
    public void run() throws Exception {
        forceDelete(getProject().getBuildDir());
    }

}

