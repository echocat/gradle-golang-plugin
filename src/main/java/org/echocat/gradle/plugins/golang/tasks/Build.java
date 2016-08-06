package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.lang3.StringUtils;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.model.GolangDependency.Type;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.echocat.gradle.plugins.golang.utils.Executor.ExecutionFailedExceptionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.echocat.gradle.plugins.golang.model.GolangDependency.newDependency;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;

public class Build extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(Build.class);

    private static final ExecutionFailedExceptionProducer<RuntimeException> EXCEPTION_PRODUCER = new ExecutionFailedExceptionProducer<RuntimeException>() {
        @Nonnull
        @Override
        public RuntimeException produceFor(@Nonnull Executor executor, @Nonnull String[] commandLine, int errorCode) {
            return new RuntimeException("Build failed (see log).");
        }
    };

    public Build() {
        setGroup("build");
        dependsOn(
            "validate",
            "prepare-toolchain",
            "prepare-sources",
            "test",
            "get-tools"
        );
    }

    @Override
    public void run() throws Exception {
        final BuildSettings build = getBuild();
        final String packageName = getGolang().getPackageName();
        final GolangDependency targetPackage = newDependency(packageName)
            .setType(Type.source)
            .setLocation(build.getGopathSourceRoot().resolve(packageName));
        getDependencyHandler().get("build", targetPackage
        );
        for (final Platform platform : getGolang().getParsedPlatforms()) {
            executeFor(platform, targetPackage);
        }
    }

    protected void executeFor(@Nonnull Platform platform, @Nonnull GolangDependency targetPackage) throws Exception {
        final GolangSettings settings = getGolang();
        final ToolchainSettings toolchain = getToolchain();
        final BuildSettings build = getBuild();

        final Path expectedPackagePath = settings.packagePathFor(build.getGopath());
        final Path projectBasedir = settings.getProjectBasedir();
        if (!expectedPackagePath.startsWith(projectBasedir)) {
            throw new IllegalStateException("Project '" + targetPackage.getGroup() + "' is not part of GOPATH (" + build.getGopath() + "). Current location: " + projectBasedir);
        }

        final Path outputFilename = build.outputFilenameFor(platform);
        LOGGER.debug("Building {}...", outputFilename);

        final Executor executor = executor(toolchain.getGoBinary())
            .workingDirectory(build.getGopath())
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
        LOGGER.info("{} build.", outputFilename);
    }

}

