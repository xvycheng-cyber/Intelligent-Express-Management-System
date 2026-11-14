package com.express.addressbook.parser;

/**
 * 解析行错误，对应 API 的 items[].errors[]。
 */
public record LineError(String code, String field, String message) {

    public static LineError of(ParserError error) {
        return error.toLineError();
    }
}
