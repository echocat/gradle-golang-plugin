package org.echocat.gradle.plugins.golang.logging;

import org.apache.maven.plugin.logging.Log;

public class LogDelegate implements Log {

    private Log _delegate = DefaultLog.INSTANCE;

    public void setDelegate(Log delegate) {
        _delegate = delegate;
    }

    @Override
    public boolean isDebugEnabled() {return _delegate.isDebugEnabled();}

    @Override
    public void debug(CharSequence content) {_delegate.debug(content);}

    @Override
    public void debug(CharSequence content, Throwable error) {_delegate.debug(content, error);}

    @Override
    public void debug(Throwable error) {_delegate.debug(error);}

    @Override
    public boolean isInfoEnabled() {return _delegate.isInfoEnabled();}

    @Override
    public void info(CharSequence content) {_delegate.info(content);}

    @Override
    public void info(CharSequence content, Throwable error) {_delegate.info(content, error);}

    @Override
    public void info(Throwable error) {_delegate.info(error);}

    @Override
    public boolean isWarnEnabled() {return _delegate.isWarnEnabled();}

    @Override
    public void warn(CharSequence content) {_delegate.warn(content);}

    @Override
    public void warn(CharSequence content, Throwable error) {_delegate.warn(content, error);}

    @Override
    public void warn(Throwable error) {_delegate.warn(error);}

    @Override
    public boolean isErrorEnabled() {return _delegate.isErrorEnabled();}

    @Override
    public void error(CharSequence content) {_delegate.error(content);}

    @Override
    public void error(CharSequence content, Throwable error) {_delegate.error(content, error);}

    @Override
    public void error(Throwable error) {_delegate.error(error);}

}
