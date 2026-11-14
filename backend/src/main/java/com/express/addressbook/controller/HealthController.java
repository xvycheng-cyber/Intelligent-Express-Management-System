package com.express.addressbook.controller;

import com.express.addressbook.common.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Web 健康检查。只表示 Web 服务可用，不检查数据库连接。
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public R<Map<String, String>> health() {
        return R.ok(Map.of("status", "UP"));
    }
}
