package org.echocat.gradle.plugins.golang.model;

import org.apache.commons.io.FilenameUtils;
import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.echocat.gradle.plugins.golang.vcs.VcsValidationException;
import org.gradle.api.artifacts.Dependency;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.echocat.gradle.plugins.golang.model.GolangDependency.Type.explicit;
import static org.echocat.gradle.plugins.golang.model.UpdatePolicy.defaultUpdatePolicy;

public class GolangDependency implements Dependency, Comparable<GolangDependency> {

    @Nonnull
    private String _group = "<unknown>";
    @Nullable
    private String _version;
    @Nullable
    private URI _repositoryUri;
    @Nullable
    private VcsType _repositoryType;
    @Nonnull
    private UpdatePolicy _updatePolicy = defaultUpdatePolicy();
    @Nonnull
    private Type _type = explicit;

    @Nullable
    private GolangDependency _parent;
    @Nullable
    private Path _location;

    @Nonnull
    public static GolangDependency newDependency(@Nonnull String group) {
        return new GolangDependency()
            .setGroup(group);
    }

    public GolangDependency() {}

    public GolangDependency(@Nonnull Dependency raw) {
        if (raw instanceof GolangDependency) {
            final GolangDependency original = (GolangDependency) raw;
            setGroup(original.getGroup());
            setVersion(original.getVersion());
            setRepositoryUri(original.getRepositoryUri());
            setRepositoryType(original.getRepositoryType());
            setUpdatePolicy(original.getUpdatePolicy());
        } else {
            setGroup(raw.getGroup());
            setVersion(raw.getVersion());
        }
    }

    @Override
    @Nonnull
    public String getGroup() {
        return _group;
    }

    @Override
    @Nonnull
    public String getName() {
        return FilenameUtils.getName(getGroup());
    }

    @Override
    @Nullable
    public String getVersion() {
        return _version;
    }

    @Nullable
    public URI getRepositoryUri() {
        return _repositoryUri;
    }

    @Nullable
    public VcsType getRepositoryType() {
        return _repositoryType;
    }

    @Nonnull
    public UpdatePolicy getUpdatePolicy() {
        return _updatePolicy;
    }

    @Nonnull
    public Type getType() {
        return _type;
    }

    @Nullable
    public GolangDependency getParent() {
        return _parent;
    }

    @Nullable
    public Path getLocation() {
        return _location;
    }

    @Nonnull
    public GolangDependency setGroup(@Nonnull String group) {
        _group = group;
        return this;
    }

    @Nonnull
    public GolangDependency setVersion(@Nullable String version) {
        _version = version;
        return this;
    }

    @Nonnull
    public GolangDependency setRepositoryUri(@Nullable URI repositoryUri) {
        _repositoryUri = repositoryUri;
        return this;
    }

    @Nonnull
    public GolangDependency setRepositoryType(@Nullable VcsType repositoryType) {
        _repositoryType = repositoryType;
        return this;
    }

    @Nonnull
    public GolangDependency setUpdatePolicy(@Nonnull UpdatePolicy updatePolicy) {
        //noinspection ConstantConditions
        if (updatePolicy == null) {
            throw new NullPointerException("There is no updatePolicy provided.");
        }
        _updatePolicy = updatePolicy;
        return this;
    }

    @Nonnull
    public GolangDependency setType(@Nonnull Type type) {
        //noinspection ConstantConditions
        if (type == null) {
            throw new NullPointerException("There is no type provided.");
        }
        _type = type;
        return this;
    }

    @Nonnull
    public GolangDependency setParent(@Nullable GolangDependency parent) {
        _parent = parent;
        return this;
    }

    @Nonnull
    public GolangDependency setLocation(@Nullable Path location) {
        _location = location;
        return this;
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        return dependency instanceof GolangDependency
            && equals(dependency)
            ;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final String group = getGroup();
        sb.append(getType()).append(' ').append(isEmpty(group) ? "<unknown>" : group);
        final String version = getVersion();
        final URI repositoryUri = getRepositoryUri();
        final VcsType repositoryType = getRepositoryType();
        final UpdatePolicy updatePolicy = getUpdatePolicy();
        final GolangDependency parent = getParent();
        final Path location = getLocation();
        if (!isEmpty(version) || repositoryUri != null || repositoryType != null || !defaultUpdatePolicy().equals(updatePolicy)) {
            sb.append(" (");
            boolean first = true;
            if (!isEmpty(version)) {
                sb.append("version=").append(version);
                first = false;
            }
            if (repositoryUri != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append("repositoryUri=").append(repositoryUri);
                first = false;
            }
            if (repositoryType != null) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append("repositoryType=").append(repositoryType);
                first = false;
            }
            if (!defaultUpdatePolicy().equals(updatePolicy)) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append("updatePolicy=").append(updatePolicy);
            }
            sb.append(')');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final GolangDependency that = (GolangDependency) o;
        return Objects.equals(getGroup(), that.getGroup()) &&
            Objects.equals(getVersion(), that.getVersion()) &&
            Objects.equals(getRepositoryUri(), that.getRepositoryUri()) &&
            getRepositoryType() == that.getRepositoryType() &&
            Objects.equals(getUpdatePolicy(), that.getUpdatePolicy()) &&
            Objects.equals(getLocation(), that.getLocation())
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroup(), getVersion(), getRepositoryUri(), getRepositoryType(), getUpdatePolicy(), getParent());
    }

    @Override
    public int compareTo(@Nonnull GolangDependency that) {
        if (!Objects.equals(getGroup(), that.getGroup())) {
            return compare(getGroup(), that.getGroup());
        }
        if (!Objects.equals(getVersion(), that.getVersion())) {
            return compare(getVersion(), that.getVersion());
        }
        if (!Objects.equals(getRepositoryUri(), that.getRepositoryUri())) {
            return compare(getRepositoryUri(), that.getRepositoryUri());
        }
        if (getRepositoryType() != that.getRepositoryType()) {
            return compare(getRepositoryType(), that.getRepositoryType());
        }
        if (!Objects.equals(getUpdatePolicy(), that.getUpdatePolicy())) {
            return compare(getUpdatePolicy(), that.getUpdatePolicy());
        }
        if (!Objects.equals(getLocation(), that.getLocation())) {
            return compare(getLocation(), that.getLocation());
        }
        return 0;
    }

    protected <T extends Comparable<T>> int compare(@Nullable T a, @Nullable T b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }

    @Override
    public GolangDependency copy() {
        return new GolangDependency()
            .setGroup(getGroup())
            .setVersion(getVersion())
            .setRepositoryUri(getRepositoryUri())
            .setRepositoryType(getRepositoryType())
            .setUpdatePolicy(getUpdatePolicy())
            .setLocation(getLocation())
            .setParent(getParent())
            .setType(getType())
            ;
    }

    @Nonnull
    public RawVcsReference toRawVcsReference() throws VcsValidationException {
        final String name = getGroup();
        if (isEmpty(name)) {
            throw new VcsValidationException("Name of a dependency must be set.");
        }
        final String ref = getVersion();
        final URI repositoryUri = getRepositoryUri();
        final VcsType repositoryType = getRepositoryType();
        final UpdatePolicy updatePolicy = getUpdatePolicy();
        return new RawVcsReference(repositoryType, name, repositoryUri, ref, updatePolicy);
    }

    public enum Type {
        explicit,
        implicit,
        source,
        system
    }

}
