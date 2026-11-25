package com.example.sb15deokhugamteam1.client.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yaml 의 kakao.* 설정
 * restApiKey 는 환경변수 KAKAO_REST_API_KEY 로만 넣는다 (깃에 올리지 않기)
 */
@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String restApiKey,
        String baseUrl
) {
    public KakaoProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://dapi.kakao.com";
        }
    }

    public boolean hasKey() {
        return restApiKey != null && !restApiKey.isBlank();
    }
}
