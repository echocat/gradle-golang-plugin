package org.echocat.gradle.plugins.golang.utils;

import org.apache.commons.io.output.TeeOutputStream;
import org.echocat.gradle.plugins.golang.utils.StdStreams.Impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.OutputStream;

public final class IOUtils {

    @Nonnull
    public static StdStreams tee(@Nonnull StdStreams a, @Nonnull StdStreams b) {
        final OutputStream out = new TeeOutputStream(a.out(), b.out());
        final OutputStream err = new TeeOutputStream(a.err(), b.err());
        return new Impl(out, err);
    }

    public static void closeQuietly(@Nullable AutoCloseable what) {
        try {
            if (what != null) {
                what.close();
            }
        } catch (final Exception ignored) {
        }
    }

}
