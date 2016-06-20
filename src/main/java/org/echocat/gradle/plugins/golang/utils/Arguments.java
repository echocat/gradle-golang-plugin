package org.echocat.gradle.plugins.golang.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Arguments {

    @Retention(RUNTIME)
    @Target(FIELD)
    public @interface Argument {
        public String value();
    }

    @Nonnull
    public static <T> Map<String, String> argumentMapOf(@Nonnull Class<T> type, @Nullable T instance) {
        final Map<String, String> result = new HashMap<>();
        if (instance != null) {
            for (final Field field : type.getDeclaredFields()) {
                final Argument argument = field.getAnnotation(Argument.class);
                if (argument != null) {
                    final Object plainValue;
                    try {
                        field.setAccessible(true);
                        plainValue = field.get(instance);
                    } catch (final Exception e) {
                        throw new IllegalStateException("Could not get value of field " + field + ".", e);
                    }
                    if (field.getType().equals(Boolean.class)) {
                        if (plainValue != null && (Boolean) plainValue) {
                            result.put(argument.value(), null);
                        }
                    } else if (field.getType().equals(Integer.class)) {
                        if (plainValue != null) {
                            result.put(argument.value(), plainValue.toString());
                        }
                    } else if (field.getType().equals(String.class)) {
                        if (isNotEmpty((String) plainValue)) {
                            result.put(argument.value(), plainValue.toString());
                        }
                    }
                }
            }
        }
        return result;
    }

}
