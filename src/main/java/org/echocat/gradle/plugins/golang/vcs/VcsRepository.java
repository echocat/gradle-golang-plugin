package org.echocat.gradle.plugins.golang.vcs;

import org.gradle.internal.logging.progress.ProgressLogger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;

public interface VcsRepository {

    @Nonnull
    public VcsReference getReference();

    public boolean isWorking() throws VcsException;

    @Nullable
    public VcsFullReference updateIfRequired(@Nonnull Path baseDirectory) throws VcsException;

    @Nullable
    public VcsFullReference updateIfRequired(@Nonnull Path baseDirectory, @Nullable ProgressMonitor progressMonitor) throws VcsException;

    @Nonnull
    public VcsFullReference forceUpdate(@Nonnull Path baseDirectory) throws VcsException;

    @Nonnull
    public VcsFullReference forceUpdate(@Nonnull Path baseDirectory, @Nullable ProgressMonitor progressMonitor) throws VcsException;

    public static interface ProgressMonitor {

        public void started();

        public void update(@Nonnegative double progress);

        public void finished();

    }

    public static class Utils {

        @Nonnull
        public static ProgressMonitor progressMonitorFor(@Nonnull final String messagePattern, @Nonnull final ProgressLogger progressLogger) {
            final MessageFormat messageFormat = new MessageFormat(messagePattern, Locale.US);
            return new ProgressMonitor() {
                @Override
                public void started() {}

                @Override
                public void update(@Nonnegative double progress) {
                    progressLogger.progress(messageFormat.format(new Object[]{progress}));
                }

                @Override
                public void finished() {
                    update(1d);
                }
            };
        }

    }
}
