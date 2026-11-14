package com.express.addressbook.parser;

/**
 * 电话定位结果。[start, end) 是原行中应从地址里剔除的范围。
 */
record PhoneMatch(int start, int end, String value, boolean valid, ParserError error) {

    static PhoneMatch valid(int start, int end, String value) {
        return new PhoneMatch(start, end, value, true, null);
    }

    static PhoneMatch invalid(int start, int end, String value, ParserError error) {
        return new PhoneMatch(start, end, value, false, error);
    }
}
