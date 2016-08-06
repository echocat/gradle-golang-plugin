package org.echocat.gradle.plugins.golang.model;

import static org.echocat.gradle.plugins.golang.model.PackageFormat.TGZ;
import static org.echocat.gradle.plugins.golang.model.PackageFormat.ZIP;
import static java.util.Locale.US;

public enum OperatingSystem {
    WINDOWS("windows", "windows", ZIP),
    LINUX("linux", "linux", TGZ),
    DARWIN("darwin", "mac os x", TGZ),
    FREEBSD("freebsd", "freebsd", TGZ),
    OPENBSD("openbsd", "openbsd", TGZ),
    NETBSD("netbsd", "netbsd", TGZ),
    SOLARIS("solaris", "solaris", TGZ),
    DRAGONFLY("dragonfly", null, TGZ),
    PLAN9("plan9", null, TGZ);

    private static final OperatingSystem CURRENT = resolveForJava(System.getProperty("os.name", "unknown"));

    private final String _nameInGo;
    private final String _nameInJava;
    private final PackageFormat _goPackageFormat;

    OperatingSystem(String nameInGo, String nameInJava, PackageFormat goPackageFormat) {
        _nameInGo = nameInGo;
        _nameInJava = nameInJava;
        _goPackageFormat = goPackageFormat;
    }

    public String getNameInGo() {
        return _nameInGo;
    }

    public String getNameInJava() {
        return _nameInJava;
    }

    public PackageFormat getGoPackageFormat() {
        return _goPackageFormat;
    }

    @Override
    public String toString() {
        return getNameInGo();
    }

    public static OperatingSystem resolveForGo(String goArchName) throws IllegalArgumentException {
        if (goArchName != null) {
            for (final OperatingSystem candidate : values()) {
                if (candidate._nameInGo != null && goArchName.equalsIgnoreCase(candidate._nameInGo)) {
                    return candidate;
                }
            }
        }
        throw new IllegalArgumentException("Illegal go operating system: " + goArchName);
    }

    public static OperatingSystem resolveForJava(String javaArchName) throws IllegalArgumentException {
        if (javaArchName != null) {
            final String asLower = javaArchName.toLowerCase(US).trim();
            for (final OperatingSystem candidate : values()) {
                if (candidate._nameInJava != null && asLower.startsWith(candidate._nameInJava)) {
                    return candidate;
                }
            }
        }
        throw new IllegalArgumentException("Illegal Java operating system: " + javaArchName);
    }

    public static OperatingSystem currentOperatingSystem() {
        return CURRENT;
    }
}
