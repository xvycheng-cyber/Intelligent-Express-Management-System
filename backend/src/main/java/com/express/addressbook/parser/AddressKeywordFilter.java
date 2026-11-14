package com.express.addressbook.parser;

import java.util.List;
import java.util.Locale;

/**
 * PR-04 的地址关键词排除：命中即不把该片段当姓名。
 */
final class AddressKeywordFilter {

    private static final List<String> CHINESE_KEYWORDS = List.of(
            "省", "市", "区", "县", "镇", "乡", "村", "街", "路", "巷", "弄", "号",
            "栋", "幢", "座", "单元", "楼", "层", "室", "苑", "园", "小区", "大厦",
            "广场", "公寓", "社区");

    private static final List<String> ENGLISH_KEYWORDS = List.of(
            "room", "building", "street", "road", "avenue", "floor", "apt", "suite",
            "unit", "block", "lane", "district", "province", "city",
            // 常见英文城市名；用于避免将 "Hangzhou" 这类地址片段当作姓名。
            "hangzhou", "beijing", "shanghai", "guangzhou", "shenzhen",
            "chengdu", "wuhan", "nanjing", "xian", "tianjin", "chongqing");

    private AddressKeywordFilter() {
    }

    static boolean containsAddressKeyword(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (String keyword : CHINESE_KEYWORDS) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        String lower = value.toLowerCase(Locale.ROOT);
        for (String keyword : ENGLISH_KEYWORDS) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
