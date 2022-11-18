package mercury.common;

import org.apache.commons.lang.RandomStringUtils;

import com.google.common.base.Strings;

public class Utils {

    public static String randomEmail(String prefix) {
        String randomEmail = RandomStringUtils.random(10, "abcdefghijklmnopqrstuvwxyz0123456789_");
        return (Strings.isNullOrEmpty(prefix) ? "" : prefix) + "citrus_" + randomEmail + "@dev.dev";
    }

    public static String randomEmail() {
        return randomEmail("");
    }
}
