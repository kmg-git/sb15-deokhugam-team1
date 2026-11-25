package com.example.sb15deokhugamteam1.service;

import com.example.sb15deokhugamteam1.exception.book.InvalidIsbnException;

import java.util.regex.Pattern;

/** 도서 등록과 ISBN 정보 조회에서 같은 ISBN 규칙을 쓰기 위한 도우미 */
final class IsbnNormalizer {

    /** schema.sql 의 ck_books_isbn 과 같은 규칙: 978/979로 시작하는 13자리 숫자 */
    private static final Pattern ISBN_13 = Pattern.compile("^97[89]\\d{10}$");

    private IsbnNormalizer() {
    }

    /** 빈 값은 null, 하이픈·공백은 제거한 뒤 ISBN-13 형식인지 검사 */
    static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String isbn = raw.replaceAll("[\\s-]", "");
        if (!ISBN_13.matcher(isbn).matches()) {
            throw new InvalidIsbnException(raw);
        }
        return isbn;
    }
}
