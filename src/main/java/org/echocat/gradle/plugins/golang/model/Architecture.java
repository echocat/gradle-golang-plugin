package org.echocat.gradle.plugins.golang.model;

public enum Architecture {
    X86("386", "x86"),
    X86_64("amd64", "x86_64"),
    AMD64("amd64", "amd64"),
    ARM("arm", "arm"),
    PPC64("ppc64", null),
    PPC64LE("ppc64le", null),
    MIPS64("mips64", null),
    MIPS64LE("mips64le", null);

    private static final Architecture CURRENT = resolveForJava(System.getProperty("os.arch", "unknown"));

    private final String _nameInGo;
    private final String _nameInJava;

    Architecture(String nameInGo, String nameInJava) {
        _nameInGo = nameInGo;
        _nameInJava = nameInJava;
    }

    public String getNameInGo() {
        return _nameInGo;
    }

    public String getNameInJava() {
        return _nameInJava;
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
                if (candidate._nameInJava != null && candidate._nameInJava.equalsIgnoreCase(javaArchName)) {
                    return candidate;
                }
            }
        }
        throw new IllegalArgumentException("Illegal Java architecture: " + javaArchName);
    }

    public static Architecture currentArchitecture() {
        return CURRENT;
    }

}
