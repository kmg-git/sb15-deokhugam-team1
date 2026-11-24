package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.dto.request.BookSearchCondition;
import com.example.sb15deokhugamteam1.entity.Book;

import java.util.List;

/** 조건이 그때그때 달라지는(동적) 도서 조회. 구현은 BookQueryRepositoryImpl */
public interface BookQueryRepository {

    /** 커서 다음부터 condition.limit() + 1 개를 가져온다. (1개 더 가져와서 다음 페이지가 있는지 판단) */
    List<Book> search(BookSearchCondition condition, Object cursorValue);

    /** 커서와 상관없이 검색어에 맞는 전체 개수 */
    long countByKeyword(String keyword);
}
