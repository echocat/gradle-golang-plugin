package org.echocat.gradle.plugins.golang.model;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.split;
import static org.echocat.gradle.plugins.golang.model.Architecture.currentArchitecture;

public class Platform {

    private static final Pattern EXTRACT_PATTERN = compile("([a-zA-Z0-9]+)-([a-zA-Z0-9]+)");
    private static final Platform CURRENT = new Platform(OperatingSystem.currentOperatingSystem(), currentArchitecture());

    private final OperatingSystem _operatingSystem;
    private final Architecture _architecture;

    public Platform(OperatingSystem operatingSystem, Architecture architecture) {
        _operatingSystem = operatingSystem;
        _architecture = architecture;
    }

    public OperatingSystem getOperatingSystem() {
        return _operatingSystem;
    }

    public Architecture getArchitecture() {
        return _architecture;
    }

    public String getNameInGo() {
        return getOperatingSystem().getNameInGo() + "-" + getArchitecture().getNameInGo();
    }

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

    public static Platform currentPlatform() {
        return CURRENT;
    }

    public static Platform resolveForGo(String plain) throws IllegalArgumentException {
        final Matcher matcher = EXTRACT_PATTERN.matcher(plain);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal platform provided: " + plain);
        }
        return resolveForGo(matcher.group(1), matcher.group(2));
    }

    public static Platform resolveForGo(String goOSName, String goArchName) throws IllegalArgumentException {
        return new Platform(OperatingSystem.resolveForGo(goOSName), Architecture.resolveForGo(goArchName));
    }

    public static Platform resolveForJava(String javaOSName, String javaArchName) throws IllegalArgumentException {
        return new Platform(OperatingSystem.resolveForJava(javaOSName), Architecture.resolveForJava(javaArchName));
    }

}
