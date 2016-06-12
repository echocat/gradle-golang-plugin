package org.echocat.gradle.plugins.golang.utils;

import java.util.Map;
import java.util.Map.Entry;

public class StringUtils {

    public static String expand(Map<String, String> match, String s) {
        String result = s;
        for (final Entry<String, String> entry : match.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

}
