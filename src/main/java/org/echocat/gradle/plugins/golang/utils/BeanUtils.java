package org.echocat.gradle.plugins.golang.utils;

import javax.annotation.Nonnull;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

public class BeanUtils {

    public static <T> void copyNonNulls(@Nonnull Class<T> type, @Nonnull T from, @Nonnull T to) {
        if (!type.isInstance(from)) {
            throw new IllegalArgumentException("From is not an instance of " + type.getName() + ". Got: " + from);
        }
        if (!type.isInstance(to)) {
            throw new IllegalArgumentException("To is not an instance of " + type.getName() + ". Got: " + to);
        }
        try {
            final BeanInfo info = Introspector.getBeanInfo(type);
            for (final PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
                final Method readMethod = descriptor.getReadMethod();
                final Method writeMethod = descriptor.getWriteMethod();
                if (readMethod != null && writeMethod != null) {
                    final Object value = readMethod.invoke(from);
                    if (value instanceof Map) {
                        //noinspection unchecked
                        final Map<Object, Object> properties = (Map<Object, Object>) readMethod.invoke(to);
                        if (properties != null) {
                            //noinspection unchecked
                            properties.putAll((Map<Object, Object>) value);
                        } else {
                            writeMethod.invoke(to, value);
                        }
                    } else if (value != null) {
                        writeMethod.invoke(to, value);
                    }
                }
            }
        } catch (final Exception e) {
            throw new RuntimeException("Could not copy properties from " + from + " to " + to + ".", e);
        }
    }

}
