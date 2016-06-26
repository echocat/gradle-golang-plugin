package org.echocat.gradle.plugins.golang.tasks;

import org.echocat.gradle.plugins.golang.DependencyHandler;
import org.echocat.gradle.plugins.golang.DependencyHandler.GetResult;
import org.echocat.gradle.plugins.golang.model.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.echocat.gradle.plugins.golang.DependencyHandler.GetResult.downloaded;
import static org.echocat.gradle.plugins.golang.model.Platform.currentPlatform;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;

public class GetTools extends GolangTask {

    public GetTools() {
        setGroup("tools");
        dependsOn("validate", "prepare-toolchain");
    }

    @Override
    public void run() throws Exception {
        final Map<GolangDependency, DependencyHandler.GetResult> dependencies = getDependencyHandler().get("tool");
        if (dependencies.isEmpty()) {
            getState().skipped("UP-TO-DATE");
            return;
        }
        boolean atLeastOneBuild = false;
        for (final Entry<GolangDependency, GetResult> entry : dependencies.entrySet()) {
            if (buildIfRequired(entry.getKey(), entry.getValue())) {
                atLeastOneBuild = true;
            }
        }
        if (!atLeastOneBuild) {
            getState().skipped("UP-TO-DATE");
        }
    }

    protected boolean buildIfRequired(@Nonnull GolangDependency dependency, @Nonnull DependencyHandler.GetResult getResult) throws Exception {
        final GolangSettings settings = getGolang();
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();
        final Platform platform = currentPlatform();
        final File targetBinaryFilename = targetBinaryFilename(dependency);

        if (getResult == downloaded || !targetBinaryFilename.exists()) {
            forceMkdir(targetBinaryFilename.getParentFile());

            executor()
                .executable(toolchain.getGoBinary())
                .workingDirectory(build.getGopath())
                .env("GOPATH", build.getGopath())
                .env("GOROOT", toolchain.getGoroot())
                .env("GOOS", platform.getOperatingSystem().getNameInGo())
                .env("GOARCH", platform.getArchitecture().getNameInGo())
                .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0")
                .arguments("build", "-o", targetBinaryFilename.getPath(), dependency.getGroup())
                .execute();
            return true;
        } else {
            return false;
        }
    }

    @Nonnull
    protected File targetBinaryFilename(@Nonnull GolangDependency dependency) throws Exception {
        final String extension = getBuild().platformExtensionFor(currentPlatform());
        final String filename = dependency.getGroup() + extension;
        return new File(targetBinaryDirectory(), filename);
    }

    @Nonnull
    protected File targetBinaryDirectory() throws Exception {
        return new File(getProject().getBuildDir(), "tools").getCanonicalFile();
    }

}
