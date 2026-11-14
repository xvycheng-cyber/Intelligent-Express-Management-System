package com.express.addressbook.parser;

import java.util.regex.Pattern;

/**
 * PR-03 电话模式。所有模式都使用数字边界断言，避免从更长数字串中截取子串。
 */
final class PhonePatterns {

    private static final String SPACE_OR_HYPHEN = "[-\\s\\u3000]?";

    static final Pattern MOBILE = Pattern.compile(
            "(?<!\\d)(?:\\+?86" + SPACE_OR_HYPHEN + ")?" + "1[3-9]\\d{9}(?!\\d)");

    static final Pattern MASKED_MOBILE = Pattern.compile(
            "(?<!\\d)1[3-9]\\d[xX*]{4}\\d{4}(?!\\d)");

    static final Pattern LANDLINE = Pattern.compile(
            "(?<!\\d)(?:\\+?86" + SPACE_OR_HYPHEN + ")?" + "0\\d{2,3}" + SPACE_OR_HYPHEN
                    + "\\d{7,8}(?:-\\d{1,6})?(?!\\d)");

    private PhonePatterns() {
    }

    static boolean matchesAny(String compact) {
        return MOBILE.matcher(compact).matches()
                || MASKED_MOBILE.matcher(compact).matches()
                || LANDLINE.matcher(compact).matches();
    }
}
