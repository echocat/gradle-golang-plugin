package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.lang3.StringUtils;
import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.GolangDependency;
import org.echocat.gradle.plugins.golang.model.GolangDependency.Type;
import org.echocat.gradle.plugins.golang.model.Platform;
import org.echocat.gradle.plugins.golang.model.ToolchainSettings;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.echocat.gradle.plugins.golang.utils.Executor.ExecutionFailedExceptionProducer;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.isDirectory;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.echocat.gradle.plugins.golang.DependencyHandler.GetTask.by;
import static org.echocat.gradle.plugins.golang.model.GolangDependency.newDependency;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;

public class BuildTask extends GolangTaskSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(BuildTask.class);

    private static final ExecutionFailedExceptionProducer<RuntimeException> EXCEPTION_PRODUCER = new ExecutionFailedExceptionProducer<RuntimeException>() {
        @Nonnull
        @Override
        public RuntimeException produceFor(@Nonnull Executor executor, @Nonnull String[] commandLine, int errorCode) {
            return new RuntimeException("Build failed (see log).");
        }
    };

    public BuildTask() {
        setGroup("support");
        dependsOn(
            "validate",
            "prepareToolchain",
            "prepareSources"
        );
    }

    @Override
    public void run() throws Exception {
        final String packageName = getGolang().getPackageName();
        final GolangDependency targetPackage = newDependency(packageName)
            .setType(Type.source)
            .setLocation(selectPackageLocation(packageName));

        getDependencyHandler().get(by("build")
            .withAdditionalRequiredPackages(targetPackage)
        );

        final ProgressLogger progress = startProgress("Build");

        final List<Platform> platforms = getGolang().getPlatforms();
        if (platforms == null || platforms.isEmpty()) {
            throw new IllegalArgumentException("There are no platforms specified.");
        }
        for (final Platform platform : platforms) {
            executeFor(platform, targetPackage, progress);
        }

        progress.completed();
    }

    protected Path selectPackageLocation(@Nonnull String packageName) {
        final BuildSettings build = getBuild();
        for (final Path gopathSourceRoot : build.getGopathSourceRoot()) {
            final Path candidate = gopathSourceRoot.resolve(packageName);
            if (isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Project '" + packageName + "' is not part of GOPATH (" + build.getGopath() + ").");
    }

    protected void executeFor(@Nonnull Platform platform, @Nonnull GolangDependency targetPackage, @Nonnull ProgressLogger progress) throws Exception {
        final ToolchainSettings toolchain = getToolchain();
        final BuildSettings build = getBuild();

        final Path outputFilename = build.outputFilenameFor(platform);
        progress.progress("Building " + outputFilename + "...");
        LOGGER.info("Building {}...", outputFilename);

        final Executor executor = executor(toolchain.getGoBinary())
            .workingDirectory(build.getFirstGopath())
            .env("GOPATH", build.getGopath())
            .env("GOROOT", toolchain.getGoroot())
            .env("GOOS", platform.getOperatingSystem().getNameInGo())
            .env("GOARCH", platform.getArchitecture().getNameInGo())
            .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0");

        executor.arguments("build");
        executor.arguments("-o", outputFilename);
        executor.arguments(build.getResolvedArguments());
        executor.argument(targetPackage.getGroup());

        executor.execute(EXCEPTION_PRODUCER);

        final String stdoutAsString = executor.getStdoutAsString();
        if (isNotEmpty(stdoutAsString)) {
            for (final String line : StringUtils.split(stdoutAsString, "\n")) {
                LOGGER.info(line);
            }
        }
        //noinspection UseOfSystemOutOrSystemErr
        System.out.println(outputFilename + " build.");
    }

}

