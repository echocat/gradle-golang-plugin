package org.echocat.gradle.plugins.golang.tasks;

import static org.apache.commons.io.FileUtils.forceDelete;

public class Clean extends GolangTask {

    @Override
    public void run() throws Exception {
        forceDelete(getProject().getBuildDir());
    }

}

