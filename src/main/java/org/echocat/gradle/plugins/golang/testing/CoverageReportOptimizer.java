package org.echocat.gradle.plugins.golang.testing;

import org.echocat.gradle.plugins.golang.model.BuildSettings;
import org.echocat.gradle.plugins.golang.model.Settings;
import org.echocat.gradle.plugins.golang.model.TestingSettings;
import org.echocat.gradle.plugins.golang.model.ToolchainSettings;
import org.echocat.gradle.plugins.golang.utils.ProjectEnabled;
import org.echocat.gradle.plugins.golang.utils.ProjectsAndSettingsEnabledSupport;
import org.echocat.gradle.plugins.golang.utils.SettingsEnabled;
import org.gradle.api.Project;
import org.gradle.internal.logging.progress.ProgressLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.exists;
import static org.echocat.gradle.plugins.golang.utils.Executor.executor;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.*;

public class CoverageReportOptimizer extends ProjectsAndSettingsEnabledSupport {

    public CoverageReportOptimizer(@Nonnull Project project, @Nonnull Settings settings) {
        super(project, settings);
    }

    public <T extends SettingsEnabled & ProjectEnabled> CoverageReportOptimizer(@Nonnull T source) {
        super(source.getProject(), source.getSettings());
    }

    @Nullable
    public Path preHandlePackagesCover() throws IOException {
        final TestingSettings testing = getTesting();
        Path coverProfile = testing.getCoverProfileFile();
        if (coverProfile != null && exists(coverProfile)) {
            delete(coverProfile);
        }
        final Path coverProfileHtml = testing.getCoverProfileHtmlFile();
        if (coverProfileHtml != null && coverProfile == null) {
            final Path testingDir = getProject().getBuildDir().toPath().resolve("testing");
            createDirectoriesIfRequired(testingDir);
            coverProfile = createTempFile(testingDir, getProject().getName() + ".", ".cover");
        }
        return coverProfile;
    }

    public void postHandlePackagesCover(@Nullable Path coverProfile, @Nonnull ProgressLogger progress) throws Exception {
        progress.progress("Post process of covering profiles...");
        final TestingSettings testing = getTesting();
        final Path coverProfileHtml = testing.getCoverProfileHtmlFile();
        if (coverProfileHtml != null && coverProfile != null && exists(coverProfile)) {
            coverToHtml(coverProfile, coverProfileHtml);
            if (testing.getCoverProfileFile() == null) {
                deleteQuietly(coverProfile);
            }
        }
    }

    protected void coverToHtml(@Nonnull Path profile, @Nonnull Path output) throws Exception {
        final BuildSettings build = getBuild();
        final ToolchainSettings toolchain = getToolchain();

        executor(toolchain.getGoBinary())
            .workingDirectory(build.getFirstGopath())
            .env("GOPATH", build.getGopathAsString())
            .env("GOROOT", toolchain.getGoroot())
            .arguments("tool", "cover")
            .arguments("-html", profile)
            .arguments("-o", output)
            .execute();
    }

}
