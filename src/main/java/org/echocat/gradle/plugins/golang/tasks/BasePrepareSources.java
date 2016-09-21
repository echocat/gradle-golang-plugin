package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.*;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.WINDOWS;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.currentOperatingSystem;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.ensureParentOf;

public class BasePrepareSources extends GolangTaskSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasePrepareSources.class);

    public BasePrepareSources() {
        setGroup("build");
        setDescription("Process sources and place it at right locations if required of base artifacts.");
        dependsOn(
            "validate"
        );
    }

    @Override
    public void run()  throws  Exception {
        final ProgressLogger progress = startProgress("Prepare sources");

        final GolangSettings settings = getGolang();
        final BuildSettings build = getBuild();
        boolean atLeastOneCopied = false;
        if (TRUE.equals(build.getUseTemporaryGopath())) {
            progress.progress("Prepare GOPATH " + build.getGopath() + "...");
            LOGGER.info("Prepare GOPATH ({})...", build.getGopath());
            final Path projectBasedir = settings.getProjectBasedir();
            final Path packagePath = settings.packagePathFor(build.getGopath());
            final Path dependencyCachePath = getDependencies().getDependencyCache();
            prepareDependencyCacheIfNeeded(packagePath, dependencyCachePath);

            final DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(projectBasedir.toFile());
            scanner.setIncludes(build.getIncludes());
            scanner.setExcludes(build.getExcludes());
            scanner.setCaseSensitive(true);
            scanner.scan();
            for (final String file : scanner.getIncludedFiles()) {
                final Path sourceFile = projectBasedir.resolve(file);
                final Path targetFile = packagePath.resolve(file);
                if (!sourceFile.equals(dependencyCachePath)) {
                    if (!exists(targetFile)
                        || size(sourceFile) != size(targetFile)
                        || !getLastModifiedTime(sourceFile).equals(getLastModifiedTime(targetFile))) {
                        atLeastOneCopied = true;
                        progress.progress("Prepare source file: " + file + "...");
                        LOGGER.debug("* {}", file);
                        ensureParentOf(targetFile);
                        try (final InputStream is = newInputStream(sourceFile)) {
                            try (final OutputStream os = newOutputStream(targetFile)) {
                                IOUtils.copy(is, os);
                            }
                        }
                        setLastModifiedTime(targetFile, getLastModifiedTime(sourceFile));
                    }
                }
            }
            if (!atLeastOneCopied) {
                getState().upToDate();
            }
        } else {
            getState().skipped("SKIPPED");
        }

        progress.completed();
    }

    protected void prepareDependencyCacheIfNeeded(Path packagePath, Path dependencyCachePath) throws IOException {
        final Path packageVendorPath = packagePath.resolve("vendor");

        if (exists(packageVendorPath)) {
            if (!isDirectory(packageVendorPath) && !isSymbolicLink(packageVendorPath)) {
                throw new IllegalStateException(packageVendorPath + " already exists but is not a symbolic link.");
            }
        } else {
            createDirectories(packageVendorPath.getParent());
            doCreateSymbolicLink(packageVendorPath, dependencyCachePath);
        }
    }

    protected void doCreateSymbolicLink(Path link, Path target) throws IOException {
        if (currentOperatingSystem() != WINDOWS) {
            createSymbolicLink(link, target);
        } else {
            executor(System.getenv("ComSpec"))
                .arguments("/C", "mklink", "/D", "/J", link, target)
                .execute();
        }
    }

}
