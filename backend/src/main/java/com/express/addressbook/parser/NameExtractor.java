package com.express.addressbook.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * PR-04：姓名候选定位。只返回位置与校验结论，不修改原行。
 */
final class NameExtractor {

    NameMatch locate(String rawLine, PhoneMatch phone) {
        int beforeEnd = phone == null ? rawLine.length() : phone.start();
        int afterStart = phone == null ? rawLine.length() : phone.end();

        List<TextSegments.Segment> before = TextSegments.split(rawLine, 0, beforeEnd);
        List<TextSegments.Segment> after = TextSegments.split(rawLine, afterStart, rawLine.length());

        List<TextSegments.Segment> candidates = new ArrayList<>();
        TextSegments.Segment firstBefore = firstNonBlank(before);
        TextSegments.Segment lastBefore = lastNonBlank(before);
        addIfDistinct(candidates, firstBefore);
        addIfDistinct(candidates, lastBefore);
        addIfDistinct(candidates, firstNonBlank(after));

        NameMatch selected = null;
        NameMatch firstInvalid = null;
        List<NameMatch> invalidMatches = new ArrayList<>();

        for (TextSegments.Segment segment : candidates) {
            NameMatch match = classify(rawLine, segment);
            if (match == null) {
                continue;
            }
            if (match.valid()) {
                if (selected == null) {
                    selected = match;
                }
            } else {
                if (firstInvalid == null) {
                    firstInvalid = match;
                }
                invalidMatches.add(match);
            }
        }

        if (selected != null) {
            // 选中姓名之后、电话之前的 E106 候选仍是明确非法姓名，不能残留到地址；
            // E111 可能是 260 字地址（无地址关键词），不能盲目删除。
            List<int[]> ranges = new ArrayList<>();
            for (NameMatch invalid : invalidMatches) {
                if (invalid.start() < selected.start() || invalid.error() == ParserError.E106) {
                    ranges.add(new int[]{invalid.start(), invalid.end()});
                }
            }
            return selected.withRemovalRanges(ranges);
        }
        if (firstInvalid != null) {
            // 没有任何合法姓名：所有非法名字候选都从地址中剔除，状态仍为 INVALID。
            List<int[]> ranges = new ArrayList<>();
            for (NameMatch invalid : invalidMatches) {
                ranges.add(new int[]{invalid.start(), invalid.end()});
            }
            return firstInvalid.withRemovalRanges(ranges);
        }
        return null;
    }

    private NameMatch classify(String rawLine, TextSegments.Segment segment) {
        int outerStart = TextSegments.trimLeft(rawLine, segment.start(), segment.end());
        int outerEnd = TextSegments.trimRight(rawLine, outerStart, segment.end());
        if (outerStart >= outerEnd) {
            return null;
        }

        int[] inner = TextSegments.contentBounds(rawLine, outerStart, outerEnd);
        String value = rawLine.substring(inner[0], inner[1]);
        if (value.isBlank()) {
            return null;
        }

        String lower = value.toLowerCase(Locale.ROOT);
        if (AddressKeywordFilter.containsAddressKeyword(value)
                || lower.contains("@")
                || lower.contains("://")
                || lower.contains("http")) {
            return null;
        }

        boolean hasHan = TextNormalizer.containsHan(value);
        boolean hasLetter = NamePatterns.hasAsciiLetter(TextNormalizer.toHalfWidth(value));
        if (!hasHan && !hasLetter) {
            return null;
        }

        if (value.codePointCount(0, value.length()) > ParserLimits.NAME_MAX) {
            return NameMatch.invalid(outerStart, outerEnd, value, ParserError.E111);
        }

        String normalized = TextNormalizer.toHalfWidth(value);
        if (NamePatterns.isValid(normalized)) {
            return NameMatch.valid(outerStart, outerEnd, value);
        }
        return NameMatch.invalid(outerStart, outerEnd, value, ParserError.E106);
    }

    private TextSegments.Segment firstNonBlank(List<TextSegments.Segment> segments) {
        for (TextSegments.Segment segment : segments) {
            if (!segment.isBlank()) {
                return segment;
            }
        }
        return null;
    }

    private TextSegments.Segment lastNonBlank(List<TextSegments.Segment> segments) {
        for (int i = segments.size() - 1; i >= 0; i--) {
            if (!segments.get(i).isBlank()) {
                return segments.get(i);
            }
        }
        return null;
    }

    private void addIfDistinct(List<TextSegments.Segment> candidates, TextSegments.Segment candidate) {
        if (candidate == null) {
            return;
        }
        for (TextSegments.Segment existing : candidates) {
            if (existing.start() == candidate.start() && existing.end() == candidate.end()) {
                return;
            }
        }
        candidates.add(candidate);
    }
}
