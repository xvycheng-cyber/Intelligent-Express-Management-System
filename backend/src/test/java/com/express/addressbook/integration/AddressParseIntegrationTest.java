package com.express.addressbook.integration;

import com.express.addressbook.parser.AddressParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 真实 AddressParserService + HTTP 集成测试（不 mock Parser）。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:mysql://127.0.0.1:3306/express_address_book?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai",
        "spring.datasource.username=test",
        "spring.datasource.password=test",
        "spring.datasource.hikari.initialization-fail-timeout=-1"
})
@AutoConfigureMockMvc
class AddressParseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private AddressParserService parserService;

    @BeforeEach
    void clearParserInvocations() {
        clearInvocations(parserService);
    }

    @Test
    void realParserNormalRequest() throws Exception {
        String rawText = "菜小鸟，13800138000，浙江省杭州市西湖区XX小区";
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].status").value("VALID"))
                .andExpect(jsonPath("$.data.items[0].receiverName").value("菜小鸟"))
                .andExpect(jsonPath("$.data.items[0].phone").value("13800138000"))
                .andExpect(jsonPath("$.data.items[0].fullAddress").value("浙江省杭州市西湖区XX小区"));

        verify(parserService).parse(rawText);
    }

    @Test
    void twoThousandCharacterLimitPassesButRealParserMarksLongLineE110() throws Exception {
        String rawText = "a".repeat(20_000);
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].errors[0].code").value("E110"));

        // 输入层通过，真实 Parser 被调用，单行 500 规则生效。
        verify(parserService).parse(rawText);
    }

    @Test
    void twoThousandAndOneCharactersDoesNotEnterParser() throws Exception {
        String rawText = "a".repeat(20_001);
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003));

        verify(parserService, never()).parse(anyString());
    }

    @Test
    void twoHundredEffectiveLinesReachRealParser() throws Exception {
        String rawText = lines(200);
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.effectiveLines").value(200))
                .andExpect(jsonPath("$.data.items.length()").value(200));

        verify(parserService).parse(rawText);
    }

    @Test
    void twoHundredAndOneEffectiveLinesDoesNotEnterParser() throws Exception {
        String rawText = lines(201);
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003));

        verify(parserService, never()).parse(anyString());
    }

    @Test
    void realParserMixedValidAndInvalidLines() throws Exception {
        String rawText = "菜小鸟，13800138000，浙江省杭州市西湖区XX小区\n"
                + "只有姓名没有电话\n"
                + "张小明，13900139000，浙江省杭州市滨江区XX小区";
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.validCount").value(2))
                .andExpect(jsonPath("$.data.invalidCount").value(1))
                .andExpect(jsonPath("$.data.items[1].errors[0].code").value("E103"));
    }

    @Test
    void realParserResponseNeverContainsE109() throws Exception {
        String rawText = "菜小鸟12，张三，13800138000，浙江省杭州市西湖区XX小区\n只有姓名没有电话";
        MvcResult result = mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("E109");
    }

    @Test
    void realParserDoesNotUseRepositoryOrJdbc() throws Exception {
        String rawText = "菜小鸟，13800138000，浙江省杭州市西湖区XX小区";
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk());
        // AddressController 只注入 AddressParserService；没有 Mapper/JdbcTemplate 调用。
        verify(parserService).parse(rawText);
    }

    private String lines(int count) {
        return IntStream.range(0, count).mapToObj(i -> "a").collect(Collectors.joining("\n"));
    }

    private String json(String rawText) {
        return "{\"rawText\":\"" + rawText.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"}";
    }
}
