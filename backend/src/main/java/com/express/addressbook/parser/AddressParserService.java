package com.express.addressbook.parser;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 地址解析唯一入口（PR-10：纯逻辑，不访问数据库/网络/文件）。
 *
 * <p>输入多行文本，按“一行一条联系人”输出 {@link ParseResult}。解析失败只影响当前行，
 * 其余行继续解析；错误通过 {@link LineError} 返回，不抛 HTTP 业务码。</p>
 */
@Service
public class AddressParserService {

    private final LineSplitter lineSplitter = new LineSplitter();
    private final PhoneExtractor phoneExtractor = new PhoneExtractor();
    private final NameExtractor nameExtractor = new NameExtractor();
    private final AddressExtractor addressExtractor = new AddressExtractor();

    public ParseResult parse(String rawText) {
        if (rawText == null) {
            throw new IllegalArgumentException("rawText must not be null");
        }
        LineSplitter.SplitResult split = lineSplitter.split(rawText);
        List<ParsedItem> items = new ArrayList<>(split.lines().size());

        for (LineSplitter.Line line : split.lines()) {
            if (line.rawLine().length() > ParserLimits.LINE_MAX) {
                items.add(new ParsedItem(
                        line.lineNo(),
                        line.rawLine(),
                        null,
                        null,
                        null,
                        ParseStatus.INVALID,
                        List.of(ParserError.E110.toLineError())));
                continue;
            }
            items.add(parseLine(line.rawLine(), line.lineNo()));
        }

        int validCount = (int) items.stream().filter(ParsedItem::isValid).count();
        int invalidCount = items.size() - validCount;
        return new ParseResult(
                split.totalLines(),
                items.size(),
                split.skippedLines(),
                validCount,
                invalidCount,
                items);
    }

    private ParsedItem parseLine(String rawLine, int lineNo) {
        String matchCopy = TextNormalizer.toHalfWidth(rawLine);
        PhoneMatch phone = phoneExtractor.locate(rawLine, matchCopy);
        NameMatch name = nameExtractor.locate(rawLine, phone);

        List<LineError> errors = new ArrayList<>();
        // 按设计伪代码顺序：电话 → 姓名 → 地址。
        if (phone == null) {
            errors.add(ParserError.E103.toLineError());
        } else if (!phone.valid()) {
            errors.add(phone.error().toLineError());
        }

        if (name == null) {
            errors.add(ParserError.E102.toLineError());
        } else if (!name.valid()) {
            errors.add(name.error().toLineError());
        }

        String address = addressExtractor.extract(rawLine, name, phone);
        if (address == null) {
            errors.add(ParserError.E105.toLineError());
        } else if (address.length() > ParserLimits.ADDRESS_MAX) {
            errors.add(ParserError.E107.toLineError());
        }

        return new ParsedItem(
                lineNo,
                rawLine,
                name == null ? null : name.value(),
                phone == null ? null : phone.value(),
                address,
                errors.isEmpty() ? ParseStatus.VALID : ParseStatus.INVALID,
                errors);
    }
}
