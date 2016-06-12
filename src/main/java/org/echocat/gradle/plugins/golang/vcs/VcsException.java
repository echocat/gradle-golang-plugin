package org.echocat.gradle.plugins.golang.vcs;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class VcsException extends IOException {

    public VcsException(String message) {
        super(message);
    }

    public VcsException(String message, Throwable cause) {
        super(messageFor(message, cause), cause);
    }

    public VcsException(Throwable cause) {
        this(null, cause);
    }

    protected static String messageFor(String message, Throwable cause) {
        if (isNotEmpty(message)) {
            return message;
        }
        if (cause != null) {
            final String causeMessage = cause.getMessage();
            if (isNotEmpty(causeMessage)) {
                return causeMessage;
            }
        }
        return null;
    }

}
