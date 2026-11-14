package com.express.addressbook.parser;

/**
 * PR-11：只用于匹配与电话展示的等长字符规范化。
 */
final class TextNormalizer {

    private TextNormalizer() {
    }

    static String toHalfWidth(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= '０' && c <= '９') {
                chars[i] = (char) ('0' + (c - '０'));
            } else if (c >= 'Ａ' && c <= 'Ｚ') {
                chars[i] = (char) ('A' + (c - 'Ａ'));
            } else if (c >= 'ａ' && c <= 'ｚ') {
                chars[i] = (char) ('a' + (c - 'ａ'));
            } else if (c == '＋') {
                chars[i] = '+';
            } else if (c == '－') {
                chars[i] = '-';
            } else if (c == '＊') {
                chars[i] = '*';
            }
        }
        return new String(chars);
    }

    static String compactPhone(String value) {
        String halfWidth = TextSegments.stripWrappers(toHalfWidth(value));
        StringBuilder builder = new StringBuilder(halfWidth.length());
        for (int i = 0; i < halfWidth.length(); i++) {
            char c = halfWidth.charAt(i);
            if (c == '-' || TextSegments.isWhitespace(c)) {
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    static boolean containsHan(String value) {
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN) {
                return true;
            }
            i += Character.charCount(codePoint);
        }
        return false;
    }
}
