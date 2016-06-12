package org.echocat.gradle.plugins.golang.model;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class UpdatePolicy {

    private enum Type {
        interval,
        always,
        never,
        daily
    }

    @Nonnull
    private Type _type = Type.never;
    @Nonnegative
    private long _intervalInMinutes;

    @Nonnull
    public static UpdatePolicy valueOf(@Nullable String value) throws IllegalArgumentException {
        return new UpdatePolicy().set(value);
    }

    @Nonnull
    public UpdatePolicy set(@Nullable String value) throws IllegalArgumentException {
        if (isEmpty(value)) {
            _type = Type.never;
            _intervalInMinutes = 0;
            return this;
        }
        final String[] parts = StringUtils.split(value, ":", 2);
        try {
            _type = Type.valueOf(parts[0]);
        } catch (final IllegalArgumentException ignored) {
            throw new IllegalArgumentException("Illegal update policy: " + value);
        }
        if ((parts.length > 1 && _type != Type.interval) || (parts.length == 1 && _type == Type.interval)) {
            throw new IllegalArgumentException("Illegal update policy: " + value);
        }
        if (_type == Type.interval) {
            try {
                _intervalInMinutes = Long.valueOf(parts[1]);
            } catch (final NumberFormatException ignored) {
                throw new IllegalArgumentException("Illegal update policy: " + value);
            }
            if (_intervalInMinutes < 0) {
                throw new IllegalArgumentException("Illegal update policy: " + value);
            }
        } else if (_type == Type.daily) {
            _intervalInMinutes = 60L * 24L;
        } else {
            _intervalInMinutes = 0;
        }
        return this;
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

    @Nonnull
    public static UpdatePolicy defaultUpdatePolicy() {
        return new UpdatePolicy();
    }

}
