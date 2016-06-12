package org.echocat.gradle.plugins.golang.vcs;

import org.echocat.gradle.plugins.golang.model.UpdatePolicy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.gradle.plugins.golang.model.UpdatePolicy.defaultUpdatePolicy;

public class VcsReference extends BaseVcsReference {

    public static VcsReference vcsReference(@Nonnull VcsType type, @Nonnull String name, @Nonnull String plainUri, @Nullable String ref) throws VcsValidationException {
        return new VcsReference(type, name, plainUri, ref);
    }

    public VcsReference(@Nonnull VcsType type, @Nonnull String id, @Nonnull String plain, @Nullable String ref) throws VcsValidationException {
        super(type, id, URI.create(plain), ref, defaultUpdatePolicy());
        //noinspection ConstantConditions
        if (type == null) {
            throw new VcsValidationException("Empty type provided.");
        }
        if (isEmpty(plain)) {
            throw new VcsValidationException("Empty uri provided.");
        }
    }

    public VcsReference(@Nonnull VcsType type, @Nonnull String id, @Nonnull URI uri, @Nullable String ref, @Nonnull UpdatePolicy updatePolicy) {
        super(type, id, uri, ref, updatePolicy);
        //noinspection ConstantConditions
        if (type == null) {
            throw new NullPointerException("type is null");
        }
        //noinspection ConstantConditions
        if (uri == null) {
            throw new NullPointerException("uri is null");
        }
    }

    @Nonnull
    @Override
    public VcsType getType() {
        //noinspection ConstantConditions
        return super.getType();
    }

    @Override
    @Nonnull
    public URI getUri() {
        //noinspection ConstantConditions
        return super.getUri();
    }

}
