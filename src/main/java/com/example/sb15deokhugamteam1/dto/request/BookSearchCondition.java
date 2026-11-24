package com.example.sb15deokhugamteam1.dto.request;

import org.springframework.data.domain.Sort;

import java.time.Instant;

/**
 * 도서 목록 조회 조건.
 *
 * @param keyword   제목·저자·ISBN 부분 검색 (null 이면 전체)
 * @param sortField 정렬 기준
 * @param direction 정렬 방향
 * @param cursor    이전 페이지 마지막 책의 정렬 기준 값 (첫 페이지면 null)
 * @param after     이전 페이지 마지막 책의 createdAt (정렬 값이 같을 때 순서를 정하는 보조 커서)
 * @param limit     한 페이지 크기
 */
public record BookSearchCondition(
        String keyword,
        BookSortField sortField,
        Sort.Direction direction,
        String cursor,
        Instant after,
        int limit
) {
    public boolean hasCursor() {
        return cursor != null && after != null;
    }
}
