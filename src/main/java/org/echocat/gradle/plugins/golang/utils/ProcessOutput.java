package org.echocat.gradle.plugins.golang.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessOutput implements Closeable {

    private static final int EOF = -1;

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

    public void waitFor() throws InterruptedException {
        _stdoutThread.join();
        _stderrThread.join();
    }

    @Override
    public void close() throws IOException {
        _stdoutThread.interrupt();
        _stderrThread.interrupt();
        try {
            _stdoutThread.join();
            _stderrThread.join();
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
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
                final byte[] buffer = new byte[4096];
                int n;
                while (!Thread.currentThread().isInterrupted() && EOF != (n = _from.read(buffer))) {
                    _to.write(buffer, 0, n);
                }
            } catch (final IOException ignored) {}
        }
    }

}
