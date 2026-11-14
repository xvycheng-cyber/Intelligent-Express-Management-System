package com.express.addressbook.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * 分隔符、空白与包裹符的纯文本工具。所有索引均基于原字符串，保证地址可原样切片。
 */
final class TextSegments {

    /** PR-06：中英文逗号、中英文分号、Tab；顿号与空格不作分隔符。 */
    static final String DELIMITERS = "，,;；\t";

    private static final String OPEN_WRAPPERS = "([{（【“‘\"'";
    private static final String CLOSE_WRAPPERS = ")]}）】”’\"'";

    private TextSegments() {
    }

    static boolean isDelimiter(char c) {
        return DELIMITERS.indexOf(c) >= 0;
    }

    static boolean isBlank(String value) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    static String trim(String value) {
        if (value == null) {
            return null;
        }
        int start = 0;
        int end = value.length();
        while (start < end && isWhitespace(value.charAt(start))) {
            start++;
        }
        while (end > start && isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }

    /**
     * 去掉首尾空白与成对/多余的包裹符，供姓名与电话候选使用。
     */
    static String stripWrappers(String value) {
        int[] bounds = contentBounds(value, 0, value.length());
        return value.substring(bounds[0], bounds[1]);
    }

    /**
     * 返回去除首尾空白与包裹符后的 [start, end) 区间。
     */
    static int[] contentBounds(String value, int start, int end) {
        start = trimLeft(value, start, end);
        end = trimRight(value, start, end);

        boolean changed = true;
        while (changed && start < end) {
            changed = false;

            if (start + 1 < end && isMatchingWrapper(value.charAt(start), value.charAt(end - 1))) {
                start++;
                end--;
                changed = true;
            } else if (isWrapper(value.charAt(start))) {
                start++;
                changed = true;
            } else if (isWrapper(value.charAt(end - 1))) {
                end--;
                changed = true;
            }

            start = trimLeft(value, start, end);
            end = trimRight(value, start, end);
        }
        return new int[]{start, end};
    }

    /**
     * 按分隔符切分 [from, to) 的子串，保留空段与原始索引。
     */
    static List<Segment> split(String value, int from, int to) {
        List<Segment> result = new ArrayList<>();
        int segmentStart = from;
        for (int i = from; i < to; i++) {
            if (isDelimiter(value.charAt(i))) {
                result.add(new Segment(value.substring(segmentStart, i), segmentStart, i));
                segmentStart = i + 1;
            }
        }
        result.add(new Segment(value.substring(segmentStart, to), segmentStart, to));
        return result;
    }

    /** 地址首尾 trim：空白 + 分隔符，循环执行。 */
    static String trimAddressBoundary(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && isAddressTrimChar(value.charAt(start))) {
            start++;
        }
        while (end > start && isAddressTrimChar(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }

    static int trimLeft(String value, int start, int end) {
        while (start < end && isWhitespace(value.charAt(start))) {
            start++;
        }
        return start;
    }

    static int trimRight(String value, int start, int end) {
        while (end > start && isWhitespace(value.charAt(end - 1))) {
            end--;
        }
        return end;
    }

    private static boolean isAddressTrimChar(char c) {
        return isWhitespace(c) || isDelimiter(c);
    }

    private static boolean isWrapper(char c) {
        return OPEN_WRAPPERS.indexOf(c) >= 0 || CLOSE_WRAPPERS.indexOf(c) >= 0;
    }

    static boolean isMatchingWrapper(char open, char close) {
        int index = OPEN_WRAPPERS.indexOf(open);
        return index >= 0 && index < CLOSE_WRAPPERS.length() && CLOSE_WRAPPERS.charAt(index) == close;
    }

    record Segment(String text, int start, int end) {
        boolean isBlank() {
            return TextSegments.isBlank(text);
        }
    }
}
