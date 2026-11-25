package com.example.sb15deokhugamteam1.client.kakao;

import com.example.sb15deokhugamteam1.exception.book.BookInfoFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KakaoBookClientTest {

    private static final String ISBN = "9788966260959";
    private static final String SEARCH_URL =
            "https://dapi.kakao.com/v3/search/book?target=isbn&query=" + ISBN;

    private MockRestServiceServer server;
    private KakaoBookClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new KakaoBookClient(builder.build(), new KakaoProperties("test-key", null));
    }

    @Test
    void ISBN으로_검색하면_키를_헤더에_담아_보내고_일치하는_책을_돌려준다() {
        server.expect(requestTo(SEARCH_URL))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "KakaoAK test-key"))
                .andRespond(withSuccess("""
                        {"documents":[
                          {"title":"다른 책","isbn":"1111111111 9791111111111"},
                          {"title":"클린 코드","contents":"설명","isbn":"8966260950 9788966260959",
                           "datetime":"2013-12-24T00:00:00.000+09:00","authors":["로버트 C. 마틴"],
                           "publisher":"인사이트","thumbnail":"https://search1.kakaocdn.net/thumb/a.jpg",
                           "price":33000}
                        ],"meta":{"total_count":2}}
                        """, MediaType.APPLICATION_JSON));

        Optional<KakaoBookResponse.Document> result = client.searchByIsbn(ISBN);

        assertThat(result).isPresent();
        assertThat(result.get().title()).isEqualTo("클린 코드");
        assertThat(result.get().authors()).containsExactly("로버트 C. 마틴");
        server.verify();
    }

    @Test
    void 검색_결과가_없으면_빈_값() {
        server.expect(requestTo(SEARCH_URL))
                .andRespond(withSuccess("{\"documents\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.searchByIsbn(ISBN)).isEmpty();
    }

    @Test
    void 카카오가_오류를_주면_BookInfoFetchException() {
        server.expect(requestTo(SEARCH_URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.searchByIsbn(ISBN)).isInstanceOf(BookInfoFetchException.class);
    }

    @Test
    void API_키가_없으면_호출하지_않고_BookInfoFetchException() {
        KakaoBookClient noKey = new KakaoBookClient(RestClient.create(), new KakaoProperties(" ", null));

        assertThatThrownBy(() -> noKey.searchByIsbn(ISBN)).isInstanceOf(BookInfoFetchException.class);
    }

    @Test
    void 카카오_CDN_표지_이미지를_받아온다() {
        byte[] image = {1, 2, 3};
        server.expect(requestTo("https://search1.kakaocdn.net/thumb/a.jpg"))
                .andRespond(withSuccess(image, MediaType.IMAGE_JPEG));

        assertThat(client.downloadThumbnail("https://search1.kakaocdn.net/thumb/a.jpg")).contains(image);
    }

    @Test
    void 표지_다운로드가_실패하면_빈_값() {
        server.expect(requestTo("https://search1.kakaocdn.net/thumb/a.jpg"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(client.downloadThumbnail("https://search1.kakaocdn.net/thumb/a.jpg")).isEmpty();
    }

    @Test
    void 카카오_CDN이_아니거나_https가_아니면_받지_않는다() {
        assertThat(client.downloadThumbnail(null)).isEmpty();
        assertThat(client.downloadThumbnail("")).isEmpty();
        assertThat(client.downloadThumbnail("http://search1.kakaocdn.net/a.jpg")).isEmpty();
        assertThat(client.downloadThumbnail("https://evil.example.com/a.jpg")).isEmpty();
        assertThat(client.downloadThumbnail("https://kakaocdn.net.evil.com/a.jpg")).isEmpty();
        assertThat(client.downloadThumbnail("not a url")).isEmpty();
        server.verify();
    }
}
