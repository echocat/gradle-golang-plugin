package org.echocat.gradle.plugins.golang.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static java.nio.file.Files.*;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.IOUtils.copy;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.createDirectoriesIfRequired;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.deleteQuietly;
import static org.echocat.gradle.plugins.golang.utils.FileUtils.ensureParentOf;

public class ArchiveUtils {

    private static final Pattern REMOVE_LEADING_GO_PATH_PATTERN = Pattern.compile("^(|\\./)go/");

    public static void download(URI uri, Path to) throws IOException {
        if (exists(to)) {
            deleteQuietly(to);
        }
        createDirectoriesIfRequired(to);

        final OkHttpClient client = new OkHttpClient();
        final Request request = new Builder()
            .url(uri.toURL())
            .build();

        final Path tempFile = createTempFile("golang-maven-plugin", "." + getExtension(uri.toString()));
        try {
            try (final InputStream is = client.newCall(request).execute().body().byteStream()) {
                try (final OutputStream os = newOutputStream(tempFile)) {
                    copy(is, os);
                }
            }
            if (uri.toString().endsWith(".tar.gz")) {
                unTarGz(tempFile, to);
            } else if (uri.toString().endsWith(".zip")) {
                unZip(tempFile, to);
            } else {
                throw new IllegalStateException("Does not support download archive of type " + uri + ".");
            }
        } finally {
            deleteQuietly(tempFile);
        }
    }

    public static void unTarGz(Path file, Path target) throws IOException {
        try (final InputStream is = newInputStream(file)) {
            final InputStream gzip = new GZIPInputStream(is);
            final TarArchiveInputStream archive = new TarArchiveInputStream(gzip);
            TarArchiveEntry entry = archive.getNextTarEntry();
            while (entry != null) {
                final Path entryFile = target.resolve(REMOVE_LEADING_GO_PATH_PATTERN.matcher(entry.getName()).replaceFirst("")).toAbsolutePath();
                if (entry.isDirectory()) {
                    createDirectoriesIfRequired(entryFile);
                } else {
                    ensureParentOf(entryFile);
                    try (final OutputStream os = newOutputStream(entryFile)) {
                        copy(archive, os);
                    }
                    final PosixFileAttributeView view = getFileAttributeView(entryFile, PosixFileAttributeView.class);
                    if (view != null) {
                        final int mode = entry.getMode();
                        final Set<PosixFilePermission> perms = new HashSet<>();
                        perms.add(PosixFilePermission.OWNER_READ);
                        perms.add(PosixFilePermission.GROUP_READ);
                        perms.add(PosixFilePermission.OTHERS_READ);
                        //noinspection OctalInteger,ResultOfMethodCallIgnored,IncompatibleBitwiseMaskOperation
                        if ((mode | 0001) > 0) {
                            perms.add(PosixFilePermission.OWNER_EXECUTE);
                        }
                        //noinspection OctalInteger,ResultOfMethodCallIgnored,IncompatibleBitwiseMaskOperation
                        if ((mode | 0100) > 0) {
                            perms.add(PosixFilePermission.GROUP_EXECUTE);
                            perms.add(PosixFilePermission.OTHERS_EXECUTE);
                        }
                        view.setPermissions(perms);
                    }
                }
                entry = archive.getNextTarEntry();
            }
        }
    }

    public static void unZip(Path file, Path target) throws IOException {
        try (final ZipFile zipFile = new ZipFile(file.toFile())) {
            final Enumeration<ZipArchiveEntry> files = zipFile.getEntriesInPhysicalOrder();
            while (files.hasMoreElements()) {
                final ZipArchiveEntry entry = files.nextElement();
                final Path entryFile = target.resolve(REMOVE_LEADING_GO_PATH_PATTERN.matcher(entry.getName()).replaceFirst("")).toAbsolutePath();
                if (entry.isDirectory()) {
                    createDirectoriesIfRequired(entryFile);
                } else {
                    ensureParentOf(entryFile);
                    try (final InputStream is = zipFile.getInputStream(entry)) {
                        try (final OutputStream os = newOutputStream(entryFile)) {
                            copy(is, os);
                        }
                    }
                }
            }
        }
    }
}
