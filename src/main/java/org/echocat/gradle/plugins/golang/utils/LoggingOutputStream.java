package org.echocat.gradle.plugins.golang.utils;

import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;

public class LoggingOutputStream extends OutputStream {

    private final Logger _logger;
    private final boolean _error;
    @SuppressWarnings("StringBufferField")
    private final StringBuilder _buffer = new StringBuilder();

    private boolean _closed;

    public LoggingOutputStream(@Nonnull Logger logger, boolean error) {
        _error = error;
        _logger = logger;
    }

    protected void assertNotClosed() throws IOException {
        if (_closed) {
            throw new IOException("Already closed");
        }
    }

    @Override
    public void close() throws IOException {
        assertNotClosed();
        _closed = true;
        if (_buffer.length() > 0) {
            if (_error) {
                _logger.error(_buffer.toString());
            } else {
                _logger.info(_buffer.toString());
            }
        }
    }

    @Override
    public void write(@Nonnull byte[] bytes, int off, int len) throws IOException {
        assertNotClosed();
        for (final char c : new String(bytes, off, len).toCharArray()) {
            //noinspection StatementWithEmptyBody
            if (c == '\r') {
                // ignored
            } else if (c == '\n') {
                if (_error) {
                    _logger.error(_buffer.toString());
                } else {
                    _logger.info(_buffer.toString());
                }
                _buffer.setLength(0);
            } else {
                _buffer.append(c);
            }
        }
    }

    @Override
    public void write(final int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

}