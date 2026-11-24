package com.example.sb15deokhugamteam1.repository;

import com.example.sb15deokhugamteam1.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID>, BookQueryRepository {

    Optional<Book> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByIsbnAndDeletedAtIsNull(String isbn);

    // ── 리뷰 집계: 읽고-계산하고-쓰기 대신 UPDATE 한 문장으로 처리해 동시 요청에도 값이 유실되지 않게 한다.
    //    SET 절의 오른쪽 값은 모두 "갱신 전" 행 기준으로 계산된다.
    //    반환값은 갱신된 행 수. 0이면 대상 도서가 없거나 삭제된 상태.

    /** 리뷰 작성 시 */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Book b
               set b.reviewCount = b.reviewCount + 1,
                   b.ratingSum   = b.ratingSum + :rating,
                   b.rating      = cast(b.ratingSum + :rating as double) / (b.reviewCount + 1)
             where b.id = :bookId
               and b.deletedAt is null
            """)
    int increaseReview(@Param("bookId") UUID bookId, @Param("rating") int rating);

    /** 리뷰 삭제 시 (논리·물리 모두) */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Book b
               set b.reviewCount = b.reviewCount - 1,
                   b.ratingSum   = b.ratingSum - :rating,
                   b.rating      = case when b.reviewCount - 1 = 0 then 0.0
                                        else cast(b.ratingSum - :rating as double) / (b.reviewCount - 1) end
             where b.id = :bookId
               and b.reviewCount > 0
            """)
    int decreaseReview(@Param("bookId") UUID bookId, @Param("rating") int rating);

    /** 리뷰 평점 수정 시: diff = 새 평점 - 옛 평점 */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update Book b
               set b.ratingSum = b.ratingSum + :diff,
                   b.rating    = cast(b.ratingSum + :diff as double) / b.reviewCount
             where b.id = :bookId
               and b.reviewCount > 0
            """)
    int changeReviewRating(@Param("bookId") UUID bookId, @Param("diff") int diff);
}
