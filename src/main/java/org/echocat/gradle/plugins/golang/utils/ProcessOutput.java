package org.echocat.gradle.plugins.golang.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.apache.commons.io.IOUtils.copy;

public class ProcessOutput implements Closeable {

    private final Thread _stdoutThread;
    private final Thread _stderrThread;

    public ProcessOutput(Process process, OutputStream stdout, OutputStream stderr) {
        _stdoutThread = new Thread(new Copier(process.getInputStream(), stdout), "stdout");
        _stderrThread = new Thread(new Copier(process.getErrorStream(), stderr), "stderr");

        _stdoutThread.setDaemon(true);
        _stderrThread.setDaemon(true);

        _stdoutThread.start();
        _stderrThread.start();
    }

    @Override
    public void close() throws IOException {
        _stdoutThread.interrupt();
        _stderrThread.interrupt();
    }

    private static class Copier implements Runnable {

        private final InputStream _from;
        private final OutputStream _to;

        public Copier(InputStream from, OutputStream to) {
            _from = from;
            _to = to;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    copy(_from, _to);
                }
            } catch (final IOException ignored) {}
        }
    }

}
