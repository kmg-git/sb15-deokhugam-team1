package com.example.sb15deokhugamteam1.client.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** 카카오 책 검색 API 응답 중 필요한 필드만 받는다 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoBookResponse(List<Document> documents) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            String title,
            String contents,
            /** "ISBN10 ISBN13" 처럼 공백으로 구분되어 온다. 둘 중 하나만 있을 수도 있다 */
            String isbn,
            /** "2013-12-24T00:00:00.000+09:00" 형식 */
            String datetime,
            List<String> authors,
            String publisher,
            String thumbnail
    ) {
    }
}
