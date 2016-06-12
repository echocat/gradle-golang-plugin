package org.echocat.gradle.plugins.golang.logging;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public class MojoLogger extends MarkerIgnoringBase {

    private final Log _delegate;

    public MojoLogger(Log delegate, String name) {
        _delegate = delegate;
        // noinspection AssignmentToSuperclassField
        this.name = name;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {}

    @Override
    public void trace(String format, Object arg) {}

    @Override
    public void trace(String format, Object arg1, Object arg2) {}

    @Override
    public void trace(String format, Object... arguments) {}

    @Override
    public void trace(String msg, Throwable t) {}

    @Override
    public boolean isDebugEnabled() {
        return _delegate.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        debug(msg, (Throwable) null);
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg);
            debug(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isDebugEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            debug(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (isDebugEnabled()) {
            final FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
            debug(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            _delegate.debug(msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return _delegate.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        info(msg, (Throwable) null);
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg);
            info(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isInfoEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            info(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (isInfoEnabled()) {
            final FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
            info(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            _delegate.info(msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return _delegate.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        warn(msg, (Throwable) null);
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg);
            warn(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isWarnEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            warn(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (isWarnEnabled()) {
            final FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
            warn(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            _delegate.warn(msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return _delegate.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        error(msg, (Throwable) null);
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg);
            error(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isErrorEnabled()) {
            final FormattingTuple tuple = MessageFormatter.format(format, arg1, arg2);
            error(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (isErrorEnabled()) {
            final FormattingTuple tuple = MessageFormatter.arrayFormat(format, arguments);
            error(tuple.getMessage(), tuple.getThrowable());
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            _delegate.error(msg, t);
        }
    }
    
}
