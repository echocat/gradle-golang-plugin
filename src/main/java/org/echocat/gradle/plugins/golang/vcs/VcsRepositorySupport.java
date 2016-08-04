package org.echocat.gradle.plugins.golang.vcs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.echocat.gradle.plugins.golang.Constants;
import org.echocat.gradle.plugins.golang.model.UpdatePolicy;
import org.echocat.gradle.plugins.golang.model.VcsRepositoryInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.*;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.createDirectoriesIfRequired;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.delete;

public abstract class VcsRepositorySupport implements VcsRepository {

    private final Gson _gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    @Nonnull
    private final VcsReference _reference;

    protected VcsRepositorySupport(@Nonnull VcsReference reference) {
        _reference = reference;
    }

    @Override
    @Nonnull
    public VcsReference getReference() {
        return _reference;
    }

    @Override
    @Nullable
    public VcsFullReference updateIfRequired(@Nonnull Path baseDirectory) throws VcsException {
        final Path targetDirectory = resolveTargetDirectoryFor(baseDirectory);
        if (!isUpdateRequired(targetDirectory)) {
            return null;
        }
        return forceUpdate(baseDirectory);
    }

    @Nonnull
    @Override
    public VcsFullReference forceUpdate(@Nonnull Path baseDirectory) throws VcsException {
        final Path targetDirectory = resolveTargetDirectoryFor(baseDirectory);
        emptyDirectoryIfExists(targetDirectory);
        final VcsFullReference result = downloadToInternal(targetDirectory);
        saveInfoFile(targetDirectory, result);
        return result;
    }

    @Nonnull
    protected abstract VcsFullReference downloadToInternal(@Nonnull Path targetDirectory) throws VcsException;

    @Nonnull
    protected Path resolveTargetDirectoryFor(@Nonnull Path baseDirectory) throws VcsException {
        final Path result = baseDirectory.resolve(getReference().getId());
        try {
            createDirectoriesIfRequired(result);
        } catch (final IOException e) {
            throw new VcsException("Could not create target directory: " + result, e);
        }
        if (!isDirectory(result)) {
            throw new VcsException("Target directory is a file: " + result);
        }
        return result;
    }

    protected void emptyDirectoryIfExists(@Nonnull Path directory) throws VcsException {
        if (exists(directory)) {
            try {
                delete(directory);
            } catch (final IOException e) {
                throw new VcsException("Could not empty target directory: " + directory, e);
            }
        }
    }

    @Nonnull
    protected Path infoFileFor(@Nonnull Path targetDirectory) throws VcsException {
        return targetDirectory.resolve(Constants.VCS_REPOSITORY_INFO_FILE_NAME);
    }

    @Nullable
    protected VcsRepositoryInfo tryReadInfoFor(@Nonnull Path targetDirectory) throws VcsException {
        final Path infoFile = infoFileFor(targetDirectory);
        final VcsReference reference = getReference();
        if (reference.getType() == VcsType.manual) {
            throw new IllegalStateException("It is not possible to update manual vcs repositories in this way.");
        }
        if (!exists(infoFile)) {
            return null;
        }
        if (!isRegularFile(infoFile)) {
            throw new VcsException(infoFile + " is expected to be an file but it isn't.");
        }
        try (final InputStream is = newInputStream(infoFile)) {
            try (final Reader reader = new InputStreamReader(is, "UTF-8")) {
                return _gson.fromJson(reader, VcsRepositoryInfo.class);
            }
        } catch (final IOException | JsonSyntaxException e) {
            throw new VcsException("Could not read " + infoFile + ".", e);
        }
    }

    protected boolean isUpdateRequired(@Nonnull Path targetDirectory) throws VcsException {
        return isUpdateRequired(tryReadInfoFor(targetDirectory));
    }

    protected boolean isUpdateRequired(@Nullable VcsRepositoryInfo info) throws VcsException {
        if (info == null) {
            return true;
        }
        final VcsReference reference = getReference();
        if (!Objects.equals(reference.getRef(), info.getRef())) {
            return true;
        }
        final UpdatePolicy updatePolicy = reference.getUpdatePolicy();
        return updatePolicy.updateRequired(info.getLastUpdatedMillis());
    }

    protected void saveInfoFile(@Nonnull Path targetDirectory, @Nonnull VcsRepositoryInfo info) throws VcsException {
        final Path infoFile = infoFileFor(targetDirectory);
        try (final OutputStream os = newOutputStream(infoFile)) {
            try (final Writer writer = new OutputStreamWriter(os, "UTF-8")) {
                _gson.toJson(info, writer);
            }
        } catch (final IOException | JsonParseException e) {
            throw new VcsException("Could not save " + infoFile + ".", e);
        }
    }

    protected void saveInfoFile(@Nonnull Path targetDirectory, @Nonnull VcsFullReference fullReference) throws VcsException {
        final VcsReference reference = getReference();
        final VcsRepositoryInfo info = new VcsRepositoryInfo()
            .setType(reference.getType())
            .setId(reference.getId())
            .setRef(reference.getRef())
            .setUri(reference.getUri())
            .setFullRef(fullReference.getFull())
            .setLastUpdatedMillis(currentTimeMillis());
        saveInfoFile(targetDirectory, info);
    }

}
