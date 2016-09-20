package org.echocat.gradle.plugins.golang.model;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public enum Architecture {
    X86("386", "x86"),
    AMD64("amd64", "amd64", "x86_64"),
    ARM("arm", "arm"),
    PPC64("ppc64", null),
    PPC64LE("ppc64le", null),
    MIPS64("mips64", null),
    MIPS64LE("mips64le", null);

    private static final Architecture CURRENT = resolveForJava(System.getProperty("os.arch", "unknown"));

    private final String _nameInGo;
    private final Set<String> _namesInJava;

    Architecture(String nameInGo, String nameInJava, String... otherNamesInJava) {
        _nameInGo = nameInGo;
        final Set<String> namesInJava = new LinkedHashSet<>();
        namesInJava.add(nameInJava);
        if (otherNamesInJava != null) {
            Collections.addAll(namesInJava, otherNamesInJava);
        }
        _namesInJava = Collections.unmodifiableSet(namesInJava);
    }

    @Nonnull
    public String getNameInGo() {
        return _nameInGo;
    }

    @Nonnull
    public String getNameInJava() {
        return _namesInJava.iterator().next();
    }

    @Nonnull
    public Set<String> getNamesInJava() {
        return _namesInJava;
    }

    @Override
    public String toString() {
        return getNameInGo();
    }

    public static Architecture resolveForGo(String goArchName) throws IllegalArgumentException {
        if (goArchName != null) {
            for (final Architecture candidate : values()) {
                if (candidate._nameInGo != null && candidate._nameInGo.equalsIgnoreCase(goArchName)) {
                    return candidate;
                }
            }
        }
        throw new IllegalArgumentException("Illegal go architecture: " + goArchName);
    }

    public static Architecture resolveForJava(String javaArchName) throws IllegalArgumentException {
        if (javaArchName != null) {
            for (final Architecture candidate : values()) {
                for (final String nameInJava : candidate._namesInJava) {
                    if (nameInJava.equalsIgnoreCase(javaArchName)) {
                        return candidate;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Illegal Java architecture: " + javaArchName);
    }

    public static Architecture currentArchitecture() {
        return CURRENT;
    }

}
