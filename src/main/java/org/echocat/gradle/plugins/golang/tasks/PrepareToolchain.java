package org.echocat.gradle.plugins.golang.tasks;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.echocat.gradle.plugins.golang.Version;
import org.echocat.gradle.plugins.golang.model.*;
import org.echocat.gradle.plugins.golang.utils.ArchiveUtils;
import org.echocat.gradle.plugins.golang.utils.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static java.io.File.createTempFile;
import static java.lang.Boolean.TRUE;
import static java.net.URI.create;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;

public class PrepareToolchain extends GolangTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareToolchain.class);

    public PrepareToolchain() {
        setGroup("build");
        dependsOn("validate");
    }

    @Override
    public void run() throws Exception {
        downloadBootstrapIfRequired();
        downloadSourcesIfRequired();
        if (!buildHostIfRequired() && !buildTargetsIfRequired() && !buildToolsIfRequired()) {
            getState().upToDate();
        }
    }

    protected boolean buildHostIfRequired() throws Exception {
        final ToolchainSettings toolchain = getToolchain();
        final String expectedVersion = toolchain.getGoversion();
        String version = goBinaryVersion();
        boolean build = false;
        if (version == null) {
            build(Platform.currentPlatform(), true);
            version = goBinaryVersion();
            build = true;
        }
        if (!Objects.equals(version, expectedVersion)) {
            throw new IllegalStateException("go SDK in " + toolchain.getGoroot() + " has expected of version " + expectedVersion + " but it is " + version + ".");
        }
        return build;
    }

    protected boolean buildTargetsIfRequired() throws Exception {
        final GolangSettings settings = getGolang();
        final ToolchainSettings toolchain = getToolchain();
        final List<Platform> platforms = settings.getParsedPlatforms();
        if (platforms.isEmpty()) {
            throw new IllegalArgumentException("There are no platforms specified.");
        }
        boolean atLeastOneBuild = false;
        for (final Platform platform : platforms) {
            if (build(platform, TRUE.equals(toolchain.getForceBuildToolchain()))) {
                atLeastOneBuild = true;
            }
        }
        return atLeastOneBuild;
    }

    protected boolean buildToolsIfRequired() throws Exception {
        return buildToolIfRequired("importsExtractor");
    }

    protected boolean buildToolIfRequired(String name) throws Exception {
        final ToolchainSettings toolchain = getToolchain();
        final File goBinary = toolchain.getGoBinary();
        final File binDirectory = goBinary.getParentFile();
        final File toolBinary = new File(binDirectory, name + toolchain.getExecutableSuffix());
        final File toolBinaryInfo = new File(binDirectory, name + ".info");
        final String info = name + ":" + Version.GROUP + ":" + Version.VERSION;
        if (toolBinary.exists() && toolBinaryInfo.exists()) {
            final String foundInfo = readFileToString(toolBinaryInfo);
            if (foundInfo.equals(info)) {
                return false;
            }
        }

        final File sourceTempFile = createTempFile(name, ".go");
        try {
            final InputStream is = getClass().getClassLoader().getResourceAsStream("org/echocat/gradle/plugins/golang/utils/" + name + ".go");
            if (is == null) {
                throw new FileNotFoundException("Could not find source code for tool " + name + " in classpath.");
            }
            try {
                try (final OutputStream os = new FileOutputStream(sourceTempFile)) {
                    IOUtils.copy(is, os);
                }
            } finally {
                //noinspection ThrowFromFinallyBlock
                is.close();
            }

            forceMkdir(binDirectory);

            Executor.executor()
                .executable(goBinary)
                .arguments("build", "-o", toolBinary.getPath(), sourceTempFile.getPath())
                .removeEnv("GOPATH")
                .env("GOROOT", toolchain.getGoroot())
                .env("GOROOT_BOOTSTRAP", toolchain.getBootstrapGoroot())
                .execute();
        } finally {
            //noinspection ResultOfMethodCallIgnored
            sourceTempFile.delete();
        }

        writeStringToFile(toolBinaryInfo, info);
        return true;
    }

    protected boolean build(Platform platform, boolean force) throws Exception {
        final ToolchainSettings toolchain = getToolchain();
        final String goos = platform.getOperatingSystem().getNameInGo();
        final String goarch = platform.getArchitecture().getNameInGo();
        final File buildMarker = new File(toolchain.getGoroot(), "pkg" + File.separator + goos + "_" + goarch + File.separator + ".builded");
        if (force || !buildMarker.exists()) {
            final File sourceDirectory = new File(toolchain.getGoroot(), "src");
            final File makeScript = new File(sourceDirectory, "make." + (OperatingSystem.currentOperatingSystem() == OperatingSystem.WINDOWS ? "bat" : "bash"));

            LOGGER.info("Going to build go toolchain for {}...", platform);

            Executor.executor()
                .executable(makeScript)
                .arguments("--no-clean")
                .workingDirectory(sourceDirectory)
                .removeEnv("GOPATH")
                .env("GOROOT", toolchain.getGoroot())
                .env("GOROOT_BOOTSTRAP", toolchain.getBootstrapGoroot())
                .env("GOOS", goos)
                .env("GOARCH", goarch)
                .env("CGO_ENABLED", TRUE.equals(toolchain.getCgoEnabled()) ? "1" : "0")
                .failKeywords("ERROR: ", "($GOPATH not set)", "Access denied")
                .execute();

            writeStringToFile(buildMarker, "");
            LOGGER.info("Going to build go toolchain for {}... DONE!", platform);
            return true;
        }
        return false;
    }

    protected void downloadSourcesIfRequired() {
        final ToolchainSettings toolchain = getToolchain();
        final File goroot = toolchain.getGoroot();
        final String expectedVersion = toolchain.getGoversion();

        String version = readGoVersionFrom(goroot);
        if (Objects.equals(version, expectedVersion)) {
            LOGGER.debug("Found go version {}.", version);
            return;
        }

        final URI downloadUri = downloadUri();
        LOGGER.info("There was no go SDK sources of version {} found. Going to download it from {} to {} ...", expectedVersion, downloadUri, goroot);
        try {
            ArchiveUtils.download(downloadUri, goroot);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not download " + downloadUri + " to " + goroot + ".", e);
        }
        version = readGoVersionFrom(goroot);
        if (version == null) {
            throw new IllegalStateException("Downloaded sources to " + goroot + " but it could not be validated as go sources.");
        }
        if (!Objects.equals(version, expectedVersion)) {
            throw new IllegalStateException("Downloaded sources to " + goroot + " and expected sources of version " + expectedVersion + " but it is " + version + ".");
        }
        LOGGER.info("Go sources (version {}) successfully downloaded to {}.", version, goroot);
    }

    protected String readGoVersionFrom(File goroot) {
        final File file = new File(goroot, "VERSION");
        if (file.isDirectory() || !file.canRead()) {
            return null;
        }
        try {
            return readFileToString(file).trim();
        } catch (final IOException e) {
            throw new RuntimeException("Could not read " + file + ".", e);
        }
    }

    protected String goBinaryVersion() {
        final ToolchainSettings toolchain = getToolchain();
        return toolchain.goBinaryVersionOf(toolchain.getGoroot());
    }

    protected URI downloadUri() {
        final ToolchainSettings toolchain = getToolchain();
        return create(toolchain.getDownloadUriRoot() + toolchain.getGoversion() + ".src.tar.gz");
    }

    protected void downloadBootstrapIfRequired() {
        final ToolchainSettings toolchain = getToolchain();
        String version = bootstrapGoBinaryVersion();
        if (version != null) {
            LOGGER.debug("Found go bootstrap version {}.", version);
            return;
        }

        final URI downloadUri = downloadUriForBootstrap();
        final File bootstrapGoroot = toolchain.getBootstrapGoroot();
        LOGGER.info("There was no go bootstrap found. Going to download it from {} to {} ...", downloadUri, bootstrapGoroot);
        try {
            ArchiveUtils.download(downloadUri, bootstrapGoroot);
        } catch (final IOException e) {
            throw new IllegalStateException("Could not download " + downloadUri + " to " + bootstrapGoroot + ".", e);
        }
        version = bootstrapGoBinaryVersion();
        if (version == null) {
            throw new IllegalStateException("Downloaded and extracted bootstrap to " + bootstrapGoroot + " but it could not be validated as working go installation.");
        }
        if (!Objects.equals(version, toolchain.getGoversion())) {
            throw new IllegalStateException("Downloaded and extracted bootstrap to " + bootstrapGoroot + " and expected an installation in version " + toolchain.getGoversion() + " but it is " + version + ".");
        }
        LOGGER.info("Go bootstrap (version {}) successfully installed to {}.", version, bootstrapGoroot);
    }

    protected String bootstrapGoBinaryVersion() {
        final ToolchainSettings toolchain = getToolchain();
        return toolchain.goBinaryVersionOf(toolchain.getBootstrapGoroot());
    }

    protected URI downloadUriForBootstrap() {
        final ToolchainSettings toolchain = getToolchain();
        final Platform platform = Platform.currentPlatform();
        final PackageFormat packageFormat = platform.getOperatingSystem().getGoPackageFormat();
        return create(toolchain.getDownloadUriRoot() + toolchain.getGoversion() + "." + platform.getNameInGo() + packageFormat.getSuffix());
    }


}
