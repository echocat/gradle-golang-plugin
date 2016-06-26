package org.echocat.gradle.plugins.golang.utils;

import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FileUtils.readFileToString;

public class ImportScanner {

    private static final Pattern MULTI_IMPORT_STATEMENT = Pattern.compile("(?:^|;)\\s*import\\s*\\((?<body>[\\w\\d\\s.\\-_\\\\/\"]+)\\)");
    private static final Pattern PACKAGE_IN_MULTI_IMPORT = Pattern.compile("(?:^|;)\\s*(?<alias>\\w\\d.\\-_+\\s+)?\"(?<package>[\\w\\d\\s.\\-_\\\\/]+)\"");
    private static final Pattern SINGLE_IMPORT_STATEMENT = Pattern.compile("(?:^|;)\\s*import\\s*(?<alias>\\w\\d.\\-_+\\s+)?\"(?<package>[\\w\\d\\s.\\-_\\\\/]+)\"");

    @Nonnull
    public static Set<String> scan(@Nonnull File file) throws Exception {
        if (!"go".equals(FilenameUtils.getExtension(file.getPath()))) {
            throw new IllegalArgumentException("Illegal file provided. '" + file + "' does not have extension .go");
        }
        return scan(readFileToString(file));
    }

    @Nonnull
    public static Set<String> scan(@Nonnull String goSourceCode) throws Exception {
        final Set<String> result = new TreeSet<>();
        attachMultiImportStatementMatches(goSourceCode, result);
        attachSingleImportStatementMatches(goSourceCode, result);
        return result;
    }

    protected static void attachMultiImportStatementMatches(@Nonnull String goSourceCode, @Nonnull Collection<String> to) throws Exception {
        final Matcher matcher = MULTI_IMPORT_STATEMENT.matcher(goSourceCode);
        while (matcher.find()) {
            final String body = matcher.group("body");
            final Matcher bodyMatcher = PACKAGE_IN_MULTI_IMPORT.matcher(body);
            while (bodyMatcher.find()) {
                to.add(bodyMatcher.group("package"));
            }
        }
    }

    protected static void attachSingleImportStatementMatches(@Nonnull String goSourceCode, @Nonnull Collection<String> to) throws Exception {
        final Matcher matcher = SINGLE_IMPORT_STATEMENT.matcher(goSourceCode);
        while (matcher.find()) {
            to.add(matcher.group("package"));
        }
    }

}
