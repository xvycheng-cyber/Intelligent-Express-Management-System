package com.express.addressbook.integration;

import com.express.addressbook.dto.AddressSaveDTO;
import com.express.addressbook.service.AddressService;
import com.express.addressbook.vo.BatchSaveResult;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class AddressApiMysqlTest {

    private static final String PHONE_PREFIX = "199000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AddressService addressService;

    @BeforeEach
    void cleanBefore() {
        cleanOnlyTestRows();
    }

    @AfterEach
    void cleanAfter() {
        cleanOnlyTestRows();
    }

    private void cleanOnlyTestRows() {
        jdbcTemplate.update("DELETE FROM express_address WHERE phone LIKE ?", PHONE_PREFIX + "%");
    }

    @Test
    void createGetUpdateDeleteRoundTrip() throws Exception {
        long id = create("菜小鸟", PHONE_PREFIX + "00001", "浙江省杭州市西湖区XX小区");
        java.sql.Timestamp before = jdbcTemplate.queryForObject(
                "SELECT update_time FROM express_address WHERE id = ?", java.sql.Timestamp.class, id);

        mockMvc.perform(get("/api/addresses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(String.valueOf(id)))
                .andExpect(jsonPath("$.data.receiverName").value("菜小鸟"))
                .andExpect(jsonPath("$.data.phone").value(PHONE_PREFIX + "00001"))
                .andExpect(jsonPath("$.data.createTime").value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")))
                .andExpect(jsonPath("$.data.updateTime").value(matchesPattern("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")));

        Thread.sleep(1100);
        mockMvc.perform(put("/api/addresses/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dto("菜中鸟", PHONE_PREFIX + "00002", "湖北省武汉市江岸区XX小区")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(String.valueOf(id)));

        java.sql.Timestamp after = jdbcTemplate.queryForObject(
                "SELECT update_time FROM express_address WHERE id = ?", java.sql.Timestamp.class, id);
        assertThat(after).isAfter(before);

        mockMvc.perform(get("/api/addresses/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.receiverName").value("菜中鸟"))
                .andExpect(jsonPath("$.data.fullAddress").value("湖北省武汉市江岸区XX小区"));

        mockMvc.perform(delete("/api/addresses/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/addresses/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));

        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM express_address WHERE id = ?", Integer.class, id);
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    void pageSearchAndSort() throws Exception {
        long first = create("西湖甲", PHONE_PREFIX + "00011", "浙江省杭州市西湖区A小区");
        long second = create("West", PHONE_PREFIX + "00012", "浙江省杭州市滨江区B小区");
        long third = create("浦东丙", PHONE_PREFIX + "00013", "上海市浦东新区C小区");

        mockMvc.perform(get("/api/addresses").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.current").isNumber())
                .andExpect(jsonPath("$.data.size").isNumber())
                .andExpect(jsonPath("$.data.pages").isNumber())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.current").value(1))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.pages").value(2))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].id").isString())
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(third)));

        mockMvc.perform(get("/api/addresses").param("keyword", "西湖"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(first)));

        mockMvc.perform(get("/api/addresses").param("keyword", "West"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(second)));

        mockMvc.perform(get("/api/addresses").param("keyword", PHONE_PREFIX + "00012"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(second)));

        mockMvc.perform(get("/api/addresses").param("keyword", "浦东"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(third)));
    }

    @Test
    void likeSpecialCharactersAreLiteral() throws Exception {
        long percent = create("百分", PHONE_PREFIX + "00021", "浙江省杭州市%小区");
        long underscore = create("下划", PHONE_PREFIX + "00022", "浙江省杭州市_小区");
        long bang = create("感叹", PHONE_PREFIX + "00023", "浙江省杭州市!小区");
        long backslash = create("反斜", PHONE_PREFIX + "00024", "浙江省杭州市\\小区");

        assertKeywordOnly("百分", percent);
        assertKeywordOnly("下划", underscore);
        assertKeywordOnly("感叹", bang);
        assertKeywordOnly("反斜", backslash);

        // % / _ / ! / \ 都必须按字面匹配，不能匹配全表。
        mockMvc.perform(get("/api/addresses").param("keyword", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(percent)));
        mockMvc.perform(get("/api/addresses").param("keyword", "_"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(underscore)));
        mockMvc.perform(get("/api/addresses").param("keyword", "!"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(bang)));
        mockMvc.perform(get("/api/addresses").param("keyword", "\\"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(backslash)));
    }

    @Test
    void batchSaveAndBatchDelete() throws Exception {
        String body = """
                {"items":[
                  {"lineNo":1,"receiverName":"批量甲","phone":"19900000031","fullAddress":"浙江省杭州市A"},
                  {"lineNo":2,"receiverName":"批量乙","phone":"19900000032","fullAddress":"浙江省杭州市B"}
                ]}
                """;
        mockMvc.perform(post("/api/addresses/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.savedCount").value(2));

        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM express_address WHERE phone LIKE ? ORDER BY id", Long.class, PHONE_PREFIX + "%");
        assertThat(ids).hasSize(2);

        mockMvc.perform(post("/api/addresses/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + ids.get(0) + "\",\"" + ids.get(1) + "\",\"" + ids.get(1) + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedCount").value(2));

        Integer active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM express_address WHERE phone LIKE ? AND is_deleted = 0",
                Integer.class, PHONE_PREFIX + "%");
        assertThat(active).isZero();
    }

    @Test
    void batchSaveTransactionRollsBackOnDatabaseFailure() {
        AddressSaveDTO valid = new AddressSaveDTO(
                "事务甲", PHONE_PREFIX + "00041", "浙江省杭州市A", 1);
        AddressSaveDTO tooLong = new AddressSaveDTO(
                "事务乙", PHONE_PREFIX + "00042", "A".repeat(300), 2);

        assertThatThrownBy(() -> addressService.batchSave(List.of(valid, tooLong)))
                .isInstanceOf(RuntimeException.class);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM express_address WHERE phone LIKE ?",
                Integer.class, PHONE_PREFIX + "%");
        assertThat(count).isZero();
    }

    @Test
    void batchSave200AndEmptyRejected() throws Exception {
        String twoHundred = IntStream.range(0, 200)
                .mapToObj(i -> "{\"lineNo\":" + (i + 1)
                        + ",\"receiverName\":\"批量\""
                        + ",\"phone\":\"199000" + String.format("%05d", i)
                        + "\",\"fullAddress\":\"浙江省杭州市\"}")
                .collect(Collectors.joining(",", "{\"items\":[", "]}"));
        mockMvc.perform(post("/api/addresses/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(twoHundred))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.savedCount").value(200));

        Integer saved = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM express_address WHERE phone LIKE ? AND is_deleted = 0",
                Integer.class, PHONE_PREFIX + "%");
        assertThat(saved).isEqualTo(200);

        mockMvc.perform(post("/api/addresses/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));

        mockMvc.perform(post("/api/addresses/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void pageSizeValidationUsesStableFieldNames() throws Exception {
        mockMvc.perform(get("/api/addresses").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.data.violations[0].field").value("page"));
        mockMvc.perform(get("/api/addresses").param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.violations[0].field").value("page"));
        mockMvc.perform(get("/api/addresses").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.violations[0].field").value("size"));
        mockMvc.perform(get("/api/addresses").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.violations[0].field").value("size"));
        mockMvc.perform(get("/api/addresses").param("page", "1").param("size", "100"))
                .andExpect(status().isOk());
    }

    @Test
    void keywordLengthUsesCodePointsAfterTrim() throws Exception {
        String ascii50 = "a".repeat(50);
        String ascii51 = "a".repeat(51);
        String chinese50 = "西".repeat(50);
        String chinese51 = "西".repeat(51);
        String ext50 = "𠀀".repeat(50);
        String ext51 = "𠀀".repeat(51);

        mockMvc.perform(get("/api/addresses").param("keyword", ascii50))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/addresses").param("keyword", ascii51))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.data.violations[0].field").value("keyword"));
        mockMvc.perform(get("/api/addresses").param("keyword", chinese50))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/addresses").param("keyword", chinese51))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.violations[0].field").value("keyword"));
        mockMvc.perform(get("/api/addresses").param("keyword", "  " + ascii50 + "  "))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/addresses").param("keyword", ""))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/addresses").param("keyword", ext50))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/addresses").param("keyword", ext51))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.violations[0].field").value("keyword"));
    }

    @Test
    void unicodeNameBoundariesThroughRealController() throws Exception {
        long bmp50 = create("张".repeat(50), PHONE_PREFIX + "00081", "浙江省杭州市");
        long ext50 = create("𠀀".repeat(50), PHONE_PREFIX + "00082", "浙江省杭州市");

        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dto("张".repeat(51), PHONE_PREFIX + "00083", "浙江省杭州市")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dto("𠀀".repeat(51), PHONE_PREFIX + "00084", "浙江省杭州市")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));

        assertThat(bmp50).isPositive();
        assertThat(ext50).isPositive();
    }

    @Test
    void frontendStateFieldsAreIgnored() throws Exception {
        String body = """
                {"receiverName":"状态测试","phone":"19900000091","fullAddress":"浙江省杭州市",
                 "isEdited":true,"original":"旧值","rawLine":"原始","rawKey":"key",
                 "status":"VALID","errors":[{"code":"E999"}]}
                """;
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM express_address WHERE phone = ? AND receiver_name = ?",
                Integer.class, PHONE_PREFIX + "00091", "状态测试");
        assertThat(count).isEqualTo(1);
    }

    @Test
    void apiBatchRollbackOnControlledDatabaseFailure() throws Exception {
        createFailureTrigger();
        try {
            String body = """
                    {"items":[
                      {"receiverName":"回滚甲","phone":"19900000071","fullAddress":"浙江省杭州市A"},
                      {"receiverName":"回滚乙","phone":"19999999999","fullAddress":"浙江省杭州市B"}
                    ]}
                    """;
            MvcResult result = mockMvc.perform(post("/api/addresses/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().is5xxServerError())
                    .andExpect(jsonPath("$.code").value(50002))
                    .andExpect(jsonPath("$.message").value("数据库操作失败，请稍后重试"))
                    .andReturn();
            String content = result.getResponse().getContentAsString();
            assertThat(content).doesNotContain("controlled failure", "SQL", "SELECT", "INSERT",
                    "password", "Exception", "java.");

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM express_address WHERE phone IN (?, ?)",
                    Integer.class, PHONE_PREFIX + "00071", "19999999999");
            assertThat(count).isZero();
        } finally {
            dropFailureTrigger();
        }
    }

    @Test
    void databaseExceptionDoesNotLeakDetails() throws Exception {
        createFailureTrigger();
        try {
            MvcResult result = mockMvc.perform(post("/api/addresses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(dto("异常测试", "19999999999", "浙江省杭州市")))
                    .andExpect(status().is5xxServerError())
                    .andExpect(jsonPath("$.code").value(50002))
                    .andExpect(jsonPath("$.message").value("数据库操作失败，请稍后重试"))
                    .andReturn();
            String content = result.getResponse().getContentAsString();
            assertThat(content).doesNotContain("controlled failure", "SQL", "SELECT", "INSERT",
                    "password", "Exception", "java.");
        } finally {
            dropFailureTrigger();
        }
    }

    @Test
    void rawPhoneIsNormalizedOnSave() throws Exception {
        long id = create("电话规范", "+86 199-0000-0092", "浙江省杭州市");

        String stored = jdbcTemplate.queryForObject(
                "SELECT phone FROM express_address WHERE id = ?", String.class, id);
        assertThat(stored).isEqualTo("19900000092");
    }

    @Test
    void validationAndLimitResponses() throws Exception {
        mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dto("张".repeat(51), PHONE_PREFIX + "00051", "浙江省杭州市")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));

        String tooManyItems = IntStream.range(0, 201)
                .mapToObj(i -> "{\"receiverName\":\"批量" + i + "\",\"phone\":\"199000" + String.format("%05d", i)
                        + "\",\"fullAddress\":\"浙江省杭州市\"}")
                .collect(Collectors.joining(",", "{\"items\":[", "]}"));
        mockMvc.perform(post("/api/addresses/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tooManyItems))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003))
                .andExpect(jsonPath("$.data.violations[0].field").value("items"));

        String tooManyIds = IntStream.rangeClosed(1, 201)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining("\",\"", "{\"ids\":[\"", "\"]}"));
        mockMvc.perform(post("/api/addresses/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tooManyIds))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40003))
                .andExpect(jsonPath("$.data.violations[0].field").value("ids"));
    }

    @Test
    void batchDeleteAllMissingAndPartialMissing() throws Exception {
        mockMvc.perform(post("/api/addresses/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"99999999\"]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));

        long existing = create("部分", PHONE_PREFIX + "00061", "浙江省杭州市");
        mockMvc.perform(post("/api/addresses/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ids\":[\"" + existing + "\",\"99999999\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deletedCount").value(1));
    }

    @Test
    void missingRecordAndInvalidId() throws Exception {
        mockMvc.perform(get("/api/addresses/{id}", 99999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40401));
        mockMvc.perform(get("/api/addresses/{id}", 0))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(40001));
    }

    private void createFailureTrigger() {
        dropFailureTrigger();
        jdbcTemplate.execute("""
                CREATE TRIGGER ms031_fail_insert BEFORE INSERT ON express_address
                FOR EACH ROW
                BEGIN
                    IF NEW.phone = '19999999999' THEN
                        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'controlled failure';
                    END IF;
                END
                """);
    }

    private void dropFailureTrigger() {
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS ms031_fail_insert");
    }

    private void assertKeywordOnly(String keyword, long expectedId) throws Exception {
        mockMvc.perform(get("/api/addresses").param("keyword", keyword))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(String.valueOf(expectedId)));
    }

    private long create(String name, String phone, String address) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dto(name, phone, address)))
                .andExpect(status().isOk())
                .andReturn();
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
        return Long.parseLong(id);
    }

    private String dto(String name, String phone, String address) {
        return "{\"receiverName\":\"" + escape(name) + "\",\"phone\":\"" + escape(phone)
                + "\",\"fullAddress\":\"" + escape(address) + "\"}";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
