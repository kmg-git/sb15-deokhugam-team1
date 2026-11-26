package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.client.kakao.KakaoBookClient;
import com.example.sb15deokhugamteam1.client.kakao.KakaoBookResponse;
import com.example.sb15deokhugamteam1.dto.response.BookInfoDto;
import com.example.sb15deokhugamteam1.exception.book.BookInfoNotFoundException;
import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookInfoServiceTest {

    @Mock
    KakaoBookClient kakaoBookClient;

    @InjectMocks
    BookInfoService bookInfoService;

    private static KakaoBookResponse.Document doc(List<String> authors, String datetime) {
        return new KakaoBookResponse.Document("클린 코드", "설명", "8966260950 9788966260959",
                datetime, authors, "인사이트", "https://search1.kakaocdn.net/thumb/a.jpg");
    }

    @Test
    void 카카오_결과를_화면에_채울_값으로_바꾼다() {
        byte[] image = {1, 2, 3};
        given(kakaoBookClient.searchByIsbn("9788966260959"))
                .willReturn(Optional.of(doc(List.of("로버트 C. 마틴", "박재호"), "2013-12-24T00:00:00.000+09:00")));
        given(kakaoBookClient.downloadThumbnail("https://search1.kakaocdn.net/thumb/a.jpg"))
                .willReturn(Optional.of(image));

        BookInfoDto info = bookInfoService.getByIsbn("978-89-6626-095-9");

        assertThat(info.title()).isEqualTo("클린 코드");
        assertThat(info.author()).isEqualTo("로버트 C. 마틴, 박재호");
        assertThat(info.description()).isEqualTo("설명");
        assertThat(info.publisher()).isEqualTo("인사이트");
        assertThat(info.publishedDate()).isEqualTo("2013-12-24");
        assertThat(info.isbn()).isEqualTo("9788966260959");
        assertThat(info.thumbnailImage()).isEqualTo(image);
    }

    @Test
    void 저자_날짜_표지가_없어도_나머지는_돌려준다() {
        given(kakaoBookClient.searchByIsbn("9788966260959")).willReturn(Optional.of(doc(null, "")));
        given(kakaoBookClient.downloadThumbnail(any())).willReturn(Optional.empty());

        BookInfoDto info = bookInfoService.getByIsbn("9788966260959");

        assertThat(info.title()).isEqualTo("클린 코드");
        assertThat(info.author()).isNull();
        assertThat(info.publishedDate()).isNull();
        assertThat(info.thumbnailImage()).isNull();
    }

    @Test
    void 카카오에_없는_책이면_BookInfoNotFoundException() {
        given(kakaoBookClient.searchByIsbn("9788966260959")).willReturn(Optional.empty());

        assertThatThrownBy(() -> bookInfoService.getByIsbn("9788966260959"))
                .isInstanceOf(BookInfoNotFoundException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "123", "8966260950", "97889662609590"})
    void ISBN_형식이_틀리면_카카오를_부르지_않고_InvalidIsbnException(String isbn) {
        assertThatThrownBy(() -> bookInfoService.getByIsbn(isbn))
                .isInstanceOf(InvalidIsbnException.class);
        verify(kakaoBookClient, never()).searchByIsbn(any());
    }
}
