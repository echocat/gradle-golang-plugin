package org.echocat.gradle.plugins.golang.testing.report;

import static java.util.Locale.US;

public enum Result {
    PASS,
    FAIL,
    SKIP;

    @Override
    public String toString() {
        return name().toUpperCase(US);
    }
}
