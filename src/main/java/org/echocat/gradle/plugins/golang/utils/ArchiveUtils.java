package org.echocat.gradle.plugins.golang.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.net.URI;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.IOUtils.copy;

public class ArchiveUtils {

    private static final Pattern REMOVE_LEADING_GO_PATH_PATTERN = Pattern.compile("^(|\\./)go/");

    public static void download(URI uri, File to) throws IOException {
        if (to.exists()) {
            forceDelete(to);
        }
        forceMkdir(to);

        final OkHttpClient client = new OkHttpClient();
        final Request request = new Builder()
            .url(uri.toURL())
            .build();

        final File tempFile = File.createTempFile("golang-maven-plugin", "." + getExtension(uri.toString()));
        try {
            try (final InputStream is = client.newCall(request).execute().body().byteStream()) {
                try (final OutputStream os = new FileOutputStream(tempFile)) {
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
            //noinspection ResultOfMethodCallIgnored
            tempFile.delete();
        }
    }

    public static void unTarGz(File file, File target) throws IOException {
        try (final InputStream is = new FileInputStream(file)) {
            final InputStream gzip = new GZIPInputStream(is);
            final TarArchiveInputStream archive = new TarArchiveInputStream(gzip);
            TarArchiveEntry entry = archive.getNextTarEntry();
            while (entry != null) {
                final File entryFile = new File(target, REMOVE_LEADING_GO_PATH_PATTERN.matcher(entry.getName()).replaceFirst("")).getCanonicalFile();
                if (entry.isDirectory()) {
                    forceMkdir(entryFile);
                } else {
                    forceMkdir(entryFile.getParentFile());
                    try (final OutputStream os = new FileOutputStream(entryFile)) {
                        copy(archive, os);
                    }
                    final int mode = entry.getMode();
                    //noinspection OctalInteger,ResultOfMethodCallIgnored,IncompatibleBitwiseMaskOperation
                    entryFile.setExecutable(
                        (mode | 0100) > 0,
                        !((mode | 0001) > 0)
                    );
                }
                entry = archive.getNextTarEntry();
            }
        }
    }

    public static void unZip(File file, File target) throws IOException {
        try (final ZipFile zipFile = new ZipFile(file)) {
            final Enumeration<ZipArchiveEntry> files = zipFile.getEntriesInPhysicalOrder();
            while (files.hasMoreElements()) {
                final ZipArchiveEntry entry = files.nextElement();
                final File entryFile = new File(target, REMOVE_LEADING_GO_PATH_PATTERN.matcher(entry.getName()).replaceFirst("")).getCanonicalFile();
                if (entry.isDirectory()) {
                    forceMkdir(entryFile);
                } else {
                    forceMkdir(entryFile.getParentFile());
                    try (final InputStream is = zipFile.getInputStream(entry)) {
                        try (final OutputStream os = new FileOutputStream(entryFile)) {
                            copy(is, os);
                        }
                    }
                }

            }
        }
    }
}
