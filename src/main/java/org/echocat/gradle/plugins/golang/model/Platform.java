package org.echocat.gradle.plugins.golang.model;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.split;
import static org.echocat.gradle.plugins.golang.model.Architecture.AMD64;
import static org.echocat.gradle.plugins.golang.model.Architecture.X86;
import static org.echocat.gradle.plugins.golang.model.Architecture.currentArchitecture;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.DARWIN;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.LINUX;
import static org.echocat.gradle.plugins.golang.model.OperatingSystem.WINDOWS;

@Immutable
public class Platform {

    @Nonnull
    public static final Platform LINUX_X86 = new Platform(LINUX, X86);
    @Nonnull
    public static final Platform LINUX_AMD64 = new Platform(LINUX, AMD64);
    @Nonnull
    public static final Platform WINDOWS_X86 = new Platform(WINDOWS, X86);
    @Nonnull
    public static final Platform WINDOWS_AMD64 = new Platform(WINDOWS, AMD64);
    @Nonnull
    public static final Platform DARWIN_X86 = new Platform(DARWIN, X86);
    @Nonnull
    public static final Platform DARWIN_AMD64 = new Platform(DARWIN, AMD64);

    @Nonnull
    private static final Pattern EXTRACT_PATTERN = compile("([a-zA-Z0-9]+)-([a-zA-Z0-9]+)");
    @Nonnull
    private static final Platform CURRENT = new Platform(OperatingSystem.currentOperatingSystem(), currentArchitecture());

    @Nonnull
    private final OperatingSystem _operatingSystem;
    @Nonnull
    private final Architecture _architecture;

    public Platform(@Nonnull OperatingSystem operatingSystem, @Nonnull Architecture architecture) {
        _operatingSystem = operatingSystem;
        _architecture = architecture;
    }

    @Nonnull
    public OperatingSystem getOperatingSystem() {
        return _operatingSystem;
    }

    @Nonnull
    public Architecture getArchitecture() {
        return _architecture;
    }

    @Nonnull
    public String getNameInGo() {
        return getOperatingSystem().getNameInGo() + "-" + getArchitecture().getNameInGo();
    }

    @Nonnull
    public String getNameInJava() {
        return getOperatingSystem().getNameInJava() + "-" + getArchitecture().getNameInJava();
    }

    @Override
    public String toString() {
        return getNameInGo();
    }

    @Nonnull
    public static List<Platform> toPlatforms(String plain) throws IllegalArgumentException {
        if (plain == null) {
            return emptyList();
        }
        final List<Platform> result = new ArrayList<>();
        for (final String candidate : split(plain, ',')) {
            final String trimmedCandidate = candidate.trim();
            if (!trimmedCandidate.isEmpty()) {
                result.add(Platform.resolveForGo(trimmedCandidate));
            }
        }
        return result.isEmpty() ? Collections.<Platform>emptyList() : unmodifiableList(result);
    }

    @Nonnull
    public static Platform currentPlatform() {
        return CURRENT;
    }

    @Nonnull
    public static Platform resolveForGo(String plain) throws IllegalArgumentException {
        final Matcher matcher = EXTRACT_PATTERN.matcher(plain);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal platform provided: " + plain);
        }
        return resolveForGo(matcher.group(1), matcher.group(2));
    }

    @Nonnull
    public static Platform platform(String goOSName, String goArchName) throws IllegalArgumentException {
        return resolveForGo(goOSName, goArchName);
    }

    @Nonnull
    public static Platform resolveForGo(String goOSName, String goArchName) throws IllegalArgumentException {
        return new Platform(OperatingSystem.resolveForGo(goOSName), Architecture.resolveForGo(goArchName));
    }

    @Nonnull
    public static Platform resolveForJava(String javaOSName, String javaArchName) throws IllegalArgumentException {
        return new Platform(OperatingSystem.resolveForJava(javaOSName), Architecture.resolveForJava(javaArchName));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Platform that = (Platform) o;
        return Objects.equals(getOperatingSystem(), that.getOperatingSystem()) &&
            Objects.equals(getArchitecture(), that.getArchitecture());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperatingSystem(), getArchitecture());
    }

}
