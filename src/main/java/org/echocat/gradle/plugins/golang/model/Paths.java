package org.echocat.gradle.plugins.golang.model;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.nio.file.Path;
import java.util.*;

import static java.io.File.pathSeparatorChar;
import static java.util.Collections.unmodifiableCollection;
import static org.apache.commons.lang3.StringUtils.*;

@Immutable
public class Paths implements Iterable<Path> {

    @Nonnull
    private final Collection<Path> _paths;

    public Paths(@Nullable String paths) {
        final List<Path> result = new ArrayList<>();
        if (paths != null) {
            for (final String plainPath : split(paths, pathSeparatorChar)) {
                final String trimmedPlainPath = plainPath.trim();
                if (isNotEmpty(plainPath)) {
                    result.add(java.nio.file.Paths.get(trimmedPlainPath).toAbsolutePath());
                }
            }
        }
        _paths = unmodifiableCollection(result);
    }

    public Paths(@Nullable Path path) {
        this(path != null ? new  Path[]{path} : null);
    }

    public Paths(@Nullable Path... paths) {
        final List<Path> result = new ArrayList<>();
        if (paths != null) {
            for (final Path path : paths) {
                if (path != null) {
                    result.add(path);
                }
            }
        }
        _paths = unmodifiableCollection(result);
    }

    public Paths(@Nullable Iterable<? extends Path> paths) {
        final List<Path> result = paths instanceof Collection ? new ArrayList<Path>(((Collection<?>) paths).size()) : new ArrayList<Path>();
        if (paths != null) {
            for (final Path path : paths) {
                if (path != null) {
                    result.add(path);
                }
            }
        }
        _paths = unmodifiableCollection(result);
    }

    private Paths(@Nonnull List<Path> paths) {
        _paths = unmodifiableCollection(paths);
    }

    @Nullable
    public Path firstElement() {
        final Iterator<Path> i = iterator();
        if (!i.hasNext()) {
            return null;
        }
        return i.next();
    }

    @Nonnull
    public Paths resolve(@Nonnull String other) {
        final Collection<Path> originals = _paths;
        final List<Path> others = new ArrayList<>(originals.size());
        for (final Path path : originals) {
            others.add(path.resolve(other));
        }
        return new Paths(others);
    }

    @Nonnull
    public Paths resolve(@Nonnull Path other) {
        final Collection<Path> originals = _paths;
        final List<Path> others = new ArrayList<>(originals.size());
        for (final Path path : originals) {
            others.add(path.resolve(other));
        }
        return new Paths(others);
    }

    @Override
    public Iterator<Path> iterator() {
        return _paths.iterator();
    }

    public boolean isEmpty() {
        return _paths.isEmpty();
    }

    @Nonnegative
    public int size() {
        return _paths.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Paths paths = (Paths) o;
        return Objects.equals(_paths, paths._paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_paths);
    }

    @Nonnull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Path path : this) {
            if (sb.length() > 0) {
                sb.append(pathSeparatorChar);
            }
            sb.append(path.toString());
        }
        return sb.toString();
    }

    @Nonnull
    public String toAbsoluteString() {
        final StringBuilder sb = new StringBuilder();
        for (final Path path : this) {
            if (sb.length() > 0) {
                sb.append(pathSeparatorChar);
            }
            sb.append(path.toAbsolutePath().toString());
        }
        return sb.toString();
    }

}
