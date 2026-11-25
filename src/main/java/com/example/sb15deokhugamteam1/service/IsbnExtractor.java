package com.example.sb15deokhugamteam1.service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** OCR 로 읽은 글자 덩어리에서 ISBN-13 을 찾아낸다 */
final class IsbnExtractor {

    /**
     * 978/979 로 시작하는 13자리 숫자. 숫자 사이의 공백·하이픈은 허용한다
     * 예) "ISBN 978-89-6626-095-9", 바코드 아래 "9 788966 260959"
     */
    private static final Pattern CANDIDATE = Pattern.compile("9[ \\t-]{0,2}7[ \\t-]{0,2}[89](?:[ \\t-]{0,2}\\d){10}");

    private IsbnExtractor() {
    }

    /** 체크 숫자까지 맞는 첫 번째 ISBN. OCR 이 숫자를 잘못 읽은 후보는 걸러진다 */
    static Optional<String> extract(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = CANDIDATE.matcher(text);
        while (matcher.find()) {
            String isbn = matcher.group().replaceAll("[^0-9]", "");
            if (hasValidCheckDigit(isbn)) {
                return Optional.of(isbn);
            }
        }
        return Optional.empty();
    }

    /** ISBN-13 마지막 자리 검사: 앞 12자리에 1,3,1,3... 을 곱해 더한 값으로 계산 */
    static boolean hasValidCheckDigit(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - sum % 10) % 10;
        return check == isbn.charAt(12) - '0';
    }
}
