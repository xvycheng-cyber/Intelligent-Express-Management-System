package com.express.addressbook.parser;

/**
 * 行级错误码（E1xx）。E109 已删除，不再使用。
 */
public enum ParserError {

    E102("E102", "receiverName", "未识别到收件人姓名，请按\"姓名，电话，地址\"顺序填写"),
    E103("E103", "phone", "未识别到联系电话"),
    E104("E104", "phone", "联系电话格式不正确"),
    E105("E105", "fullAddress", "未识别到详细地址"),
    E106("E106", "receiverName", "收件人姓名不合法（不能含数字或特殊字符，长度 1～50）"),
    E107("E107", "fullAddress", "详细地址过长（最多 255 字）"),
    E108("E108", "phone", "联系电话过长（最多 20 位）"),
    E110("E110", "rawLine", "单行内容过长（最多 500 字符）"),
    E111("E111", "receiverName", "收件人姓名过长（最多 50 字）");

    private final String code;
    private final String field;
    private final String message;

    ParserError(String code, String field, String message) {
        this.code = code;
        this.field = field;
        this.message = message;
    }

    public String code() {
        return code;
    }

    public String field() {
        return field;
    }

    public String message() {
        return message;
    }

    public LineError toLineError() {
        return new LineError(code, field, message);
    }
}
