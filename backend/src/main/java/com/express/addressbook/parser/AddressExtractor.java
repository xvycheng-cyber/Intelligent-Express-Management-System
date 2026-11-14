package com.express.addressbook.parser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * PR-05：从原始行剔除姓名/电话区间，保留其余原文与内部标点。
 */
final class AddressExtractor {

    String extract(String rawLine, NameMatch name, PhoneMatch phone) {
        if (name == null && phone == null) {
            return null;
        }

        List<int[]> ranges = new ArrayList<>();
        if (name != null) {
            ranges.add(new int[]{name.start(), name.end()});
            for (int[] extra : name.removalRanges()) {
                ranges.add(new int[]{extra[0], extra[1]});
            }
        }
        if (phone != null) {
            ranges.add(new int[]{phone.start(), phone.end()});
        }
        ranges.sort(Comparator.comparingInt(range -> range[0]));

        List<int[]> merged = new ArrayList<>();
        for (int[] range : ranges) {
            if (merged.isEmpty() || range[0] > merged.get(merged.size() - 1)[1]) {
                merged.add(new int[]{range[0], range[1]});
            } else {
                int[] last = merged.get(merged.size() - 1);
                last[1] = Math.max(last[1], range[1]);
            }
        }

        StringBuilder remaining = new StringBuilder(rawLine.length());
        int cursor = 0;
        for (int[] range : merged) {
            if (cursor < range[0]) {
                remaining.append(rawLine, cursor, range[0]);
            }
            cursor = Math.max(cursor, range[1]);
            // 删除某个字段后若两侧都是分隔符，去掉重复的那个；不修改地址内部正常标点。
            if (cursor < rawLine.length() && remaining.length() > 0
                    && TextSegments.isDelimiter(remaining.charAt(remaining.length() - 1))
                    && TextSegments.isDelimiter(rawLine.charAt(cursor))) {
                cursor++;
            }
        }
        if (cursor < rawLine.length()) {
            remaining.append(rawLine, cursor, rawLine.length());
        }

        String address = TextSegments.trimAddressBoundary(remaining.toString());
        return address.isEmpty() ? null : address;
    }
}
