package com.example.sb15deokhugamteam1.exception.book;

import java.util.HashMap;
import java.util.Map;

/** cursor 를 정렬 기준 값으로 읽을 수 없거나, cursor·after 중 하나만 왔을 때 → 400 */
public class InvalidCursorException extends BookException {

    public InvalidCursorException(String cursor, Object after) {
        super(BookErrorCode.INVALID_CURSOR, details(cursor, after));
    }

    // Map.of 는 null 값을 받지 못해서(NPE) 둘 중 하나만 와도 담을 수 있게 HashMap 사용
    private static Map<String, Object> details(String cursor, Object after) {
        Map<String, Object> details = new HashMap<>();
        details.put("cursor", cursor);
        details.put("after", after);
        return details;
    }
}
