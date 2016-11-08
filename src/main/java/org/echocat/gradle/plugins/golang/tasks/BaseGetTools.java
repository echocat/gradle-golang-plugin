package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.DependencyHandler.GetResult;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.model.GolangDependency.Type;
import org.gradle.internal.logging.progress.ProgressLogger;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.exists;
import static org.echocat.gradle.plugins.golang.DependencyHandler.GetResult.downloaded;
import static org.echocat.gradle.plugins.golang.DependencyHandler.GetTask.by;
import static org.echocat.gradle.plugins.golang.model.Platform.currentPlatform;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.delete;

public class BaseGetTools extends GolangTaskSupport {

    public BaseGetTools() {
        setGroup("tools");
        setDescription("Download and build required tools for base artifacts.");
        dependsOn(
            "validate",
            "prepareToolchain"
        );
    }

    @Override
    public void run() throws Exception {
        final ProgressLogger progress = startProgress("Get tools");

        final Map<GolangDependency, GetResult> dependencies = getDependencyHandler().get(by("tool"));
        boolean atLeastOneBuild = false;
        for (final Entry<GolangDependency, GetResult> entry : dependencies.entrySet()) {
            final GolangDependency dependency = entry.getKey();
            if (dependency.getType() == Type.explicit) {
                if (buildIfRequired(dependency, entry.getValue(), progress)) {
                    atLeastOneBuild = true;
                }
            }
        }
        if (!atLeastOneBuild) {
            getState().upToDate();
        }

        progress.completed();
    }

    protected boolean buildIfRequired(@Nonnull GolangDependency dependency, @Nonnull GetResult getResult, @Nonnull ProgressLogger progress) throws Exception {
        final GolangSettings settings = getGolang();
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();
        final Platform platform = currentPlatform();
        final Path targetBinaryFilename = targetBinaryFilename(dependency);

        if (getResult == downloaded || !exists(targetBinaryFilename)) {
            getLogger().info("Get tool {}...", dependency.getGroup());
            progress.progress("Get tool " + dependency.getGroup() + "...");
            delete(targetBinaryFilename.getParent());

            executor(toolchain.getGoBinary())
                .workingDirectory(build.getFirstGopath())
                .env("GOPATH", build.getGopathAsString())
                .env("GOROOT", toolchain.getGoroot())
                .env("GOOS", platform.getOperatingSystem().getNameInGo())
                .env("GOARCH", platform.getArchitecture().getNameInGo())
                .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0")
                .arguments("build", "-o", targetBinaryFilename, dependency.getGroup())
                .execute();

            //noinspection UseOfSystemOutOrSystemErr
            System.out.println("Tool " + dependency.getGroup() + " installed.");
            return true;
        } else {
            return false;
        }
    }

    @Nonnull
    protected Path targetBinaryFilename(@Nonnull GolangDependency dependency) throws Exception {
        final String extension = getBuild().platformExtensionFor(currentPlatform());
        final String filename = dependency.getGroup() + extension;
        return targetBinaryDirectory().resolve(filename);
    }

    @Nonnull
    protected Path targetBinaryDirectory() throws Exception {
        return getProject().getBuildDir().toPath().resolve("tools").toAbsolutePath();
    }

}
