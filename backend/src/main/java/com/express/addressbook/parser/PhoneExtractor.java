package com.express.addressbook.parser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PR-03：电话定位与规范化。输入为原始行与等长半角匹配副本。
 */
public final class PhoneExtractor {

    PhoneMatch locate(String rawLine, String matchCopy) {
        List<TextSegments.Segment> segments = TextSegments.split(rawLine, 0, rawLine.length());

        // 1) 整段优先：段内除首尾空白/包裹符外完全是一个电话
        for (TextSegments.Segment segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            int start = TextSegments.trimLeft(segment.text(), 0, segment.text().length());
            int end = TextSegments.trimRight(segment.text(), start, segment.text().length());
            if (start >= end) {
                continue;
            }
            String candidate = matchCopy.substring(segment.start() + start, segment.start() + end);
            String compact = TextNormalizer.compactPhone(candidate);
            if (PhonePatterns.matchesAny(compact)) {
                int rawStart = segment.start() + start;
                int rawEnd = segment.start() + end;
                return PhoneMatch.valid(rawStart, rawEnd, normalizePhone(rawLine.substring(rawStart, rawEnd)));
            }
        }

        // 2) 整行模式匹配：手机 → 脱敏手机 → 座机，取优先级最高且位置最靠前者
        PhoneMatch found = findFirst(matchCopy, PhonePatterns.MOBILE);
        if (found == null) {
            found = findFirst(matchCopy, PhonePatterns.MASKED_MOBILE);
        }
        if (found == null) {
            found = findFirst(matchCopy, PhonePatterns.LANDLINE);
        }
        if (found != null) {
            int start = found.start();
            int end = found.end();
            start = skipWhitespaceLeft(matchCopy, start);
            end = skipWhitespaceRight(matchCopy, end);
            if (start > 0 && end < matchCopy.length()
                    && TextSegments.isMatchingWrapper(matchCopy.charAt(start - 1), matchCopy.charAt(end))) {
                start--;
                end++;
            }
            return PhoneMatch.valid(start, end, normalizePhone(rawLine.substring(start, end)));
        }

        // 3) 纯数字段兜底：有候选但不合法时区分 E104/E108，而不是 E103
        for (TextSegments.Segment segment : segments) {
            if (segment.isBlank()) {
                continue;
            }
            int start = TextSegments.trimLeft(segment.text(), 0, segment.text().length());
            int end = TextSegments.trimRight(segment.text(), start, segment.text().length());
            String candidate = rawLine.substring(segment.start() + start, segment.start() + end);
            String digits = TextNormalizer.compactPhone(candidate);
            if (!digits.matches("\\d{5,}")) {
                continue;
            }
            ParserError error = digits.length() > ParserLimits.PHONE_MAX ? ParserError.E108 : ParserError.E104;
            int rawStart = segment.start() + start;
            int rawEnd = segment.start() + end;
            return PhoneMatch.invalid(rawStart, rawEnd, normalizePhone(candidate), error);
        }
        return null;
    }

    private PhoneMatch findFirst(String matchCopy, Pattern pattern) {
        Matcher matcher = pattern.matcher(matchCopy);
        if (!matcher.find()) {
            return null;
        }
        return new PhoneMatch(matcher.start(), matcher.end(), matchCopy.substring(matcher.start(), matcher.end()), true, null);
    }

    private int skipWhitespaceLeft(String value, int start) {
        while (start > 0 && TextSegments.isWhitespace(value.charAt(start - 1))) {
            start--;
        }
        return start;
    }

    private int skipWhitespaceRight(String value, int end) {
        while (end < value.length() && TextSegments.isWhitespace(value.charAt(end))) {
            end++;
        }
        return end;
    }

    /**
     * 输出级规范化：去包裹符/空白/短横，去 +86/86 前缀，全角转半角；脱敏 x 原样保留。
     */
    public static String normalizePhone(String value) {
        String halfWidth = TextNormalizer.toHalfWidth(TextSegments.stripWrappers(value));
        String compact = TextNormalizer.compactPhone(halfWidth);
        if (compact.startsWith("+86") && compact.length() > 3) {
            String tail = compact.substring(3);
            if (tail.matches("1[3-9]\\d{9}")) {
                return tail;
            }
        }
        if (compact.startsWith("86") && compact.length() > 2) {
            String tail = compact.substring(2);
            if (tail.matches("1[3-9]\\d{9}")) {
                return tail;
            }
        }
        return compact;
    }
}
