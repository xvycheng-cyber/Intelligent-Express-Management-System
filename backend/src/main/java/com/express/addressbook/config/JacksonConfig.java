package com.express.addressbook.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 只将包装类型 Long（主要是实体 ID）序列化为字符串，避免前端 JS 精度丢失。
 * 基本类型 long 保持 JSON 数字，供 PageResult 的 total/current/size/pages 使用。
 * 时间格式由 application.yml 的 spring.jackson.date-format 配置。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance);
    }
}
