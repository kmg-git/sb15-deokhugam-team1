package com.example.sb15deokhugamteam1.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String CLIENT_IP_KEY = "clientIp";
    public static final String REQUEST_ID_HEADER = "Deokhugam-Request-ID";

    private static final String API_PATH_PREFIX = "/api/";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String FORWARDED_FOR_DELIMITER = ",";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path =
                request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith(API_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException,
    IOException {
        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(CLIENT_IP_KEY, resolveClientIp(request));

        response.setHeader(REQUEST_ID_HEADER, requestId);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("{} {} -> {} ({}ms)",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), System.currentTimeMillis() - start);
            MDC.remove(REQUEST_ID_KEY);
            MDC.remove(CLIENT_IP_KEY);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(FORWARDED_FOR_DELIMITER)[0].trim();
        }
        return request.getRemoteAddr();
    }
}
