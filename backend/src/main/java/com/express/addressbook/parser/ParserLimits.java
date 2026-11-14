package com.express.addressbook.parser;

/**
 * 解析器长度/容量常量（PR-08、PR-13）。
 *
 * <p>姓名长度按 Unicode code point 计数（1～50），与 MySQL VARCHAR(50)、
 * 前端 Array.from 计数一致；不使用 UTF-16 code unit 的 String.length() 作为姓名长度。</p>
 *
 * <p>单行 500 与姓名/电话/地址长度由解析器标记；rawText 20000 与有效行 200
 * 由上层 Controller/Service 在调用解析器前拒绝（返回 40003），解析器本身不抛 HTTP 业务码。</p>
 */
public final class ParserLimits {

    public static final int NAME_MAX = 50;
    public static final int PHONE_MAX = 20;
    public static final int ADDRESS_MAX = 255;
    public static final int LINE_MAX = 500;
    public static final int MAX_EFFECTIVE_LINES = 200;
    public static final int RAW_TEXT_MAX = 20000;

    private ParserLimits() {
    }
}
