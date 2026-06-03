package com.healthydiet.system;

import com.healthydiet.system.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResultTests {

    @Test
    void successShouldUseUnifiedCodeAndMessage() {
        Result<String> result = Result.success("ok");

        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("ok", result.getData());
    }

    @Test
    void failShouldUseProvidedMessage() {
        Result<Void> result = Result.fail("参数错误");

        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
    }
}