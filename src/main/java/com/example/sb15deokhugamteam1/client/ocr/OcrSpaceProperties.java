package com.example.sb15deokhugamteam1.client.ocr;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yaml 의 ocr-space.* 설정
 * apiKey 는 환경변수 OCR_SPACE_API_KEY 로만 넣는다 (깃에 올리지 않기)
 */
@ConfigurationProperties(prefix = "ocr-space")
public record OcrSpaceProperties(
        String apiKey,
        String baseUrl
) {
    public OcrSpaceProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.ocr.space";
        }
    }

    public boolean hasKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
