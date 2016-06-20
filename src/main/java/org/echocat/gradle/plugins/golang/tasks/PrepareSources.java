package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.io.FileUtils.forceMkdir;

public class PrepareSources extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareSources.class);

    public PrepareSources() {
        dependsOn("validate");
    }

    @Override
    public void run()  throws  Exception {
        final GolangSettings settings = getGolang();
        final BuildSettings build = getBuild();
        boolean atLeastOneCopied = false;
        if (TRUE.equals(build.getUseTemporaryGopath())) {
            LOGGER.debug("Prepare GOPATH ({})...", build.getGopath());
            final File projectBasedir = settings.getProjectBasedir();
            final File packagePath = settings.packagePathFor(build.getGopath());

            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(projectBasedir);
            scanner.setIncludes(build.getIncludes());
            scanner.setExcludes(build.getExcludes());
            scanner.setCaseSensitive(true);
            scanner.scan();
            for (final String file : scanner.getIncludedFiles()) {
                final File sourceFile = new File(projectBasedir, file);
                final File targetFile = new File(packagePath, file);
                if (!targetFile.exists()
                    || sourceFile.length() != targetFile.length()
                    || sourceFile.lastModified() != targetFile.lastModified()) {
                    atLeastOneCopied = true;
                    LOGGER.debug("* {}", file);
                    forceMkdir(targetFile.getParentFile());
                    try (final InputStream is = new FileInputStream(sourceFile)) {
                        try (final OutputStream os = new FileOutputStream(targetFile)) {
                            IOUtils.copy(is, os);
                        }
                    }
                    //noinspection ResultOfMethodCallIgnored
                    targetFile.setLastModified(sourceFile.lastModified());
                }
            }
            if (!atLeastOneCopied) {
                getState().upToDate();
            }
        } else {
            getState().skipped("SKIPPED");
        }
    }

}
