package org.echocat.gradle.plugins.golang.vcs;

public class VcsIllegalReferenceException extends VcsException {

    public VcsIllegalReferenceException(String message) {
        super(message);
    }

    public VcsIllegalReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public VcsIllegalReferenceException(Throwable cause) {
        super(cause);
    }
}
