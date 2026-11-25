package com.example.sb15deokhugamteam1.client.kakao;

import com.example.sb15deokhugamteam1.exception.book.BookInfoFetchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

/** 카카오 책 검색 API 호출과 표지 이미지 다운로드를 담당한다 */
@Slf4j
@Component
public class KakaoBookClient {

    /** 표지 이미지는 카카오 CDN 에서만 받는다 (응답에 엉뚱한 주소가 와도 따라가지 않도록) */
    private static final String THUMBNAIL_HOST_SUFFIX = ".kakaocdn.net";

    /** 카카오 썸네일은 수십 KB 정도라 넉넉하게 2MB 까지만 받는다 */
    private static final int MAX_THUMBNAIL_BYTES = 2 * 1024 * 1024;

    private final RestClient restClient;
    private final KakaoProperties properties;

    public KakaoBookClient(RestClient kakaoRestClient, KakaoProperties properties) {
        this.restClient = kakaoRestClient;
        this.properties = properties;
    }

    /** ISBN 으로 검색한 결과 중 그 ISBN 을 가진 첫 번째 책 */
    public Optional<KakaoBookResponse.Document> searchByIsbn(String isbn) {
        if (!properties.hasKey()) {
            log.error("카카오 REST API 키가 없습니다. 환경변수 KAKAO_REST_API_KEY 를 설정하세요.");
            throw new BookInfoFetchException(isbn);
        }

        KakaoBookResponse response;
        try {
            response = restClient.get()
                    .uri(properties.baseUrl() + "/v3/search/book?target=isbn&query={isbn}", isbn)
                    .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + properties.restApiKey())
                    .retrieve()
                    .body(KakaoBookResponse.class);
        } catch (RestClientException e) {
            log.warn("카카오 책 검색 실패: isbn={}, reason={}", isbn, e.getMessage());
            throw new BookInfoFetchException(isbn);
        }

        if (response == null || response.documents() == null) {
            return Optional.empty();
        }
        return response.documents().stream()
                .filter(doc -> doc.isbn() != null && doc.isbn().contains(isbn))
                .findFirst();
    }

    /** 표지 이미지를 받아온다. 실패해도 도서 정보는 보여줘야 하므로 예외 대신 빈 값 */
    public Optional<byte[]> downloadThumbnail(String url) {
        if (!isAllowedThumbnailUrl(url)) {
            return Optional.empty();
        }
        try {
            byte[] bytes = restClient.get()
                    .uri(URI.create(url))
                    .retrieve()
                    .body(byte[].class);
            if (bytes == null || bytes.length == 0 || bytes.length > MAX_THUMBNAIL_BYTES) {
                return Optional.empty();
            }
            return Optional.of(bytes);
        } catch (RestClientException | IllegalArgumentException e) {
            log.warn("표지 이미지 다운로드 실패: url={}, reason={}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private static boolean isAllowedThumbnailUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            return "https".equalsIgnoreCase(uri.getScheme())
                    && host != null
                    && host.toLowerCase(Locale.ROOT).endsWith(THUMBNAIL_HOST_SUFFIX);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
