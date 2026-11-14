package com.express.addressbook.parser;

import java.util.List;

/**
 * 一次解析的汇总结果，对应 API 的 data 对象。
 */
public record ParseResult(
        int totalLines,
        int effectiveLines,
        int skippedLines,
        int validCount,
        int invalidCount,
        List<ParsedItem> items) {

    public ParseResult {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
