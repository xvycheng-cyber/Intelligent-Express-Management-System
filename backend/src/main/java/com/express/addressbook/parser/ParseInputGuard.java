package com.express.addressbook.parser;

import com.express.addressbook.common.BusinessException;
import com.express.addressbook.common.ResultCode;
import com.express.addressbook.common.Violation;

import java.util.List;
import java.util.Map;

/**
 * 解析 API 入参上限校验（PR-13）。
 *
 * <p>必须在调用 {@link AddressParserService#parse(String)} 之前执行；它只做长度/行数
 * 统计，不执行解析。数量超限优先于普通的 rawText 空值校验。</p>
 */
public final class ParseInputGuard {

    private ParseInputGuard() {
    }

    public static void validate(String rawText) {
        if (rawText == null) {
            throw new BusinessException(ResultCode.VALIDATION_FAILED.code(), "rawText 不能为空",
                    violations("rawText 不能为空"));
        }
        // 数量优先：即使内容同时为空/非法，先按超限返回 40003。
        if (rawText.length() > ParserLimits.RAW_TEXT_MAX) {
            throw new BusinessException(ResultCode.LIMIT_EXCEEDED.code(), "rawText 不能超过 20000 字符",
                    violations("rawText 不能超过 20000 字符"));
        }
        if (TextSegments.isBlank(rawText)) {
            throw new BusinessException(ResultCode.VALIDATION_FAILED.code(), "rawText 不能为空",
                    violations("rawText 不能为空"));
        }

        LineSplitter.SplitResult split = new LineSplitter().split(rawText);
        if (split.lines().size() > ParserLimits.MAX_EFFECTIVE_LINES) {
            throw new BusinessException(ResultCode.LIMIT_EXCEEDED.code(), "有效行不能超过 200 行",
                    violations("有效行不能超过 200 行"));
        }
    }

    private static Map<String, List<Violation>> violations(String message) {
        return Map.of("violations", List.of(new Violation("rawText", message)));
    }
}
