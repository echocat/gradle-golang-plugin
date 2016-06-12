package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.FileUtils;
import org.echocat.gradle.plugins.golang.model.DependenciesSettings;
import org.echocat.gradle.plugins.golang.model.GolangDependency;
import org.echocat.gradle.plugins.golang.model.GolangSettings;
import org.echocat.gradle.plugins.golang.vcs.*;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;

import static java.io.File.separatorChar;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walkFileTree;
import static org.echocat.gradle.plugins.golang.tasks.Get.DependencyDirType.*;

public class Get extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Get.class);

    public Get() {
        dependsOn("validate", "prepare-toolchain");
    }

    @Nonnull
    private final VcsRepositoryProvider _vcsRepositoryProvider = new CombinedVcsRepositoryProvider();

    @Override
    public void run() throws Exception {
        final GolangSettings settings = settings();
        final DependenciesSettings dependencies = settings.dependencies();

        final File dependencyCacheDirectory = dependencies.getDependencyCache();
        final Set<String> knownDependencyIds = new HashSet<>();

        boolean atLeastOneUpdated = false;
        for (final GolangDependency dependency : getDependencies()) {
            knownDependencyIds.add(dependency.getGroup());
            final RawVcsReference reference = dependency.toRawVcsReference();
            final VcsRepository repository = _vcsRepositoryProvider.tryProvideFor(reference);
            if (repository == null) {
                throw new RuntimeException("Could not download dependency: " + reference);
            }
            LOGGER.debug("Update dependency {} (if required)...", reference);
            if (dependencies.isForceUpdate()) {
                repository.forceUpdate(dependencyCacheDirectory);
                LOGGER.info("Dependency {} updated.", reference);
            } else {
                final VcsFullReference fullReference = repository.updateIfRequired(dependencyCacheDirectory);
                if (fullReference != null) {
                    LOGGER.info("Dependency {} updated.", reference);
                    atLeastOneUpdated = true;
                } else {
                    LOGGER.debug("No update required for dependency {}.", reference);
                }
            }
        }
        if (!atLeastOneUpdated && !doDeleteUnknownDependenciesIfRequired(dependencyCacheDirectory.toPath(), knownDependencyIds)) {
            getState().upToDate();
        }
    }

    @Nonnull
    protected Iterable<GolangDependency> getDependencies() {
        final List<GolangDependency> result = new ArrayList<>();
        final ConfigurationContainer configurations = getProject().getConfigurations();
        for (final Configuration configuration : configurations) {
            for (final Dependency dependency : configuration.getDependencies()) {
                if (dependency instanceof GolangDependency) {
                    result.add((GolangDependency) dependency);
                } else {
                    result.add(new GolangDependency(dependency));
                }
            }
        }
        return result;
    }

    protected boolean doDeleteUnknownDependenciesIfRequired(@Nonnull Path root, @Nonnull Set<String> knownDependencyIds) throws IOException {
        if (settings().dependencies().isDeleteUnknownDependencies()) {
            return doDeleteUnknownDependencies(root, knownDependencyIds);
        }
        return false;
    }

    protected boolean doDeleteUnknownDependencies(@Nonnull Path root, @Nonnull Set<String> knownDependencyIds) throws IOException {
        final Collection<Path> paths = collectUnknownDependencyDirectories(root, knownDependencyIds);
        for (final Path path : paths) {
            LOGGER.debug("Deleting unknown dependency in {}...", path);
            try {
                FileUtils.forceDelete(path.toFile());
            } catch (final IOException e) {
                throw new IOException("Could not remove " + path + ".", e);
            }
            LOGGER.info("Unknown dependency in {} deleted.", path);
        }
        return !paths.isEmpty();
    }

    @Nonnull
    protected Collection<Path> collectUnknownDependencyDirectories(@Nonnull Path root, @Nonnull Set<String> knownDependencyIds) {
        final Set<Path> result = new TreeSet<>(Collections.<Path>reverseOrder());
        final Map<Path, DependencyDirType> candidates = collectDirectoriesToKnownDependencyOf(root, knownDependencyIds);
        for (final Entry<Path, DependencyDirType> candidate : candidates.entrySet()) {
            if (candidate.getValue() == unknown) {
                result.add(candidate.getKey());
            }
        }
        return result;
    }

    @Nonnull
    protected Map<Path, DependencyDirType> collectDirectoriesToKnownDependencyOf(@Nonnull final Path root, @Nonnull final Set<String> knownDependencyIds) {
        final Map<Path, DependencyDirType> directories = new TreeMap<>();
        try {
            walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.equals(root)) {
                        return CONTINUE;
                    }

                    if (isKnownDependencyDirectory(dir, root, knownDependencyIds)) {
                        directories.put(dir, containsInfoFile);
                        Path parent = dir.getParent();
                        while (parent != null && !parent.equals(root)) {
                            final DependencyDirType type = directories.get(parent);
                            if (type == null || type == unknown) {
                                directories.put(parent, parentOfContainsInfoFile);
                            }
                            parent = parent.getParent();
                        }
                        return CONTINUE;
                    }

                    if (isChildOfWithInfoFile(dir, root, directories)) {
                        directories.put(dir, hasContainsInfoFileParent);
                        return CONTINUE;
                    }

                    if (!directories.containsKey(dir)) {
                        directories.put(dir, unknown);
                    }
                    return CONTINUE;
                }
            });
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return directories;
    }

    protected boolean isChildOfWithInfoFile(@Nonnull Path directory, @Nonnull Path root, @Nonnull Map<Path, DependencyDirType> directories) {
        Path parent = directory.getParent();
        while (parent != null && !parent.equals(root)) {
            if (directories.get(parent) == containsInfoFile) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    protected boolean isKnownDependencyDirectory(@Nonnull Path directory, @Nonnull Path root, @Nonnull Set<String> knownDependencyIds) throws IOException {
        final int dirCount = directory.getNameCount();
        final int rootCount = root.getNameCount();
        final Path subPath = directory.subpath(rootCount, dirCount);
        final String idToTest = subPath.toString().replace(separatorChar, '/');
        return knownDependencyIds.contains(idToTest);
    }

    protected enum DependencyDirType {
        unknown,
        containsInfoFile,
        hasContainsInfoFileParent,
        parentOfContainsInfoFile
    }

}
