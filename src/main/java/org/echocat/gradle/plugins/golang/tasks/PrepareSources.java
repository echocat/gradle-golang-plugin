package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.apache.commons.io.FileUtils.forceMkdir;

public class PrepareSources extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareSources.class);

    private final String[] _sourcesIncludes = null;
    private final String[] _sourcesExcludes = {
        ".git/**", ".svn/**", "build.gradle", "build/**", ".gradle/**", "gradle/**"
    };

    public PrepareSources() {
        dependsOn("validate");
    }

    @Override
    public void run()  throws  Exception {
        final GolangSettings settings = settings();
        final BuildSettings build = build();
        if (build.isUseTemporaryGopath()) {
            LOGGER.debug("Prepare GOPATH ({})...", build.getGopath());
            final File projectBasedir = settings.projectBasedir();
            final File packagePath = settings.packagePath();

            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(projectBasedir);
            scanner.setIncludes(_sourcesIncludes);
            scanner.setExcludes(_sourcesExcludes);
            scanner.scan();
            for (final String file : scanner.getIncludedFiles()) {
                final File sourceFile = new File(projectBasedir, file);
                final File targetFile = new File(packagePath, file);
                if (!targetFile.exists()
                    || sourceFile.length() != targetFile.length()
                    || sourceFile.lastModified() != targetFile.lastModified()) {
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
        }
    }

}
