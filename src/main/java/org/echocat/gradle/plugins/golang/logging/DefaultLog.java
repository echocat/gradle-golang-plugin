package org.echocat.gradle.plugins.golang.logging;

import org.apache.maven.plugin.logging.Log;

import java.io.PrintStream;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class DefaultLog implements Log {

    public static final String DEBUG_PROPERTY_NAME = DefaultLog.class.getName() + ".debugEnabled";

    public static void setDebugEnabled(boolean value) {
        setProperty(DEBUG_PROPERTY_NAME, value ? "true" : "false");
    }

    public static final DefaultLog INSTANCE = new DefaultLog();

    @Override
    public boolean isDebugEnabled() {
        return "true".equals(getProperty(DEBUG_PROPERTY_NAME));
    }

    @Override
    public void debug(CharSequence content) {
        debug(content, null);
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        print(System.out, "DEBUG", content, error);
    }

    @Override
    public void debug(Throwable error) {
        debug(null, error);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(CharSequence content) {
        info(content, null);
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        print(System.out, "INFO", content, error);
    }

    @Override
    public void info(Throwable error) {
        info(null, error);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(CharSequence content) {
        warn(content, null);
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        print(System.err, "WARN", content, error);
    }

    @Override
    public void warn(Throwable error) {
        warn(null, error);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(CharSequence content) {
        error(content, null);
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        print(System.err, "ERROR", content, error);
    }

    @Override
    public void error(Throwable error) {
        error(null, error);
    }

    protected void print(PrintStream out, String prefix, CharSequence content, Throwable error) {
        if (content != null || error != null) {
            final StringBuilder sb = new StringBuilder();
            sb.append('[').append(prefix).append("] ");
            if (isNotEmpty(content)) {
                sb.append(content);
            } else {
                sb.append(error.getMessage());
            }
            out.println(sb);
            if (error != null) {
                error.printStackTrace(out);
            }
        }
    }

}
