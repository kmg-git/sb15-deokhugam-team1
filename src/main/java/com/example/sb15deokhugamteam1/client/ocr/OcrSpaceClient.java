package com.example.sb15deokhugamteam1.client.ocr;

import com.example.sb15deokhugamteam1.exception.book.OcrFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Objects;
import java.util.stream.Collectors;

/** OCR.space 에 이미지를 보내 글자를 읽어 온다 */
@Slf4j
@Component
public class OcrSpaceClient {

    private final RestClient restClient;
    private final OcrSpaceProperties properties;

    public OcrSpaceClient(@Qualifier("ocrSpaceRestClient") RestClient restClient, OcrSpaceProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /** 이미지 속 글자를 한 문자열로 돌려준다. 글자가 없으면 빈 문자열 */
    public String parseText(byte[] image, String filename) {
        if (!properties.hasKey()) {
            log.error("OCR.space API 키가 없습니다. 환경변수 OCR_SPACE_API_KEY 를 설정하세요.");
            throw new OcrFailedException();
        }

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new NamedByteArrayResource(image, filename));
        form.add("language", "eng");   // ISBN 은 숫자라 영어 모델로 충분하다
        form.add("OCREngine", "2");    // 2번 엔진이 숫자·바코드 아래 글자를 더 잘 읽는다
        form.add("scale", "true");     // 작은 글씨를 키워서 읽는다

        OcrSpaceResponse response;
        try {
            response = restClient.post()
                    .uri(properties.baseUrl() + "/parse/image")
                    .header("apikey", properties.apiKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(form)
                    .retrieve()
                    .body(OcrSpaceResponse.class);
        } catch (RestClientException e) {
            log.warn("OCR.space 호출 실패: reason={}", e.getMessage());
            throw new OcrFailedException();
        }

        if (response == null || response.erroredOnProcessing()) {
            log.warn("OCR.space 처리 실패: error={}", response == null ? "빈 응답" : response.errorMessage());
            throw new OcrFailedException();
        }
        if (response.parsedResults() == null) {
            return "";
        }
        return response.parsedResults().stream()
                .map(OcrSpaceResponse.ParsedResult::parsedText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }

    /** OCR.space 는 파일 이름(확장자)으로 이미지 종류를 판단하므로 이름을 꼭 붙여 보낸다 */
    private static final class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        NamedByteArrayResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
