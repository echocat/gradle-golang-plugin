package org.echocat.gradle.plugins.golang.model;

import org.apache.commons.io.FilenameUtils;
import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;
import org.echocat.gradle.plugins.golang.vcs.VcsType;
import org.echocat.gradle.plugins.golang.vcs.VcsValidationException;
import org.gradle.api.artifacts.Dependency;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class GolangDependency implements Dependency {

    private String _group;
    private String _version;
    private URI _repositoryUri;
    private VcsType _repositoryType;
    private UpdatePolicy _updatePolicy = UpdatePolicy.defaultUpdatePolicy();

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
    public String getGroup() {
        return _group;
    }

    @Override
    public String getName() {
        return FilenameUtils.getName(getGroup());
    }

    @Override
    public String getVersion() {
        return _version;
    }

    public URI getRepositoryUri() {
        return _repositoryUri;
    }

    public VcsType getRepositoryType() {
        return _repositoryType;
    }

    public UpdatePolicy getUpdatePolicy() {
        return _updatePolicy;
    }

    public GolangDependency setGroup(String group) {
        _group = group;
        return this;
    }

    public GolangDependency setVersion(String version) {
        _version = version;
        return this;
    }

    public GolangDependency setRepositoryUri(URI repositoryUri) {
        _repositoryUri = repositoryUri;
        return this;
    }

    public GolangDependency setRepositoryType(VcsType repositoryType) {
        _repositoryType = repositoryType;
        return this;
    }

    public GolangDependency setUpdatePolicy(UpdatePolicy updatePolicy) {
        _updatePolicy = updatePolicy;
        return this;
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        if (!(dependency instanceof GolangDependency)) {
            return false;
        }
        return equals(dependency);
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
            Objects.equals(getUpdatePolicy(), that.getUpdatePolicy());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroup(), getVersion(), getRepositoryUri(), getRepositoryType(), getUpdatePolicy());
    }

    @Override
    public GolangDependency copy() {
        return new GolangDependency()
            .setGroup(getGroup())
            .setVersion(getVersion())
            .setRepositoryUri(getRepositoryUri())
            .setRepositoryType(getRepositoryType())
            .setUpdatePolicy(getUpdatePolicy())
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


}
