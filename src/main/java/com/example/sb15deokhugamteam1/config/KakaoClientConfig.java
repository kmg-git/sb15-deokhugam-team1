package com.example.sb15deokhugamteam1.config;

import com.example.sb15deokhugamteam1.client.kakao.KakaoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(KakaoProperties.class)
public class KakaoClientConfig {

    /** 카카오가 느리거나 응답이 없을 때 요청이 오래 붙잡혀 있지 않도록 타임아웃을 건다 */
    @Bean
    public RestClient kakaoRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }
}
