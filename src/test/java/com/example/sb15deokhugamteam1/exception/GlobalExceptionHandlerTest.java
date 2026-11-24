package com.example.sb15deokhugamteam1.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.sb15deokhugamteam1.exception.user.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class GlobalExceptionHandlerTest {
    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ExceptionTestController(validator))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void 도메인_예외는_정의된_상태와_상세_정보를_반환한다() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        // when
        mockMvc.perform(get("/test/users/{id}", userId))
                // then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("USER_02"))
                .andExpect(jsonPath("$.details.userId").value(userId.toString()))
                .andExpect(jsonPath("$.exceptionType").value(UserNotFoundException.class.getName()));
    }

    @Test
    void 요청_본문_검증_실패는_필드_오류와_400을_반환한다() throws Exception {
        // given
        var request = post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"content": ""}
                        """);

        // when
        mockMvc.perform(request)
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"))
                .andExpect(jsonPath("$.details.content").value("내용은 필수입니다."))
                .andExpect(jsonPath("$.exceptionType").value("MethodArgumentNotValidException"));
    }

    @Test
    void 제약조건_위반은_속성_오류와_400을_반환한다() throws Exception {
        // given: 테스트 컨트롤러에서 제약조건 위반을 발생시킨다.

        // when
        mockMvc.perform(get("/test/constraint"))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_01"))
                .andExpect(jsonPath("$.details.content").value("내용은 필수입니다."))
                .andExpect(jsonPath("$.exceptionType").value(ConstraintViolationException.class.getName()));
    }

    @Test
    void 지원하지_않는_HTTP_메서드는_405와_Allow_헤더를_반환한다() throws Exception {
        // given: /test/validate는 POST만 지원한다.

        // when
        mockMvc.perform(get("/test/validate"))
                // then
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", "POST"))
                .andExpect(jsonPath("$.code").value("HTTP_405"))
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void 업로드_크기_제한을_초과하면_413을_반환한다() throws Exception {
        // given: 테스트 컨트롤러에서 업로드 크기 초과 예외를 발생시킨다.

        // when
        mockMvc.perform(post("/test/upload"))
                // then
                .andExpect(status().is(413))
                .andExpect(jsonPath("$.code").value("HTTP_413"))
                .andExpect(jsonPath("$.status").value(413))
                .andExpect(jsonPath("$.exceptionType").value(MaxUploadSizeExceededException.class.getName()));
    }

    @Test
    void 스프링_MVC_변환_오류는_공통_500_응답을_반환한다() throws Exception {
        // given: 테스트 컨트롤러에서 타입 변환 예외를 발생시킨다.

        // when
        mockMvc.perform(get("/test/conversion"))
                // then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("COMMON_99"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.exceptionType").value(ConversionNotSupportedException.class.getName()));
    }

    @Test
    void 예상하지_못한_예외는_공통_500_응답을_반환한다() throws Exception {
        // given: 테스트 컨트롤러에서 IllegalStateException을 발생시킨다.

        // when
        mockMvc.perform(get("/test/unexpected"))
                // then
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("COMMON_99"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.exceptionType").value(IllegalStateException.class.getName()));
    }

    @RestController
    static class ExceptionTestController {
        private final Validator validator;

        ExceptionTestController(Validator validator) {
            this.validator = validator;
        }

        @GetMapping("/test/users/{id}")
        void userNotFound(@PathVariable("id") UUID id) {
            throw new UserNotFoundException(id);
        }

        @PostMapping("/test/validate")
        void validate(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/constraint")
        void constraintViolation() {
            throw new ConstraintViolationException(validator.validate(new TestRequest("")));
        }

        @PostMapping("/test/upload")
        void uploadSizeExceeded() {
            throw new MaxUploadSizeExceededException(1024);
        }

        @GetMapping("/test/conversion")
        void conversionFailure() {
            throw new ConversionNotSupportedException("value", UUID.class, null);
        }

        @GetMapping("/test/unexpected")
        void unexpectedFailure() {
            throw new IllegalStateException("Unexpected failure");
        }
    }

    record TestRequest(@NotBlank(message = "내용은 필수입니다.") String content) {
    }
}
