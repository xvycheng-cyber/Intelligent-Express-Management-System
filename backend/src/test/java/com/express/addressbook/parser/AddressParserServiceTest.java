package com.express.addressbook.parser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AddressParserServiceTest {

    private final AddressParserService service = new AddressParserService();

    @Test
    void tc001ChineseComma() {
        ParsedItem item = one("菜小鸟，136xxxx0000，浙江省杭州市西湖区湖滨小区12栋301室");
        assertValid(item, "菜小鸟", "136xxxx0000", "浙江省杭州市西湖区湖滨小区12栋301室");
    }

    @Test
    void tc002EnglishComma() {
        ParsedItem item = one("张小明,13900139000,浙江省杭州市滨江区某某小区5栋202室");
        assertValid(item, "张小明", "13900139000", "浙江省杭州市滨江区某某小区5栋202室");
    }

    @Test
    void tc003Tab() {
        ParsedItem item = one("李四\t13800138000\t湖北省武汉市江岸区沿江小区8栋602室");
        assertValid(item, "李四", "13800138000", "湖北省武汉市江岸区沿江小区8栋602室");
    }

    @Test
    void tc004ChineseSemicolon() {
        ParsedItem item = one("王五；13800138001；湖北省宜昌市西陵区XX路1号");
        assertValid(item, "王五", "13800138001", "湖北省宜昌市西陵区XX路1号");
    }

    @Test
    void tc005EnglishSemicolon() {
        ParsedItem item = one("王五;13800138001;湖北省宜昌市西陵区XX路1号");
        assertValid(item, "王五", "13800138001", "湖北省宜昌市西陵区XX路1号");
    }

    @Test
    void tc006PhoneFirst() {
        ParsedItem item = one("13800138000，王五，湖北省孝感市孝南区东苑小区15栋201室");
        assertValid(item, "王五", "13800138000", "湖北省孝感市孝南区东苑小区15栋201室");
    }

    @Test
    void tc007NameAddressPhone() {
        ParsedItem item = one("赵六，湖北省宜昌市西陵区XX路1号，13900139000");
        assertValid(item, "赵六", "13900139000", "湖北省宜昌市西陵区XX路1号");
    }

    @Test
    void tc008AddressNamePhone() {
        ParsedItem item = one("湖北省宜昌市西陵区XX路1号，赵六，13900139000");
        assertValid(item, "赵六", "13900139000", "湖北省宜昌市西陵区XX路1号");
    }

    @Test
    void tc009AddressContainsEnglishComma() {
        ParsedItem item = one("菜小鸟，浙江省杭州市,西湖区、XX路12号，13800138000");
        assertValid(item, "菜小鸟", "13800138000", "浙江省杭州市,西湖区、XX路12号");
    }

    @Test
    void tc010AddressContainsChineseListMark() {
        ParsedItem item = one("菜中鸟，13800138002，湖北省武汉市江岸区沿江路、解放大道12号");
        assertValid(item, "菜中鸟", "13800138002", "湖北省武汉市江岸区沿江路、解放大道12号");
    }

    @Test
    void tc011MaskedPhone() {
        ParsedItem item = one("菜小鸟，136xxxx0000，浙江省杭州市西湖区XX小区");
        assertValid(item, "菜小鸟", "136xxxx0000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc012MissingPhone() {
        ParsedItem item = one("只有姓名没有电话");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("只有姓名没有电话");
        assertThat(item.phone()).isNull();
        assertThat(item.fullAddress()).isNull();
        assertThat(codes(item)).containsExactly("E103", "E105");
    }

    @Test
    void tc013MissingName() {
        ParsedItem item = one("13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isNull();
        assertThat(item.phone()).isEqualTo("13800138000");
        assertThat(item.fullAddress()).isEqualTo("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E102");
    }

    @Test
    void tc014InvalidPhone() {
        ParsedItem item = one("菜小鸟，12345，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("菜小鸟");
        assertThat(item.phone()).isEqualTo("12345");
        assertThat(item.fullAddress()).isEqualTo("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E104");
    }

    @Test
    void tc015MissingAddress() {
        ParsedItem item = one("菜小鸟，13800138000，");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("菜小鸟");
        assertThat(item.phone()).isEqualTo("13800138000");
        assertThat(item.fullAddress()).isNull();
        assertThat(codes(item)).containsExactly("E105");
    }

    @Test
    void tc016PhoneWrappedInParentheses() {
        ParsedItem item = one("（13800138000）李四，湖北省武汉市江岸区沿江小区8栋602室");
        assertValid(item, "李四", "13800138000", "湖北省武汉市江岸区沿江小区8栋602室");
    }

    @Test
    void tc017Landline() {
        ParsedItem item = one("公司前台，0571-88888888，浙江省杭州市西湖区XX大厦8楼");
        assertValid(item, "公司前台", "057188888888", "浙江省杭州市西湖区XX大厦8楼");
    }

    @Test
    void tc018Plus86AndEnglishAddress() {
        ParsedItem item = one("John, +86 138-0013-8000, Room 301, Building 12, Hangzhou");
        assertValid(item, "John", "13800138000", "Room 301, Building 12, Hangzhou");
    }

    @Test
    void tc019EnglishName() {
        ParsedItem item = one("John Smith,13800138000,Room 301, Building 12, Hangzhou");
        assertValid(item, "John Smith", "13800138000", "Room 301, Building 12, Hangzhou");
    }

    @Test
    void bomIsStrippedBeforeParsing() {
        ParsedItem item = one("\uFEFF菜小鸟，13800138000，浙江省杭州市西湖区XX小区");
        assertValid(item, "菜小鸟", "13800138000", "浙江省杭州市西湖区XX小区");
        assertThat(item.rawLine()).doesNotStartWith("\uFEFF");
    }

    @Test
    void tc020BlankLineSkippedButLineNumberKept() {
        ParseResult result = service.parse("菜小鸟，136xxxx0000，浙江省杭州市西湖区湖滨小区12栋301室\n\n张小明,13900139000,浙江省杭州市滨江区某某小区5栋202室");
        assertThat(result.totalLines()).isEqualTo(3);
        assertThat(result.effectiveLines()).isEqualTo(2);
        assertThat(result.skippedLines()).isEqualTo(1);
        assertThat(result.validCount()).isEqualTo(2);
        assertThat(result.items()).extracting(ParsedItem::lineNo).containsExactly(1, 3);
    }

    @Test
    void tc021LeadingBlankLine() {
        ParsedItem item = one("\n菜小鸟，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.lineNo()).isEqualTo(2);
    }

    @Test
    void tc022FullWidthDigits() {
        ParsedItem item = one("菜小鸟，１３８００１３８０００，浙江省杭州市西湖区XX小区");
        assertValid(item, "菜小鸟", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc023NameWithDigits() {
        ParsedItem item = one("菜小鸟12，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("菜小鸟12");
        assertThat(codes(item)).containsExactly("E106");
    }

    @Test
    void tc024AddressTooLong() {
        String address = "浙".repeat(260);
        ParsedItem item = one("菜小鸟，13800138000，" + address);
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.fullAddress()).hasSize(260);
        assertThat(codes(item)).containsExactly("E107");
    }

    @Test
    void tc025PhoneTooLong() {
        ParsedItem item = one("菜小鸟，123456789012345678901，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.phone()).hasSize(21);
        assertThat(codes(item)).containsExactly("E108");
    }

    @Test
    void tc026SingleLineTooLong() {
        ParsedItem item = one("菜".repeat(501));
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isNull();
        assertThat(item.phone()).isNull();
        assertThat(item.fullAddress()).isNull();
        assertThat(codes(item)).containsExactly("E110");
    }

    @Test
    void tc027WhitespaceOnlyLineSkipped() {
        ParseResult result = service.parse("   \n菜小鸟，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(result.skippedLines()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).lineNo()).isEqualTo(2);
    }

    @Test
    void tc028OriginalPunctuationPreserved() {
        String address = "浙江省杭州市西湖区#12-3号（东门）";
        ParsedItem item = one("菜小鸟，13800138000，" + address);
        assertValid(item, "菜小鸟", "13800138000", address);
    }

    @Test
    void tc029TwoNumbersKeepsSecondInAddress() {
        ParsedItem item = one("菜小鸟，13800138000，13900139000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.VALID); // 第二个号码留在地址中，不报错
        assertThat(item.phone()).isEqualTo("13800138000");
        assertThat(item.fullAddress()).contains("13900139000");
    }

    @Test
    void tc030Idempotent() {
        String text = "菜小鸟，13800138000，浙江省杭州市西湖区XX小区\n张小明,13900139000,浙江省杭州市滨江区XX小区";
        assertThat(service.parse(text)).isEqualTo(service.parse(text));
    }

    @Test
    void tc031InvalidLineDoesNotAffectOthers() {
        ParseResult result = service.parse(
                "菜小鸟，13800138000，浙江省杭州市西湖区XX小区\n"
                        + "张小明，浙江省杭州市滨江区XX小区\n"
                        + "李小红，13700137000，上海市浦东新区XX路100号");
        assertThat(result.items()).hasSize(3);
        assertThat(result.validCount()).isEqualTo(2);
        assertThat(result.invalidCount()).isEqualTo(1);
        assertThat(result.items().get(1).errors()).extracting(LineError::code).contains("E103");
        assertThat(result.items().get(2).status()).isEqualTo(ParseStatus.VALID);
    }

    @Test
    void tc032EmptyTextProducesNoItems() {
        ParseResult result = service.parse("");
        assertThat(result.totalLines()).isEqualTo(1);
        assertThat(result.effectiveLines()).isZero();
        assertThat(result.skippedLines()).isEqualTo(1);
        assertThat(result.items()).isEmpty();
    }

    @Test
    void tc033SpaceSeparatedStillParses() {
        ParsedItem item = one("菜小鸟 13600136000 浙江省杭州市西湖区湖滨小区12栋301室");
        assertValid(item, "菜小鸟", "13600136000", "浙江省杭州市西湖区湖滨小区12栋301室");
    }

    @Test
    void tc034NoDelimiterStillParses() {
        ParsedItem item = one("菜小鸟13600136000浙江省杭州市西湖区湖滨小区12栋301室");
        assertValid(item, "菜小鸟", "13600136000", "浙江省杭州市西湖区湖滨小区12栋301室");
    }

    @Test
    void tc035SingleCharacterChineseName() {
        ParsedItem item = one("王，13800138000，浙江省杭州市西湖区某某小区");
        assertValid(item, "王", "13800138000", "浙江省杭州市西湖区某某小区");
    }

    @Test
    void tc036SingleCharacterNameWithLandline() {
        ParsedItem item = one("李，0571-88888888，浙江省杭州市西湖区XX大厦8楼");
        assertValid(item, "李", "057188888888", "浙江省杭州市西湖区XX大厦8楼");
    }

    @Test
    void tc037Name50Characters() {
        String name = "张".repeat(50);
        ParsedItem item = one(name + "，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.VALID);
        assertThat(item.receiverName()).hasSize(50).isEqualTo(name);
    }

    @Test
    void tc038Name51Characters() {
        String name = "张".repeat(51);
        ParsedItem item = one(name + "，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).hasSize(51);
        assertThat(codes(item)).containsExactly("E111");
    }

    @Test
    void tc039InvalidFirstNameFallsBackToValidName() {
        ParsedItem item = one("菜小鸟12，张三，13800138000，浙江省杭州市西湖区XX小区");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc039bInvalidFirstNameFallsBackWithEnglishComma() {
        ParsedItem item = one("菜小鸟12, 张三, 13800138000, 浙江省杭州市西湖区XX小区");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc039cInvalidFirstNameFallsBackWithLandline() {
        ParsedItem item = one("王五!，李四，0571-88888888，浙江省杭州市西湖区XX小区");
        assertValid(item, "李四", "057188888888", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc040ChineseParenthesesWithWhitespace() {
        ParsedItem item = one("（ 13800138000 ）李四，浙江省杭州市西湖区XX小区");
        assertValid(item, "李四", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc041EnglishParenthesesWithWhitespace() {
        ParsedItem item = one("( 13800138000 ) 李四，浙江省杭州市西湖区XX小区");
        assertValid(item, "李四", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc042MultipleSpacesAroundPhone() {
        ParsedItem item = one("（   13800138000   ）李四，浙江省杭州市西湖区XX小区");
        assertValid(item, "李四", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc043NormalParenthesesInAddressArePreserved() {
        ParsedItem item = one("菜小鸟，13800138000，浙江省杭州市（西湖区）XX路12号");
        assertValid(item, "菜小鸟", "13800138000", "浙江省杭州市（西湖区）XX路12号");
    }

    @Test
    void tc044Phone20DigitsIsE104() {
        ParsedItem item = one("菜小鸟，12345678901234567890，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.phone()).hasSize(20);
        assertThat(codes(item)).containsExactly("E104");
    }

    @Test
    void tc045Phone21DigitsIsE108() {
        ParsedItem item = one("菜小鸟，123456789012345678901，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.phone()).hasSize(21);
        assertThat(codes(item)).containsExactly("E108");
    }

    @Test
    void tc046Phone30DigitsIsE108() {
        ParsedItem item = one("菜小鸟，123456789012345678901234567890，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.phone()).hasSize(30);
        assertThat(codes(item)).containsExactly("E108");
    }

    @Test
    void tc047Phone31DigitsIsE108() {
        ParsedItem item = one("菜小鸟，1234567890123456789012345678901，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.phone()).hasSize(31);
        assertThat(codes(item)).containsExactly("E108");
    }

    @Test
    void tc048Phone50DigitsIsE108() {
        ParsedItem item = one("菜小鸟，" + "1234567890".repeat(5) + "，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.phone()).hasSize(50);
        assertThat(codes(item)).containsExactly("E108");
    }

    @Test
    void tc049Name50SupplementaryCodePoints() {
        String name = "𠀀".repeat(50);
        ParsedItem item = one(name + "，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.VALID);
        assertThat(item.receiverName().codePointCount(0, item.receiverName().length())).isEqualTo(50);
    }

    @Test
    void tc050Name51SupplementaryCodePointsIsE111() {
        String name = "𠀀".repeat(51);
        ParsedItem item = one(name + "，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName().codePointCount(0, item.receiverName().length())).isEqualTo(51);
        assertThat(codes(item)).containsExactly("E111");
    }

    @Test
    void tc051RoomIsNotANameAndAddressHasNoDoubleComma() {
        ParsedItem item = one("Room 301，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isNull();
        assertThat(item.fullAddress()).doesNotContain(",,").doesNotContain("，，");
        assertThat(item.fullAddress()).contains("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E102");
        assertThat(item.rawLine()).isEqualTo("Room 301，13800138000，浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc052HangzhouIsNotAName() {
        ParsedItem item = one("Hangzhou，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isNull();
        assertThat(codes(item)).containsExactly("E102");
    }

    @Test
    void tc053RoomWithEnglishCommaAddressIsClean() {
        ParsedItem item = one("Room 301,13800138000,浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isNull();
        assertThat(item.fullAddress()).doesNotContain(",,");
        assertThat(item.fullAddress()).contains("浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc054ParseNullThrowsIllegalArgumentException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.parse(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rawText");
    }

    @Test
    void tc055Address255IsAccepted() {
        String address = "浙".repeat(255);
        ParsedItem item = one("菜小鸟，13800138000，" + address);
        assertThat(item.status()).isEqualTo(ParseStatus.VALID);
        assertThat(item.fullAddress()).hasSize(255);
    }

    @Test
    void tc056Address256IsE107() {
        String address = "浙".repeat(256);
        ParsedItem item = one("菜小鸟，13800138000，" + address);
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.fullAddress()).hasSize(256);
        assertThat(codes(item)).containsExactly("E107");
    }

    @Test
    void tc057Line500IsNotE110() {
        String line = "张".repeat(50) + "，13800138000，" + "浙".repeat(437);
        assertThat(line.length()).isEqualTo(500);
        ParsedItem item = one(line);
        assertThat(codes(item)).contains("E107").doesNotContain("E110");
    }

    @Test
    void tc058Line501IsE110() {
        ParsedItem item = one("菜".repeat(501));
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(codes(item)).containsExactly("E110");
    }

    @Test
    void consecutiveDelimitersAreIgnored() {
        ParsedItem item = one("菜小鸟，，13800138000，，浙江省杭州市西湖区XX小区");
        assertValid(item, "菜小鸟", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void addressWithMultipleCommasIsNotTruncated() {
        ParsedItem item = one("菜小鸟，13800138000，浙江省杭州市,西湖区,XX路12号");
        assertValid(item, "菜小鸟", "13800138000", "浙江省杭州市,西湖区,XX路12号");
    }

    @Test
    void supplementaryHanNameIsHandledByJava17Regex() {
        ParsedItem item = one("𠀀，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.VALID);
        assertThat(item.receiverName()).isEqualTo("𠀀");
    }

    @Test
    void unpairedSurrogateDoesNotCrash() {
        ParsedItem item = one("张\uD800，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(codes(item)).containsExactly("E106");
    }

    @Test
    void englishNameWithMiddleNameAndApostrophe() {
        ParsedItem item = one("Mary-Jane O'Neil,13800138000,浙江省杭州市西湖区XX小区");
        assertValid(item, "Mary-Jane O'Neil", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void fullWidthEnglishNameUsesMatchCopyButKeepsOriginal() {
        ParsedItem item = one("Ｊｏｈｎ，13800138000，浙江省杭州市西湖区XX小区");
        assertValid(item, "Ｊｏｈｎ", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void twoHundredEffectiveLinesParse() {
        List<String> lines = IntStream.rangeClosed(1, 200)
                .mapToObj(i -> "王，13800138000，浙江省杭州市西湖区第" + i + "号")
                .toList();
        ParseResult result = service.parse(String.join("\n", lines));
        assertThat(result.effectiveLines()).isEqualTo(200);
        assertThat(result.validCount()).isEqualTo(200);
    }

    @Test
    void overTwoHundredLinesAreStillParsedAtParserLayer() {
        // PR-13：200 行上限由 Controller/Service 在调用前拒绝；Parser 本身只负责逐行解析。
        List<String> lines = IntStream.rangeClosed(1, 201)
                .mapToObj(i -> "王，13800138000，浙江省杭州市西湖区第" + i + "号")
                .toList();
        ParseResult result = service.parse(String.join("\n", lines));
        assertThat(result.effectiveLines()).isEqualTo(201);
        assertThat(result.validCount()).isEqualTo(201);
    }

    @Test
    void smithJohnIsNotCombinedAsOneName() {
        // 文档已知限制：不支持 "Smith, John" 这种“姓, 名”复合英文姓名；逗号仍按字段分隔处理。
        ParsedItem item = one("Smith, John, 13800138000, 浙江省杭州市西湖区XX小区");
        assertThat(item.receiverName()).isEqualTo("Smith");
        assertThat(item.fullAddress()).contains("John");
    }

    @Test
    void maximumRawTextAroundTwentyThousandCharacters() {
        String name = "张".repeat(6);
        String address = "浙江省杭州市西湖区" + "A".repeat(71);
        List<String> lines = IntStream.rangeClosed(1, 200)
                .mapToObj(i -> name + "，13800138000，" + address)
                .toList();
        String rawText = String.join("\n", lines);
        assertThat(rawText.length()).isEqualTo(19_999);

        ParseResult result = service.parse(rawText);
        assertThat(result.effectiveLines()).isEqualTo(200);
        assertThat(result.validCount()).isEqualTo(200);
    }

    @Test
    void rawTextOverTwentyThousandIsDeferredToApiLayer() {
        // PR-13：rawText >20000 由 Controller/Service 在调用前返回 40003；Parser 本身按行处理。
        String name = "张".repeat(6);
        String address = "浙江省杭州市西湖区" + "A".repeat(72);
        List<String> lines = IntStream.rangeClosed(1, 200)
                .mapToObj(i -> name + "，13800138000，" + address)
                .toList();
        String rawText = String.join("\n", lines);
        assertThat(rawText.length()).isGreaterThan(20_000);
        assertThat(service.parse(rawText).effectiveLines()).isEqualTo(200);
    }

    @Test
    void oneHundredCharacterValidLineIsAccepted() {
        String name = "张".repeat(20);
        String address = "浙江省杭州市西湖区" + "A".repeat(80);
        ParsedItem item = one(name + "，13800138000，" + address);
        assertThat(item.status()).isEqualTo(ParseStatus.VALID);
        assertThat(item.receiverName()).hasSize(20);
    }

    @Test
    void tc061ValidNameBeforeInvalidCandidateKeepsAddressClean() {
        ParsedItem item = one("张三，菜小鸟12，13800138000，浙江省杭州市西湖区XX小区");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc062ValidNameBeforeInvalidSpecialCandidateKeepsAddressClean() {
        ParsedItem item = one("张三，王五!，13800138000，浙江省杭州市西湖区XX小区");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc063InvalidFirstCandidateStillFallsBack() {
        ParsedItem item = one("菜小鸟12，张三，13800138000，浙江省杭州市西湖区XX小区");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc064AddressBeforePhoneIsPreserved() {
        ParsedItem item = one("张三，浙江省杭州市西湖区XX小区，13800138000");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区");
    }

    @Test
    void tc065AddressWithBuildingAndRoomIsPreserved() {
        ParsedItem item = one("张三，浙江省杭州市西湖区XX小区12栋301室，13800138000");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区12栋301室");
    }

    @Test
    void tc066AddressParenthesesAfterPhoneArePreserved() {
        ParsedItem item = one("张三，13800138000，浙江省杭州市西湖区XX小区（东门）");
        assertValid(item, "张三", "13800138000", "浙江省杭州市西湖区XX小区（东门）");
    }

    @Test
    void tc067AllInvalidCandidatesAreRemovedFromAddress() {
        ParsedItem item = one("菜小鸟12，王五!，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("菜小鸟12");
        assertThat(item.fullAddress()).isEqualTo("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E106");
    }

    @Test
    void tc068SingleInvalidCandidateWithAddress() {
        ParsedItem item = one("菜小鸟12，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("菜小鸟12");
        assertThat(item.fullAddress()).isEqualTo("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E106");
    }

    @Test
    void tc069InvalidCandidateMixedWithAddressKeyword() {
        ParsedItem item = one("菜小鸟12，浙江省杭州市西湖区XX小区，13800138000");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("菜小鸟12");
        assertThat(item.fullAddress()).isEqualTo("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E106");
    }

    @Test
    void tc070AllInvalidWithoutAddressKeepsInvalidAndNoAddress() {
        ParsedItem item = one("菜小鸟12，王五!，13800138000");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("菜小鸟12");
        assertThat(item.fullAddress()).isNull();
        assertThat(codes(item)).containsExactly("E106", "E105");
    }

    @Test
    void tc071AllInvalidWithAddressKeepsPartialAddress() {
        ParsedItem item = one("王五!，菜小鸟12，13800138000，浙江省杭州市西湖区XX小区");
        assertThat(item.status()).isEqualTo(ParseStatus.INVALID);
        assertThat(item.receiverName()).isEqualTo("王五!");
        assertThat(item.fullAddress()).isEqualTo("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E106");
    }

    @Test
    void tc060ErrorOrderIsPhoneNameAddress() {
        ParsedItem item = one("浙江省杭州市西湖区XX小区");
        assertThat(codes(item)).containsExactly("E103", "E102", "E105");
    }

    @Test
    void tc059E109IsNeverEmitted() {
        ParseResult result = service.parse("菜小鸟12，张三，13800138000，浙江省杭州市西湖区XX小区\n只有姓名没有电话");
        assertThat(result.items()).flatExtracting(ParsedItem::errors)
                .extracting(LineError::code)
                .doesNotContain("E109");
    }

    private ParsedItem one(String text) {
        ParseResult result = service.parse(text);
        assertThat(result.items()).hasSize(1);
        return result.items().get(0);
    }

    private void assertValid(ParsedItem item, String name, String phone, String address) {
        assertThat(item.status()).as("status: " + item.errors()).isEqualTo(ParseStatus.VALID);
        assertThat(item.receiverName()).isEqualTo(name);
        assertThat(item.phone()).isEqualTo(phone);
        assertThat(item.fullAddress()).isEqualTo(address);
        assertThat(item.errors()).isEmpty();
    }

    private List<String> codes(ParsedItem item) {
        List<String> codes = new ArrayList<>();
        item.errors().forEach(error -> codes.add(error.code()));
        return codes;
    }
}
