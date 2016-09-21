package org.echocat.gradle.plugins.golang;

import org.apache.commons.lang3.StringUtils;
import org.echocat.gradle.plugins.golang.model.DependenciesSettings;
import org.echocat.gradle.plugins.golang.model.GolangDependency;
import org.echocat.gradle.plugins.golang.model.Settings;
import org.echocat.gradle.plugins.golang.utils.FileUtils;
import org.echocat.gradle.plugins.golang.vcs.*;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static java.io.File.separatorChar;
import static java.lang.Boolean.TRUE;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.echocat.gradle.plugins.golang.DependencyHandler.DependencyDirType.*;
import static org.echocat.gradle.plugins.golang.DependencyHandler.GetResult.alreadyExists;
import static org.echocat.gradle.plugins.golang.DependencyHandler.GetResult.downloaded;
import static org.echocat.gradle.plugins.golang.model.GolangDependency.Type.*;
import static org.echocat.gradle.plugins.golang.model.GolangDependency.newDependency;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;
import static org.echocat.gradle.plugins.golang.vcs.VcsRepository.Utils.progressMonitorFor;

public class DependencyHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyHandler.class);

    protected static final Pattern IS_EXTERNAL_DEPENDENCY_PATTERN = Pattern.compile("^([a-zA-Z0-9\\-]+\\.[a-zA-Z0-9\\-.]+/[a-zA-Z0-9\\-_.$]+[^ ]*)");
    protected static final Filter<Path> GO_FILENAME_FILTER = new Filter<Path>() {
        @Override
        public boolean accept(Path path) {
            return path.getFileName().toString().endsWith(".go");
        }
    };

    @Nonnull
    private final VcsRepositoryProvider _vcsRepositoryProvider = new CombinedVcsRepositoryProvider();

    @Nonnull
    private final ProgressLoggerFactory _progressLoggerFactory;
    @Nonnull
    private final Settings _settings;

    public DependencyHandler(@Nonnull ServiceRegistry serviceRegistry, @Nonnull Settings settings) {
        this(serviceRegistry.get(ProgressLoggerFactory.class), settings);
    }

    public DependencyHandler(@Nonnull ProgressLoggerFactory progressLoggerFactory, @Nonnull Settings settings) {
        _progressLoggerFactory = progressLoggerFactory;
        _settings = settings;
    }

    @Nonnull
    public Map<GolangDependency, GetResult> get(@Nonnull GetTask task) throws Exception {
        final ProgressLogger progressLogger = _progressLoggerFactory.newOperation(DependencyHandler.class);

        final DependenciesSettings dependencies = _settings.getDependencies();
        final Map<GolangDependency, GetResult> result = new TreeMap<>();
        final Set<String> handledReferenceIds = new LinkedHashSet<>();
        final Queue<GolangDependency> toHandle = new LinkedList<>();
        toHandle.addAll(task.getAdditionalRequiredPackages());
        toHandle.addAll(dependencies(task));

        progressLogger.setDescription("Checking " + task.getConfiguration() + " dependencies...");
        progressLogger.started();
        GolangDependency dependency;
        while ((dependency = toHandle.poll()) != null) {
            if (!result.containsKey(dependency)) {
                final RawVcsReference reference = dependency.toRawVcsReference();
                final VcsRepository repository = _vcsRepositoryProvider.tryProvideFor(reference);
                if (repository == null) {
                    throw new RuntimeException("Could not download dependency: " + reference);
                }
                final String normalizedReferenceId = repository.getReference().getId();
                if (!handledReferenceIds.contains(normalizedReferenceId)) {
                    if (dependency.getType() != source) {
                        LOGGER.info("Update dependency {} (if required)...", normalizedReferenceId);
                        progressLogger.progress("Update dependency " + normalizedReferenceId + " (if required)...");
                        if (TRUE.equals(dependencies.getForceUpdate())) {
                            repository.forceUpdate(selectTargetDirectoryFor(task),
                                progressMonitorFor("Updating dependency " + normalizedReferenceId + "... {0,number,0.0%}", progressLogger)
                            );
                            //noinspection UseOfSystemOutOrSystemErr
                            System.out.println("Dependency " + normalizedReferenceId + " updated.");
                            progressLogger.progress("Dependency " + normalizedReferenceId + " updated.");
                        } else {
                            final VcsFullReference fullReference = repository.updateIfRequired(selectTargetDirectoryFor(task),
                                progressMonitorFor("Updating dependency " + normalizedReferenceId + "... {0,number,0.0%}", progressLogger)
                            );
                            if (fullReference != null) {
                                //noinspection UseOfSystemOutOrSystemErr
                                System.out.println("Dependency " + normalizedReferenceId + " updated.");
                                progressLogger.progress("Dependency " + normalizedReferenceId + " updated.");
                                result.put(dependency, downloaded);
                            } else {
                                LOGGER.debug("No update for {} required.", normalizedReferenceId);
                                progressLogger.progress("No update for " + normalizedReferenceId + " required.");
                                result.put(dependency, alreadyExists);
                            }
                        }
                    } else {
                        result.put(dependency, alreadyExists);
                    }
                    handledReferenceIds.add(normalizedReferenceId);
                } else {
                    result.put(dependency, alreadyExists);
                }
                LOGGER.debug("Resolve child dependencies of dependency {}...", normalizedReferenceId);
                progressLogger.progress("Resolve child dependencies of dependency " + normalizedReferenceId + "...");
                for (final GolangDependency nextCandidate : resolveDependenciesOf(dependency)) {
                    if (!result.containsKey(nextCandidate) && !toHandle.contains(nextCandidate)) {
                        toHandle.add(nextCandidate);
                    }
                }
            }
        }
        int numberOfDownloadedDependencies = 0;
        for (final GetResult getResult : result.values()) {
            if (getResult == downloaded) {
                numberOfDownloadedDependencies++;
            }
        }
        if (numberOfDownloadedDependencies > 0) {
            LOGGER.info("{} dependencies updated.", numberOfDownloadedDependencies);
            progressLogger.completed(numberOfDownloadedDependencies + " dependencies updated.");
        } else {
            progressLogger.completed();
        }

        if (LOGGER.isDebugEnabled() && !handledReferenceIds.isEmpty()) {
            final StringBuilder sb = new StringBuilder(capitalize(task.getConfiguration()) + " dependencies:");
            for (final String id : handledReferenceIds) {
                sb.append("\n\t* ").append(id);
            }
            LOGGER.debug(sb.toString());
        }
        return result;
    }

    protected boolean isPartOfProjectSources(@Nonnull String packageName) throws Exception {
        final String projectPackageName = _settings.getGolang().getPackageName();
        return packageName.equals(projectPackageName) || packageName.startsWith(projectPackageName + "/");
    }

    @Nonnull
    protected Path selectTargetDirectoryFor(@Nonnull GetTask task) throws Exception {
        return selectTargetDirectoryFor(task.getConfiguration());
    }

    @Nonnull
    protected Path selectTargetDirectoryFor(@Nullable String configuration) throws Exception {
        if ("tool".equals(configuration)) {
            return _settings.getBuild().getGopathSourceRoot();
        }
        return _settings.getDependencies().getDependencyCache();
    }

    @Nonnull
    protected Set<GolangDependency> resolveDependenciesOf(@Nonnull GolangDependency dependency) throws Exception {
        final Set<GolangDependency> result = new TreeSet<>();
        for (final Path file : filesFor(dependency)) {
            final String plainPackages = executor(_settings.getToolchain().toolchainBinary("importsExtractor"))
                .arguments(file)
                .execute()
                .getStdoutAsString();
            for (final String plainPackage : StringUtils.split(plainPackages, '\n')) {
                final String trimmed = plainPackage.trim();
                if (!trimmed.isEmpty() && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                    final String candidate = trimmed.substring(1, trimmed.length() - 1);
                    if (IS_EXTERNAL_DEPENDENCY_PATTERN.matcher(candidate).matches()) {
                        final GolangDependency childDependency = resolvePackage(dependency, candidate);
                        result.add(childDependency);
                    }
                }
            }
        }
        return result;
    }

    @Nonnull
    protected GolangDependency resolvePackage(@Nonnull GolangDependency demandedBy, @Nonnull String packageName) throws Exception {
        GolangDependency candidate = null;
        if (candidate == null) {
            candidate = resolveVendorPackage(demandedBy, packageName);
        }
        if (candidate == null) {
            candidate = resolveDependenciesPackage(packageName);
        }
        if (candidate == null) {
            candidate = resolveGopathPackage(packageName);
        }
        if (candidate == null) {
            candidate = resolveGorootPackage(packageName);
        }
        if (candidate == null) {
            candidate = newDependency(packageName)
                .setType(implicit);
        }
        return candidate;
    }

    @Nullable
    protected GolangDependency resolveVendorPackage(@Nonnull GolangDependency demandedBy, @Nonnull String packageName) throws Exception {
        final Path root = _settings.getBuild().getGopathSourceRoot().toAbsolutePath();
        final Path demandedByLocation = demandedBy.getLocation();
        if (demandedByLocation != null) {
            Path current = demandedByLocation.toAbsolutePath();
            while (current.startsWith(root)) {
                final Path vendorCandidate = current.resolve("vendor");
                if (isDirectory(vendorCandidate)) {
                    final Path packagePathCandidate = vendorCandidate.resolve(packageName);
                    if (containsGoSources(packagePathCandidate)) {
                        return newDependency(packageName)
                            .setType(implicit)
                            .setParent(demandedBy)
                            .setLocation(packagePathCandidate);
                    }
                }
                current = current.getParent();
            }
        }
        return null;
    }

    @Nullable
    protected GolangDependency resolveDependenciesPackage(@Nonnull String packageName) throws Exception {
        final Path location = _settings.getDependencies().getDependencyCache().resolve(packageName);
        if (!containsGoSources(location)) {
            return null;
        }
        return newDependency(packageName)
            .setType(implicit)
            .setLocation(location);
    }

    @Nullable
    protected GolangDependency resolveGopathPackage(@Nonnull String packageName) throws Exception {
        final Path location = _settings.getBuild().getGopathSourceRoot().resolve(packageName);
        if (!containsGoSources(location)) {
            return null;
        }
        return newDependency(packageName)
            .setType(isPartOfProjectSources(packageName) ? source : implicit)
            .setLocation(location);
    }

    @Nullable
    protected GolangDependency resolveGorootPackage(@Nonnull String packageName) throws Exception {
        final Path location = _settings.getToolchain().getGorootSourceRoot().resolve(packageName);
        if (!containsGoSources(location)) {
            return null;
        }
        return newDependency(packageName)
            .setType(system)
            .setLocation(location);
    }

    protected boolean containsGoSources(@Nonnull Path candidate) throws Exception {
        return isDirectory(candidate) && newDirectoryStream(candidate, GO_FILENAME_FILTER).iterator().hasNext();
    }

    @Nonnull
    protected Set<Path> filesFor(@Nonnull GolangDependency dependency) throws Exception {
        final Set<Path> result = new TreeSet<>();
        //final GolangDependency parent = dependency.getParent();

        appendFilesFor(_settings.getDependencies().getDependencyCache(), dependency, result);
        appendFilesFor(_settings.getBuild().getGopathSourceRoot(), dependency, result);
        return result;
    }

    protected void appendFilesFor(@Nonnull Path root, @Nonnull GolangDependency dependency, @Nonnull Set<Path> to) throws Exception {
        final Path directory = root.resolve(dependency.getGroup());
        if (isDirectory(directory)) {
            for (final Path path : newDirectoryStream(directory, GO_FILENAME_FILTER)) {
                to.add(path);
            }
        }
    }

    @Nonnull
    public Collection<Path> deleteUnknownDependenciesIfRequired() throws Exception {
        final ProgressLogger progress = _progressLoggerFactory.newOperation(DependencyHandler.class);
        progress.setDescription("Delete unknown dependencies");
        progress.started("Delete unknown dependencies if required...");
        final DependenciesSettings dependencies = _settings.getDependencies();
        final Path dependencyCacheDirectory = dependencies.getDependencyCache();
        final Set<String> knownDependencyIds = new HashSet<>();
        for (final GolangDependency dependency : allProjectDependencies()) {
            knownDependencyIds.add(dependency.getGroup());
        }
        final Collection<Path> result = doDeleteUnknownDependenciesIfRequired(dependencyCacheDirectory, knownDependencyIds);
        progress.completed();
        return result;
    }

    @Nonnull
    public Collection<Path> deleteAllCachedDependenciesIfRequired() throws Exception {
        final ProgressLogger progress = _progressLoggerFactory.newOperation(DependencyHandler.class);
        progress.setDescription("Delete all cached dependencies");
        progress.started("Delete all cached dependencies if required...");
        final DependenciesSettings dependencies = _settings.getDependencies();
        final Path dependencyCacheDirectory = dependencies.getDependencyCache();
        final Collection<Path> result = doDeleteAllCachedDependenciesIfRequired(dependencyCacheDirectory);
        progress.completed();
        return result;
    }

    @Nonnull
    protected Collection<GolangDependency> dependencies(@Nonnull GetTask task) {
        final Set<GolangDependency> result = new LinkedHashSet<>();
        appendDependenciesOf(task.getConfiguration(), result);
        for (final String additionalConfiguration : task.getAdditionalConfigurations()) {
            appendDependenciesOf(additionalConfiguration, result);
        }
        return result;
    }

    protected void appendDependenciesOf(@Nonnull String configurationName, @Nonnull Collection<GolangDependency> to) {
        final Project project = _settings.getProject();
        final ConfigurationContainer configurations = project.getConfigurations();
        final Configuration configuration = configurations.getByName(configurationName);
        appendDependenciesOf(configuration, to);
    }

    @Nonnull
    protected Collection<GolangDependency> allProjectDependencies() {
        final Collection<GolangDependency> result = new LinkedHashSet<>();
        final Project project = _settings.getProject();
        for (final Configuration configuration : project.getConfigurations()) {
            appendDependenciesOf(configuration, result);
        }
        return result;
    }

    protected void appendDependenciesOf(@Nonnull Configuration configuration, @Nonnull Collection<GolangDependency> to) {
        for (final Dependency dependency : configuration.getDependencies()) {
            final GolangDependency toAdd;
            if (dependency instanceof GolangDependency) {
                toAdd = (GolangDependency) dependency;
            } else {
                toAdd = new GolangDependency(dependency);
            }
            if (!to.contains(toAdd)) {
                to.add(toAdd);
            }
        }
    }

    @Nonnull
    protected Collection<Path> doDeleteUnknownDependenciesIfRequired(@Nonnull Path root, @Nonnull Set<String> knownDependencyIds) throws IOException {
        if (!TRUE.equals(_settings.getDependencies().getDeleteUnknownDependencies())) {
            return emptyList();
        }
        return doDeleteUnknownDependencies(root, knownDependencyIds);
    }

    @Nonnull
    protected Collection<Path> doDeleteAllCachedDependenciesIfRequired(@Nonnull Path root) throws IOException {
        if (!TRUE.equals(_settings.getDependencies().getDeleteAllCachedDependenciesOnClean())) {
            return emptyList();
        }
        LOGGER.debug("Deleting cached dependency in {}...", root);
        final Set<Path> deleted = FileUtils.deleteWithLogging(root);
        LOGGER.info("Unknown cached in {} deleted.", root);
        return deleted;
    }

    @Nonnull
    protected Collection<Path> doDeleteUnknownDependencies(@Nonnull Path root, @Nonnull Set<String> knownDependencyIds) throws IOException {
        final Collection<Path> paths = collectUnknownDependencyDirectories(root, knownDependencyIds);
        for (final Path path : paths) {
            LOGGER.debug("Deleting unknown dependency in {}...", path);
            FileUtils.deleteWithLogging(path);
            LOGGER.info("Unknown dependency in {} deleted.", path);
        }
        return paths;
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
        if (isDirectory(root)) {
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

    public enum GetResult {
        downloaded,
        alreadyExists
    }

    public static class GetTask {

        @Nonnull
        public static GetTask by(@Nonnull String configuration) {
            return new GetTask(configuration);
        }

        @Nonnull
        private final String _configuration;
        @Nonnull
        private final Set<String> _additionalConfigurations = new LinkedHashSet<>();
        @Nonnull
        private final Set<GolangDependency> _additionalRequiredPackages = new LinkedHashSet<>();

        public GetTask(@Nonnull String configuration) {
            _configuration = configuration;
        }

        @Nonnull
        public GetTask withAdditionalConfigurations(@Nullable Collection<String> configurations) {
            if (configurations != null) {
                _additionalConfigurations.addAll(configurations);
            }
            return this;
        }

        @Nonnull
        public GetTask withAdditionalConfigurations(@Nullable String... configurations) {
            return withAdditionalConfigurations(configurations != null ? asList(configurations) : null);
        }

        @Nonnull
        public GetTask withAdditionalRequiredPackages(@Nullable Collection<GolangDependency> requiredPackages) {
            if (requiredPackages != null) {
                _additionalRequiredPackages.addAll(requiredPackages);
            }
            return this;
        }

        @Nonnull
        public GetTask withAdditionalRequiredPackages(@Nullable GolangDependency... requiredPackages) {
            return withAdditionalRequiredPackages(requiredPackages != null ? asList(requiredPackages) : null);
        }

        @Nonnull
        public String getConfiguration() {
            return _configuration;
        }

        @Nonnull
        public Set<String> getAdditionalConfigurations() {
            return unmodifiableSet(_additionalConfigurations);
        }

        @Nonnull
        public Set<GolangDependency> getAdditionalRequiredPackages() {
            return unmodifiableSet(_additionalRequiredPackages);
        }
    }

}
