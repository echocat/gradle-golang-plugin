package org.echocat.gradle.plugins.golang.tasks;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.Files.*;

public class Clean extends GolangTask {

    public Clean() {
        setGroup("build");
        dependsOn(
            "validate"
        );
    }

    @Override
    public void run() throws Exception {
        final Path path = getProject().getBuildDir().toPath();
        if (exists(path)) {
            final Set<Path> doNotCleanSubTreeOfThisPaths = doNotCleanSubTreeOfThisPaths();


            walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    delete(file);
                    return CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (doNotCleanSubTreeOfThisPaths.contains(dir) || isSymbolicLink(dir)) {
                        delete(dir);
                        return SKIP_SUBTREE;
                    }
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    delete(dir);
                    return CONTINUE;
                }

            });
        }
        getDependencyHandler().deleteUnknownDependenciesIfRequired();
    }

    protected Set<Path> doNotCleanSubTreeOfThisPaths() {
        final Set<Path> results = new HashSet<>();
        if (TRUE.equals(getBuild().getUseTemporaryGopath())) {
            final File packagePath = getGolang().packagePathFor(getBuild().getGopath());
            final Path packageVendorPath = new File(packagePath, "vendor").toPath();
            results.add(packageVendorPath);
        }
        return results;
    }

}

