package com.express.addressbook.parser;

import java.util.List;

/**
 * 姓名定位结果。[start, end) 是选中姓名在原文中的范围；
 * removalRanges 是同时应从地址中剔除的非法姓名候选范围（P0-1 回退修复）。
 */
record NameMatch(int start, int end, String value, boolean valid, ParserError error,
                 List<int[]> removalRanges) {

    NameMatch {
        removalRanges = removalRanges == null ? List.of() : List.copyOf(removalRanges);
    }

    static NameMatch valid(int start, int end, String value) {
        return new NameMatch(start, end, value, true, null, List.of());
    }

    static NameMatch invalid(int start, int end, String value, ParserError error) {
        return new NameMatch(start, end, value, false, error, List.of());
    }

    NameMatch withRemovalRanges(List<int[]> ranges) {
        return new NameMatch(start, end, value, valid, error, ranges);
    }
}
