package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** orderBy 가 title | publishedDate | rating | reviewCount 가 아닐 때 → 400 */
public class InvalidSortFieldException extends BookException {

    public InvalidSortFieldException(String orderBy) {
        super(BookErrorCode.INVALID_SORT_FIELD, Map.of("orderBy", orderBy));
    }
}
