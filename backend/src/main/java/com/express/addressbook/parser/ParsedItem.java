package com.express.addressbook.parser;

import java.util.List;

/**
 * 单行解析结果。
 *
 * @param lineNo      原始行号（1-based，空行也占位）
 * @param rawLine     原始行文本（未 trim）
 * @param receiverName 姓名；失败时可能为部分结果或 null
 * @param phone       电话（展示级规范化）；失败时可能为候选值或 null
 * @param fullAddress 地址（保留内部标点）；失败时可能为 null
 * @param status      VALID / INVALID
 * @param errors      行级错误；VALID 时为空
 */
public record ParsedItem(
        int lineNo,
        String rawLine,
        String receiverName,
        String phone,
        String fullAddress,
        ParseStatus status,
        List<LineError> errors) {

    public ParsedItem {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public boolean isValid() {
        return status == ParseStatus.VALID;
    }
}
