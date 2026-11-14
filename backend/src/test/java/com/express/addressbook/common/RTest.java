package com.express.addressbook.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {

    @Test
    void okShouldWrapDataWithSuccessCode() {
        R<String> result = R.ok("hello");
        assertThat(result.code()).isZero();
        assertThat(result.message()).isEqualTo("OK");
        assertThat(result.data()).isEqualTo("hello");
    }

    @Test
    void failShouldKeepCodeAndMessage() {
        R<Void> result = R.fail(40401, "记录不存在或已被删除");
        assertThat(result.code()).isEqualTo(40401);
        assertThat(result.message()).isEqualTo("记录不存在或已被删除");
        assertThat(result.data()).isNull();
    }
}
