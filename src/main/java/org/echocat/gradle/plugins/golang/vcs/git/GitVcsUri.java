package org.echocat.gradle.plugins.golang.vcs.git;

import org.echocat.gradle.plugins.golang.vcs.VcsReference;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;
import java.util.regex.Matcher;

import static java.net.URI.create;

public class GitVcsUri {

    @Nonnull
    public static GitVcsUri gitVcsUriFor(@Nonnull VcsReference reference) {
        return gitVcsUriFor(reference.getUri());
    }

    @Nonnull
    public static GitVcsUri gitVcsUriFor(@Nonnull URI uri) {
        final Matcher matcher = GitVcsRepository.SPLIT_PATH_PATTERN.matcher(uri.toString());
        if (matcher.matches()) {
            return new GitVcsUri(create(matcher.group("root")), matcher.group("subPath"));
        }
        return new GitVcsUri(uri, "");
    }

    @Nonnull
    private final URI _uri;
    @Nonnull
    private final String _subPath;

    public GitVcsUri(@Nonnull URI uri, @Nonnull String subPath) {
        _uri = uri;
        _subPath = subPath;
    }

    @Nonnull
    public URI getUri() {
        return _uri;
    }

    @Nonnull
    public String getSubPath() {
        return _subPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final GitVcsUri that = (GitVcsUri) o;
        return Objects.equals(_uri, that._uri) &&
            Objects.equals(_subPath, that._subPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_uri, _subPath);
    }

    @Override
    public String toString() {
        return _uri + _subPath;
    }
}
