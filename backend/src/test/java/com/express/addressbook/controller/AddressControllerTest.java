package com.express.addressbook.controller;

import com.express.addressbook.parser.AddressParserService;
import com.express.addressbook.service.AddressService;
import com.express.addressbook.parser.ParseResult;
import com.express.addressbook.parser.ParseStatus;
import com.express.addressbook.parser.ParsedItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AddressController.class)
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressParserService parserService;

    @MockBean
    private AddressService addressService;

    @Test
    void tcApiParse001NormalRequest() throws Exception {
        String rawText = "菜小鸟，13800138000，浙江省杭州市西湖区某某小区";
        ParsedItem item = new ParsedItem(
                1, rawText, "菜小鸟", "13800138000",
                "浙江省杭州市西湖区某某小区", ParseStatus.VALID, List.of());
        when(parserService.parse(rawText))
                .thenReturn(new ParseResult(1, 1, 0, 1, 0, List.of(item)));

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rawText\":\"" + rawText + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].receiverName").value("菜小鸟"))
                .andExpect(jsonPath("$.data.items[0].phone").value("13800138000"))
                .andExpect(jsonPath("$.data.items[0].status").value("VALID"));

        verify(parserService).parse(rawText);
    }

    @Test
    void tcApiParse002EmptyText() throws Exception {
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rawText\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.data.violations[0].field").value("rawText"));

        verifyNoInteractions(parserService);
    }

    @Test
    void tcApiParse003NullText() throws Exception {
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rawText\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.data.violations[0].field").value("rawText"));

        verifyNoInteractions(parserService);
    }

    @Test
    void tcApiParse004TwentyThousandCharactersBoundary() throws Exception {
        String rawText = "a".repeat(20_000);
        when(parserService.parse(rawText)).thenReturn(emptyResult());

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rawText\":\"" + rawText + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(parserService).parse(rawText);
    }

    @Test
    void tcApiParse005TwentyThousandAndOneCharacters() throws Exception {
        String rawText = "a".repeat(20_001);

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rawText\":\"" + rawText + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003))
                .andExpect(jsonPath("$.data.violations[0].field").value("rawText"));

        verifyNoInteractions(parserService);
    }

    @Test
    void tcApiParse006TwoHundredEffectiveLines() throws Exception {
        String rawText = lines(200, "\n");
        when(parserService.parse(rawText)).thenReturn(emptyResult());

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk());

        verify(parserService).parse(rawText);
    }

    @Test
    void tcApiParse007TwoHundredAndOneEffectiveLines() throws Exception {
        String rawText = lines(201, "\n");

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003));

        verifyNoInteractions(parserService);
    }

    @Test
    void tcApiParse008LimitResponseHasViolationField() throws Exception {
        String rawText = "a".repeat(20_001);

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003))
                .andExpect(jsonPath("$.data.violations[0].field").value("rawText"))
                .andExpect(jsonPath("$.data.violations[0].message").isNotEmpty());

        verifyNoInteractions(parserService);
    }

    @Test
    void tcApiParse009OverLimitDoesNotEnterParser() throws Exception {
        String tooLong = "a".repeat(20_001);
        String tooManyLines = lines(201, "\r\n");

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tooLong)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(tooManyLines)))
                .andExpect(status().isBadRequest());

        verify(parserService, never()).parse(anyString());
    }

    @Test
    void tcApiParse010ParseDoesNotWriteDatabase() throws Exception {
        String rawText = "菜小鸟，13800138000，浙江省杭州市西湖区某某小区";
        when(parserService.parse(rawText)).thenReturn(emptyResult());

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk());

        // 该切片只依赖 AddressParserService；没有 Repository/JdbcTemplate 注入。
        verify(parserService).parse(rawText);
    }

    @Test
    void limitTakesPrecedenceOverBlankValidation() throws Exception {
        String rawText = " ".repeat(20_001);

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003))
                .andExpect(jsonPath("$.data.violations[0].field").value("rawText"));

        verifyNoInteractions(parserService);
    }

    @Test
    void bomAndCrlfDoNotIncreaseEffectiveLineCount() throws Exception {
        String rawText = "\uFEFF" + lines(200, "\r\n");
        when(parserService.parse(rawText)).thenReturn(emptyResult());

        mockMvc.perform(post("/api/addresses/parse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(rawText)))
                .andExpect(status().isOk());

        verify(parserService).parse(rawText);
    }

    private ParseResult emptyResult() {
        return new ParseResult(0, 0, 0, 0, 0, List.of());
    }

    private String lines(int count, String separator) {
        return IntStream.range(0, count)
                .mapToObj(i -> "a")
                .collect(Collectors.joining(separator));
    }

    private String json(String rawText) {
        return "{\"rawText\":\"" + rawText.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"}";
    }
}
