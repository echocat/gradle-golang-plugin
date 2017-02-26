package org.echocat.gradle.plugins.golang.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.*;

public class FileUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    @Nullable
    public static Path toPath(@Nullable String plain) {
        return plain != null ? Paths.get(plain) : null;
    }

    public static void ensureParentOf(@Nonnull Path path) throws IOException {
        createDirectoriesIfRequired(path.getParent());
    }

    public static void createDirectoriesIfRequired(@Nonnull Path path) throws IOException {
        if (!exists(path)) {
            createDirectories(path);
        }
    }

    public static void delete(@Nullable Path path) throws IOException {
        if (path == null || !exists(path)) {
            return;
        }
        if (isRegularFile(path)) {
            doDelete(path);
            return;
        }
        walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                doDelete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                doDelete(dir);
                return CONTINUE;
            }
        });
    }

    public static void deleteQuietly(@Nullable Path path) {
        if (path == null || !exists(path)) {
            return;
        }
        if (isRegularFile(path)) {
            try {
                doDelete(path);
            } catch (final IOException ignored) {}
            return;
        }
        try {
            walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        doDelete(file);
                    } catch (final IOException ignored) {}
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    try {
                        doDelete(dir);
                    } catch (final IOException ignored) {}
                    return CONTINUE;
                }
            });
        } catch (final IOException ignored) {}
    }

    private static void doDelete(@Nonnull Path path) throws IOException {
        final FileStore fileStore = Files.getFileStore(path);
        if (fileStore.supportsFileAttributeView("dos")) {
            setAttribute(path, "dos:readonly", false);
            setAttribute(path, "dos:system", false);
            setAttribute(path, "dos:hidden", false);
        }
        Files.delete(path);

    }

    public static Set<Path> deleteWithLogging(@Nullable Path path) {
        final Set<Path> result = new TreeSet<>();
        if (path == null || !exists(path)) {
            return result;
        }
        if (isRegularFile(path)) {
            try {
                doDelete(path);
            } catch (final IOException e) {
                LOG.warn("Could not delete '{}' Got: {} - {}", path, e.getClass().getName(), e.getMessage());
            }
            result.add(path);
            return result;
        }
        try {
            walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    result.add(file);
                    try {
                        doDelete(file);
                    } catch (final IOException e) {
                        LOG.warn("Could not delete '{}' Got: {} - {}", file, e.getClass().getName(), e.getMessage());
                    }
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    result.add(dir);
                    try {
                        doDelete(dir);
                    } catch (final IOException e) {
                        LOG.warn("Could not delete '{}' Got: {} - {}", dir, e.getClass().getName(), e.getMessage());
                    }
                    return CONTINUE;
                }
            });
        } catch (final IOException ignored) {}
        return result;
    }

}
