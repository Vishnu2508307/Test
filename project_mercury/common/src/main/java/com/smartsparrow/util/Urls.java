package com.smartsparrow.util;

public class Urls {

    /**
     * Removes leading and trailing slashes if exist
     * @param str string
     * @return string without leading and trailing slashes
     */
    public static String stripSlash(String str) {
        if (str != null) {
            if (str.startsWith("/")) {
                str = str.substring(1);
            }
            if (str.endsWith("/")) {
                str = str.substring(0, str.length() - 1);
            }
        }
        return str;
    }

    /**
     * Concatenates two strings together with slash(/) between them. Before it removes leading and trailing slashes from provided strings.
     * @param str1 string1
     * @param str2 string2
     * @return string1/string2
     */
    public static String concat(String str1, String str2) {

        if (str1 == null || stripSlash(str1).isEmpty()) {
            return stripSlash(str2);
        } else {
            if (str2 == null || stripSlash(str2).isEmpty()) {
                return stripSlash(str1);
            } else {
                return stripSlash(str1) + "/" + stripSlash(str2);
            }
        }
    }
}
