package org.echocat.gradle.plugins.golang.model;

public enum PackageFormat {
    ZIP(".zip"),
    TGZ(".tar.gz");

    private final String _suffix;

    PackageFormat(String suffix) {
        _suffix = suffix;
    }

    public String getSuffix() {
        return _suffix;
    }

    public static PackageFormat forSuffix(String suffix) {
        for (final PackageFormat candidate : values()) {
            if (candidate._suffix.equalsIgnoreCase(suffix)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("There is no package format for suffix '" + suffix + "'.");
    }

}
