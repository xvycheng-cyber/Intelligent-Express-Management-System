package com.express.addressbook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchDeleteDTO(

        @NotEmpty(message = "请选择要删除的记录")
        @Size(max = 200, message = "单次最多删除 200 条")
        List<@NotBlank(message = "id 不能为空")
                @Pattern(regexp = "^[1-9]\\d*$", message = "id 必须是正整数") String> ids) {
}
