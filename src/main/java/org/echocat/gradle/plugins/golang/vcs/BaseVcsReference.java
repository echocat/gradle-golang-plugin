package org.echocat.gradle.plugins.golang.vcs;

import org.echocat.gradle.plugins.golang.model.UpdatePolicy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class BaseVcsReference {

    @Nullable
    private final VcsType _type;
    @Nonnull
    private final String _id;
    @Nullable
    private final URI _uri;
    @Nullable
    private final String _ref;
    @Nonnull
    private final UpdatePolicy _updatePolicy;

    public BaseVcsReference(@Nullable VcsType type, @Nonnull String id, @Nullable URI uri, @Nullable String ref, @Nonnull UpdatePolicy updatePolicy) {
        _type = type;
        _id = id;
        _uri = uri;
        _ref = ref;
        _updatePolicy = updatePolicy;
    }

    @Nullable
    public VcsType getType() {
        return _type;
    }

    @Nonnull
    public String getId() {
        return _id;
    }

    @Nullable
    public String getRef() {
        return _ref;
    }

    @Nullable
    public URI getUri() {
        return _uri;
    }

    @Nonnull
    public UpdatePolicy getUpdatePolicy() {
        return _updatePolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final BaseVcsReference vcsReference = (BaseVcsReference) o;
        return Objects.equals(_uri, vcsReference._uri)
            && Objects.equals(_ref, vcsReference._ref)
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_uri, _ref);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getId());
        final String ref = getRef();
        if (isNotEmpty(ref)) {
            sb.append('@').append(ref);
        }
        return sb.toString();
    }

}
