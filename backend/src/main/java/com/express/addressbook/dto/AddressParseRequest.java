package com.express.addressbook.dto;

/**
 * POST /api/addresses/parse 请求。null/空/上限由 ParseInputGuard 统一校验，
 * 以便数量超限优先返回 40003（不用 @NotBlank 抢先返回 40001）。
 */
public record AddressParseRequest(String rawText) {
}
