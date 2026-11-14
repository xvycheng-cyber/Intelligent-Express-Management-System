package com.express.addressbook.parser.strategy;

import com.express.addressbook.parser.ParseResult;

/**
 * P2 扩展点：AI 辅助解析策略。MVP 只启用规则解析，不注册任何 AI 实现。
 */
public interface AddressParseStrategy {

    String name();

    ParseResult parse(String rawText);
}
