package com.example.sb15deokhugamteam1.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MdcLoggingFilterTest {

    private static final String HTTP_METHOD = "GET";
    private static final String REQUEST_URI = "/api/notifications";
    private static final String STATIC_URI = "/index.html";
    private static final String REMOTE_ADDR = "203.0.113.7";
    private static final String PROXY_ADDR = "10.0.0.1";
    private static final String FORWARDED_CLIENT_ADDR = "198.51.100.23";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final MdcLoggingFilter filter = new MdcLoggingFilter();

    @Test
    @DisplayName("요청 ID와 IP가 MDC에 들어가고 같은 요청 ID가 응답 헤더에 담긴다")
    void putsRequestIdAndClientIpIntoMdcAndResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HTTP_METHOD, REQUEST_URI);
        request.setRemoteAddr(REMOTE_ADDR);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> idInChain = new AtomicReference<>();
        AtomicReference<String> ipInChain = new AtomicReference<>();

        filter.doFilter(request, response, (req, res) -> {
            idInChain.set(MDC.get(MdcLoggingFilter.REQUEST_ID_KEY));
            ipInChain.set(MDC.get(MdcLoggingFilter.CLIENT_IP_KEY));
        });

        assertThat(idInChain.get()).isNotBlank();
        assertThat(ipInChain.get()).isEqualTo(REMOTE_ADDR);
        assertThat(response.getHeader(MdcLoggingFilter.REQUEST_ID_HEADER)).isEqualTo(idInChain.get());
    }

    @Test
    @DisplayName("X-Forwarded-For가 있으면 첫 번째 IP를 사용한다")
    void usesFirstIpOfForwardedForHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HTTP_METHOD, REQUEST_URI);
        request.setRemoteAddr(PROXY_ADDR);
        request.addHeader(FORWARDED_FOR_HEADER, FORWARDED_CLIENT_ADDR + ", " + PROXY_ADDR);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> ipInChain = new AtomicReference<>();

        filter.doFilter(request, response,
                (req, res) -> ipInChain.set(MDC.get(MdcLoggingFilter.CLIENT_IP_KEY)));

        assertThat(ipInChain.get()).isEqualTo(FORWARDED_CLIENT_ADDR);
    }

    @Test
    @DisplayName("요청이 끝나면 예외가 나도 MDC가 정리된다")
    void clearsMdcEvenWhenChainThrows() {
        MockHttpServletRequest request = new MockHttpServletRequest(HTTP_METHOD, REQUEST_URI);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (req, res) -> {
            throw new IOException("boom");
        })).isInstanceOf(IOException.class);

        assertThat(MDC.get(MdcLoggingFilter.REQUEST_ID_KEY)).isNull();
        assertThat(MDC.get(MdcLoggingFilter.CLIENT_IP_KEY)).isNull();
    }

    @Test
    @DisplayName("API가 아닌 요청(정적 파일 등)에는 요청 ID를 만들지 않는다")
    void skipsRequestsOutsideApiPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(HTTP_METHOD, STATIC_URI);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> idInChain = new AtomicReference<>();

        filter.doFilter(request, response,
                (req, res) -> idInChain.set(MDC.get(MdcLoggingFilter.REQUEST_ID_KEY)));

        assertThat(idInChain.get()).isNull();
        assertThat(response.getHeader(MdcLoggingFilter.REQUEST_ID_HEADER)).isNull();
    }
}