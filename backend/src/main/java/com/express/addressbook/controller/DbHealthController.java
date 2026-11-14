package com.express.addressbook.controller;

import com.express.addressbook.common.R;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 独立的数据库连接检查。与 /api/health 区分：/api/health 只表示 Web 服务可用。
 */
@RestController
@RequestMapping("/api/health")
public class DbHealthController {

    private final JdbcTemplate jdbcTemplate;

    public DbHealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db")
    public R<Map<String, String>> database() {
        Integer value = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return R.ok(Map.of("status", value != null && value == 1 ? "UP" : "DOWN"));
    }
}
