package org.echocat.gradle.plugins.golang.logging;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class MojoLoggerFactory implements ILoggerFactory {

    private static final LogDelegate LOG_DELEGATE = new LogDelegate();

    @Override
    public Logger getLogger(String name) {
        return new MojoLogger(LOG_DELEGATE, name);
    }

    public static void setLog(Log log) {
        LOG_DELEGATE.setDelegate(log != null ? log : DefaultLog.INSTANCE);
    }

}
