package com.express.addressbook.parser;

import java.util.regex.Pattern;

/**
 * PR-04 姓名模式：中文 1～50 个汉字/·；英文 1～50 个字符，至少 1 个字母。
 */
final class NamePatterns {

    static final Pattern CHINESE = Pattern.compile("^[\\p{IsHan}·]{1,50}$");

    static final Pattern ENGLISH = Pattern.compile("^[A-Za-z][A-Za-z .'\\-]{0,49}$");

    private NamePatterns() {
    }

    static boolean isValid(String value) {
        return CHINESE.matcher(value).matches() || ENGLISH.matcher(value).matches();
    }

    static boolean hasAsciiLetter(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                return true;
            }
        }
        return false;
    }
}
