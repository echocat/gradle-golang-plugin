package org.echocat.gradle.plugins.golang.utils;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

public interface StdStreams extends Closeable {

    @Nonnull
    public OutputStream out();

    @Nonnull
    public OutputStream err();

    public static class Impl implements StdStreams {

        @Nonnull
        public static StdStreams stdStreams(@Nonnull OutputStream out, @Nonnull OutputStream err) {
            return new Impl(out, err);
        }

        @Nonnull
        public static StdStreams stdStreams(@Nonnull OutputStream out) {
            return stdStreams(out, out);
        }

        @Nonnull
        private final OutputStream _out;
        @Nonnull
        private final OutputStream _err;

        public Impl(@Nonnull OutputStream out, @Nonnull OutputStream err) {
            _out = out;
            _err = err;
        }

        @Override
        @Nonnull
        public OutputStream out() {
            return _out;
        }

        @Override
        @Nonnull
        public OutputStream err() {
            return _err;
        }

        @Override
        public void close() throws IOException {
            closeQuietly(_out);
            closeQuietly(_err);
        }

    }

}
