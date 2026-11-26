package com.example.sb15deokhugamteam1.config;

import com.example.sb15deokhugamteam1.client.ocr.OcrSpaceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OcrSpaceProperties.class)
public class OcrSpaceClientConfig {

    /** OCR 은 이미지 분석이라 카카오보다 오래 걸려서 읽기 타임아웃을 넉넉히 둔다 */
    @Bean
    public RestClient ocrSpaceRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(20));
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
