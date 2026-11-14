package com.express.addressbook.dto;

import com.express.addressbook.validation.CodePointLength;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 单条地址的新增/编辑入参。不接收 id、时间、isDeleted 等字段。
 */
public record AddressSaveDTO(

        @NotBlank(message = "收件人姓名不能为空")
        @CodePointLength(min = 1, max = 50, message = "收件人姓名长度必须为 1～50 个 Unicode 码点")
        @Pattern(regexp = "^(?:[\\p{IsHan}·]{1,50}|[A-Za-zＡ-Ｚａ-ｚ][A-Za-zＡ-Ｚａ-ｚ .'\\-．＇－]{0,49})$",
                message = "收件人姓名只能包含中文/英文姓名允许的字符")
        String receiverName,

        @NotBlank(message = "联系电话不能为空")
        @Size(max = 20, message = "联系电话最多 20 个字符")
        @Pattern(regexp = "^(?:1[3-9]\\d{9}|1[3-9]\\d[xX*]{4}\\d{4}|0\\d{2,3}\\d{7,8}(?:-?\\d{1,6})?|(?:\\+?86[\\s-]*)?1[3-9]\\d(?:[\\s-]?\\d){8}|[（(]?\\s*1[3-9]\\d(?:[\\s-]?\\d){8}\\s*[）)]?)$",
                message = "联系电话格式不正确")
        String phone,

        @NotBlank(message = "详细地址不能为空")
        @Size(max = 255, message = "详细地址最多 255 个字符")
        String fullAddress,

        Integer lineNo) {
}
