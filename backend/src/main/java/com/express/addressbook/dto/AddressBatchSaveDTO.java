package com.express.addressbook.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AddressBatchSaveDTO(

        @NotEmpty(message = "没有可保存的记录")
        @Size(max = 200, message = "单次最多保存 200 条")
        List<@NotNull(message = "记录不能为空") @Valid AddressSaveDTO> items) {
}
