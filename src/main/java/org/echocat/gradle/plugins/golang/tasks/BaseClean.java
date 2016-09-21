package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.DependencyHandler;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.Files.*;

public class BaseClean extends GolangTaskSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseClean.class);

    public BaseClean() {
        setGroup("build");
        setDescription("Deletes the build directory and dependencies if required of base artifacts.");
        dependsOn(
            "validate"
        );
    }

    @Override
    public void run() throws Exception {
        deleteBuildDirIfRequired();
        final DependencyHandler dependencyHandler = getDependencyHandler();
        dependencyHandler.deleteUnknownDependenciesIfRequired();
        dependencyHandler.deleteAllCachedDependenciesIfRequired();
    }

    protected void deleteBuildDirIfRequired() throws IOException {
        final ProgressLogger progress = startProgress("Clean");
        final Path path = getProject().getBuildDir().toPath();
        if (exists(path)) {
            progress.progress("Delete all contents of " + path + "...");
            final Set<Path> doNotCleanSubTreeOfThisPaths = doNotCleanSubTreeOfThisPaths();

            walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        deleteIfExists(file);
                    } catch (final IOException e) {
                        LOGGER.warn("Could not delete file {}.", file, e);
                    }
                    return CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (doNotCleanSubTreeOfThisPaths.contains(dir) || isSymbolicLink(dir)) {
                        try {
                            deleteIfExists(dir);
                        } catch (final IOException e) {
                            LOGGER.warn("Could not delete dir {}.", dir, e);
                        }
                        return SKIP_SUBTREE;
                    }
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    try {
                        deleteIfExists(dir);
                    } catch (final IOException e) {
                        LOGGER.warn("Could not delete dir {}.", dir, e);
                    }
                    return CONTINUE;
                }

            });
        }
        progress.completed();
    }

    protected Set<Path> doNotCleanSubTreeOfThisPaths() {
        final Set<Path> results = new HashSet<>();
        if (TRUE.equals(getBuild().getUseTemporaryGopath())) {
            final Path packagePath = getGolang().packagePathFor(getBuild().getGopath());
            final Path packageVendorPath = packagePath.resolve("vendor");
            results.add(packageVendorPath);
        }
        return results;
    }

}

