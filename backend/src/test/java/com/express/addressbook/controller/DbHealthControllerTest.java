package com.express.addressbook.controller;

import com.express.addressbook.common.R;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DbHealthControllerTest {

    @Test
    void databaseShouldReturnUpWhenSelectOneSucceeds() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        R<Map<String, String>> result = new DbHealthController(jdbcTemplate).database();

        assertThat(result.code()).isZero();
        assertThat(result.data()).containsEntry("status", "UP");
    }
}
