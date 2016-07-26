package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.*;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.WINDOWS;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.currentOperatingSystem;

public class PrepareSources extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareSources.class);

    public PrepareSources() {
        setGroup("build");
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
            final File dependencyCachePath = getDependencies().getDependencyCache();
            prepareDepencencyCacheIFNeeded(packagePath, dependencyCachePath);

            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(projectBasedir);
            scanner.setIncludes(build.getIncludes());
            scanner.setExcludes(build.getExcludes());
            scanner.setCaseSensitive(true);
            scanner.scan();
            for (final String file : scanner.getIncludedFiles()) {
                final File sourceFile = new File(projectBasedir, file);
                final File targetFile = new File(packagePath, file);
                if (!sourceFile.equals(dependencyCachePath)) {
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
            }
            if (!atLeastOneCopied) {
                getState().upToDate();
            }
        } else {
            getState().skipped("SKIPPED");
        }
    }

    protected void prepareDepencencyCacheIFNeeded(File packagePath, File dependencyCachePath) throws IOException {
        final Path packageVendorPath = new File(packagePath, "vendor").toPath();

        if (exists(packageVendorPath)) {
            if (!isDirectory(packageVendorPath) && !isSymbolicLink(packageVendorPath)) {
                throw new IllegalStateException(packageVendorPath + " already exists but is not a symbolic link.");
            }
        } else {
            createDirectories(packageVendorPath.getParent());
            doCreateSymbolicLink(packageVendorPath, dependencyCachePath.toPath());
        }
    }

    protected void doCreateSymbolicLink(Path link, Path target) throws IOException {
        if (currentOperatingSystem() != WINDOWS) {
            createSymbolicLink(link, target);
        } else {
            final String comSpec = System.getenv("ComSpec");
            Executor.executor()
                .executable(new File(comSpec))
                .argument("/C")
                .arguments("mklink", "/D", "/J", link.toString(), target.toString())
                .execute();
        }
    }

}
