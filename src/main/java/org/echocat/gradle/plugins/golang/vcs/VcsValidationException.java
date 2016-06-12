package org.echocat.gradle.plugins.golang.vcs;

public class VcsValidationException extends VcsException {

    public VcsValidationException(String message) {
        super(message);
    }

    public VcsValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public VcsValidationException(Throwable cause) {
        super(cause);
    }

}
