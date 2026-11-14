package com.express.addressbook.common;

/**
 * 字段校验错误项：data.violations[]。
 *
 * @param field   字段路径，如 phone / items[2].phone
 * @param message 面向用户的中文提示，不回显原始值
 * @param lineNo  可选，批量保存时回填原始行号
 */
public record Violation(String field, String message, Integer lineNo) {

    public Violation(String field, String message) {
        this(field, message, null);
    }
}
