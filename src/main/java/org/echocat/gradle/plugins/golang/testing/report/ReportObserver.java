package org.echocat.gradle.plugins.golang.testing.report;

import org.echocat.gradle.plugins.golang.utils.StdStreams;

import javax.annotation.Nonnull;

public interface ReportObserver  extends StdStreams {

    @Nonnull
    public Report getReport();

    public void registerNotifier(@Nonnull Notifier notifier);

    public static interface Notifier {

        public void onTestStarted(@Nonnull String name);

    }
}
