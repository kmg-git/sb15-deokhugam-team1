package com.example.sb15deokhugamteam1.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatusCode;

class ErrorResponseTest {

    @ParameterizedTest
    @CsvSource({
            "400, COMMON_01, 입력값이 올바르지 않습니다.",
            "503, COMMON_99, 서버에 문제가 발생했습니다.",
            "405, HTTP_405, Method Not Allowed",
            "499, HTTP_499, 요청을 처리할 수 없습니다."
    })
    void 스프링_상태를_공통_오류_응답으로_변환한다(int status, String code, String message) {
        // given: @CsvSource에서 HTTP 상태와 기대 응답을 전달한다.

        // when
        ErrorResponse response = ErrorResponse.fromHttpStatus(
                HttpStatusCode.valueOf(status), "TestException");

        // then
        assertEquals(status, response.getStatus());
        assertEquals(code, response.getCode());
        assertEquals(message, response.getMessage());
        assertEquals("TestException", response.getExceptionType());
        assertNull(response.getDetails());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void 도메인_예외의_발생_시각과_상세_정보를_유지한다() {
        // given
        BaseException exception = new BaseException(
                CommonErrorCode.INVALID_INPUT, Map.of("content", "required")) {};

        // when
        ErrorResponse response = ErrorResponse.of(exception);

        // then
        assertEquals(exception.getTimestamp(), response.getTimestamp());
        assertEquals(exception.getDetails(), response.getDetails());
        assertEquals(exception.getClass().getName(), response.getExceptionType());
        assertEquals("COMMON_01", response.getCode());
        assertEquals(400, response.getStatus());
    }
}
