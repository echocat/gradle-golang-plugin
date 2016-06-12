package org.echocat.gradle.plugins.golang.model;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntegerVersion implements Comparable<IntegerVersion> {

    private static final Pattern PARSE_PATTERN = Pattern.compile("^(?<major>[0-9]{1,9})(?:\\.(?<minor>[0-9]{1,9})(?:\\.(?<patch>[0-9]{1,9}))?)?$");

    @Nonnegative
    private final int _major;
    @Nonnegative
    @Nullable
    private final Integer _minor;
    @Nonnegative
    @Nullable
    private final Integer _patch;

    public IntegerVersion(@Nonnull String plain) throws IllegalArgumentException {
        final Matcher matcher = PARSE_PATTERN.matcher(plain);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal integer version: " + plain);
        }
        _major = Integer.valueOf(matcher.group("major"));
        final String plainMinor = matcher.group("minor");
        final String plainPatch = matcher.group("patch");
        _minor = plainMinor != null ? Integer.valueOf(plainMinor) : null;
        _patch = plainPatch != null ? Integer.valueOf(plainPatch) : null;
    }

    public IntegerVersion(@Nonnegative int major, @Nonnegative @Nullable Integer minor, @Nonnegative @Nullable Integer patch) {
        if (minor == null && patch != null) {
            throw new IllegalArgumentException("If the minor is null patch could never be not null.");
        }
        _major = major;
        _minor = minor;
        _patch = patch;
    }

    @Nonnegative
    public int getMajor() {
        return _major;
    }

    @Nonnegative
    @Nullable
    public Integer getMinor() {
        return _minor;
    }

    @Nonnegative
    @Nullable
    public Integer getPatch() {
        return _patch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final IntegerVersion version = (IntegerVersion) o;
        return _major == version._major &&
            Objects.equals(_minor, version._minor) &&
            Objects.equals(_patch, version._patch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_major, _minor, _patch);
    }

    @Override
    public int compareTo(@Nullable IntegerVersion that) {
        if (that == null) {
            return 1;
        }
        if (_major != that._major) {
            return _major - that._major;
        }
        if (!Objects.equals(_minor, that._minor)) {
            if (_minor == null) {
                return -1;
            }
            if (that._minor == null) {
                return 1;
            }
            return _minor - that._minor;
        }
        if (!Objects.equals(_patch, that._patch)) {
            if (_patch == null) {
                return -1;
            }
            if (that._patch == null) {
                return 1;
            }
            return _patch - that._patch;
        }
        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_major);
        if (_minor != null) {
            sb.append('.').append(_minor);
        }
        if (_patch != null) {
            sb.append('.').append(_patch);
        }
        return sb.toString();
    }
}
