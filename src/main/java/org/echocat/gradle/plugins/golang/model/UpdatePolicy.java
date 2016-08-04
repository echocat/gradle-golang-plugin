package org.echocat.gradle.plugins.golang.model;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Immutable
public final class UpdatePolicy implements Comparable<UpdatePolicy> {

    private static final UpdatePolicy ALWAYS = new UpdatePolicy(Type.always, 0);
    private static final UpdatePolicy NEVER = new UpdatePolicy(Type.never, 0);
    private static final UpdatePolicy DAILY = new UpdatePolicy(Type.daily, 60L * 24L);

    @Nonnull
    public static UpdatePolicy defaultUpdatePolicy() {
        return never();
    }

    @Nonnull
    public static UpdatePolicy interval(@Nonnegative long intervalInMinutes) {
        return new UpdatePolicy(Type.interval, intervalInMinutes);
    }

    @Nonnull
    public static UpdatePolicy always() {
        return ALWAYS;
    }

    @Nonnull
    public static UpdatePolicy never() {
        return NEVER;
    }

    @Nonnull
    public static UpdatePolicy daily() {
        return DAILY;
    }

    @Nonnull
    private final Type _type;
    @Nonnegative
    private final long _intervalInMinutes;

    @Nonnull
    public static UpdatePolicy valueOf(@Nullable String value) throws IllegalArgumentException {
        if (isEmpty(value)) {
            return defaultUpdatePolicy();
        }
        final String[] parts = StringUtils.split(value, ':');
        final Type type;
        try {
            type = Type.valueOf(parts[0]);
        } catch (final IllegalArgumentException ignored) {
            throw new IllegalArgumentException("Illegal update policy: " + value);
        }
        if (type == Type.interval) {
            if (parts.length != 2) {
                throw new IllegalArgumentException("Illegal update policy: " + value);
            }
            try {
                final long intervalInMinutes = Long.valueOf(parts[1]);
                if (intervalInMinutes < 0) {
                    throw new IllegalArgumentException("Illegal update policy: " + value);
                }
                return interval(intervalInMinutes);
            } catch (final NumberFormatException ignored) {
                throw new IllegalArgumentException("Illegal update policy: " + value);
            }
        }
        if (parts.length != 1) {
            throw new IllegalArgumentException("Illegal update policy: " + value);
        }
        if (type == Type.never) {
            return never();
        }
        if (type == Type.always) {
            return always();
        }
        if (type == Type.daily) {
            return always();
        }
        throw new IllegalArgumentException("Illegal update policy: " + value);
    }

    private UpdatePolicy(@Nonnull Type type, @Nonnegative long intervalInMinutes) {
        _type = type;
        _intervalInMinutes = intervalInMinutes;
    }

    public boolean updateRequired(@Nonnegative long lastUpdatedMillis) {
        final Type type = _type;
        if (type == Type.never) {
            return false;
        }
        if (type == Type.always) {
            return true;
        }
        final long updateRequiredAfter = lastUpdatedMillis + MINUTES.toMillis(_intervalInMinutes);
        return updateRequiredAfter < currentTimeMillis();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(_type);
        if (_type == Type.interval) {
            sb.append(':').append(_intervalInMinutes);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final UpdatePolicy that = (UpdatePolicy) o;
        return _intervalInMinutes == that._intervalInMinutes &&
            _type == that._type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_type, _intervalInMinutes);
    }

    @Override
    public int compareTo(@Nonnull UpdatePolicy that) {
        if (_intervalInMinutes != that._intervalInMinutes) {
            return Long.compare(_intervalInMinutes, that._intervalInMinutes);
        }
        if (_type != that._type) {
            return _type.compareTo(that._type);
        }
        return 0;
    }

    private enum Type {
        interval,
        always,
        never,
        daily
    }

}
