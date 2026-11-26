package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.client.kakao.KakaoBookClient;
import com.example.sb15deokhugamteam1.client.kakao.KakaoBookResponse;
import com.example.sb15deokhugamteam1.dto.response.BookInfoDto;
import com.example.sb15deokhugamteam1.exception.book.BookInfoNotFoundException;
import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/** ISBN 으로 카카오에서 도서 정보를 찾아, 등록 화면에 미리 채울 값으로 바꿔준다 */
@Service
@RequiredArgsConstructor
public class BookInfoService {

    private final KakaoBookClient kakaoBookClient;

    public BookInfoDto getByIsbn(String rawIsbn) {
        String isbn = IsbnNormalizer.normalize(rawIsbn);
        if (isbn == null) {
            throw new InvalidIsbnException(rawIsbn == null ? "" : rawIsbn);
        }

        KakaoBookResponse.Document doc = kakaoBookClient.searchByIsbn(isbn)
                .orElseThrow(() -> new BookInfoNotFoundException(isbn));

        byte[] thumbnail = kakaoBookClient.downloadThumbnail(doc.thumbnail()).orElse(null);

        return new BookInfoDto(
                doc.title(),
                joinAuthors(doc.authors()),
                doc.contents(),
                doc.publisher(),
                toDate(doc.datetime()),
                isbn,
                thumbnail
        );
    }

    /** ["저자1", "저자2"] → "저자1, 저자2" */
    private static String joinAuthors(List<String> authors) {
        if (authors == null) {
            return null;
        }
        return String.join(", ", authors.stream().filter(Objects::nonNull).toList());
    }

    /** "2013-12-24T00:00:00.000+09:00" → "2013-12-24" (화면 날짜 입력칸 형식) */
    private static String toDate(String datetime) {
        if (datetime == null || datetime.length() < 10) {
            return null;
        }
        return datetime.substring(0, 10);
    }
}
