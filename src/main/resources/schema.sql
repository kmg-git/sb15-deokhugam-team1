CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =========================== 사용자 ===========================
CREATE TABLE users (
    id          uuid PRIMARY KEY,
    email       varchar NOT NULL UNIQUE,
    nickname    varchar(20) NOT NULL CHECK (char_length(nickname) BETWEEN 2 AND 20),
    password    varchar NOT NULL CHECK (char_length(password) >= 60),      -- BCrypt 해시
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz,
    deleted_at  timestamptz
);

-- =========================== 도서 ===========================
CREATE TABLE books (
    id              uuid PRIMARY KEY,
    title           varchar NOT NULL,
    author          varchar NOT NULL,
    description     text NOT NULL,
    publisher       varchar NOT NULL,
    published_date  date NOT NULL,
    isbn            varchar(13) CHECK (isbn ~ '^97[89][0-9]{10}$'),        -- 선택, 하이픈 없는 13자리
    thumbnail_url   varchar,
    review_count    integer NOT NULL DEFAULT 0 CHECK (review_count >= 0),
    rating_sum      bigint  NOT NULL DEFAULT 0 CHECK (rating_sum >= 0),    -- 평균 재계산용
    rating          double precision NOT NULL DEFAULT 0 CHECK (rating BETWEEN 0 AND 5),
    created_at      timestamptz NOT NULL,
    updated_at      timestamptz,
    deleted_at      timestamptz
);

-- 삭제 안 된 책끼리만 ISBN 중복 금지 (삭제한 책과 같은 ISBN 재등록 가능)
CREATE UNIQUE INDEX uq_books_active_isbn
    ON books (isbn)
    WHERE deleted_at IS NULL AND isbn IS NOT NULL;

-- =========================== 리뷰 ===========================
CREATE TABLE reviews (
    id             uuid PRIMARY KEY,
    book_id        uuid NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    user_id        uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content        text NOT NULL,
    rating         integer NOT NULL CHECK (rating BETWEEN 1 AND 5),
    like_count     integer NOT NULL DEFAULT 0 CHECK (like_count >= 0),
    comment_count  integer NOT NULL DEFAULT 0 CHECK (comment_count >= 0),
    created_at     timestamptz NOT NULL,
    updated_at     timestamptz,
    deleted_at     timestamptz
);

CREATE UNIQUE INDEX uq_reviews_active_book_user
    ON reviews (book_id, user_id)
    WHERE deleted_at IS NULL;

CREATE TABLE review_likes (
    id          uuid PRIMARY KEY,
    review_id   uuid NOT NULL REFERENCES reviews (id) ON DELETE CASCADE,
    user_id     uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at  timestamptz NOT NULL,

    CONSTRAINT uq_review_likes_review_user UNIQUE (review_id, user_id)
);

-- =========================== 댓글 ===========================
CREATE TABLE comments (
    id          uuid PRIMARY KEY,
    review_id   uuid NOT NULL REFERENCES reviews (id) ON DELETE CASCADE,
    user_id     uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content     text NOT NULL,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz,
    deleted_at  timestamptz
);

-- =========================== 알림 ===========================
CREATE TABLE notifications (
    id          uuid PRIMARY KEY,
    user_id     uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    review_id   uuid NOT NULL REFERENCES reviews (id) ON DELETE CASCADE,
    message     varchar NOT NULL,
    confirmed   boolean NOT NULL DEFAULT false,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz
);

-- =========================== 대시보드 (스냅샷, FK 없음) ===========================
CREATE TABLE popular_books (
    id             uuid PRIMARY KEY,
    book_id        uuid NOT NULL,
    title          varchar NOT NULL,
    author         varchar NOT NULL,
    thumbnail_url  varchar,
    period         varchar NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    base_date      date NOT NULL,
    rank           bigint NOT NULL,
    score          double precision NOT NULL,
    review_count   bigint NOT NULL,
    rating         double precision NOT NULL,
    created_at     timestamptz NOT NULL,

    CONSTRAINT uq_popular_books_period_date_book UNIQUE (period, base_date, book_id)
);

CREATE TABLE popular_reviews (
    id                  uuid PRIMARY KEY,
    review_id           uuid NOT NULL,
    book_id             uuid NOT NULL,
    book_title          varchar NOT NULL,
    book_thumbnail_url  varchar,
    user_id             uuid NOT NULL,
    user_nickname       varchar NOT NULL,
    review_content      text NOT NULL,
    review_rating       double precision NOT NULL,
    period              varchar NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    base_date           date NOT NULL,
    rank                bigint NOT NULL,
    score               double precision NOT NULL,
    like_count          bigint NOT NULL,
    comment_count       bigint NOT NULL,
    created_at          timestamptz NOT NULL,

    CONSTRAINT uq_popular_reviews_period_date_review UNIQUE (period, base_date, review_id)
);

CREATE TABLE power_users (
    id                uuid PRIMARY KEY,
    user_id           uuid NOT NULL,
    nickname          varchar NOT NULL,
    period            varchar NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME')),
    base_date         date NOT NULL,
    rank              bigint NOT NULL,
    score             double precision NOT NULL,
    review_score_sum  double precision NOT NULL,
    like_count        bigint NOT NULL,
    comment_count     bigint NOT NULL,
    created_at        timestamptz NOT NULL,

    CONSTRAINT uq_power_users_period_date_user UNIQUE (period, base_date, user_id)
);

-- =========================== 인덱스 (조회·커서 페이지네이션용) ===========================
-- 도서 목록: 키워드 검색 + 정렬(title, publishedDate, rating, reviewCount) + 커서(created_at)
CREATE INDEX idx_books_title_trgm   ON books USING gin (title gin_trgm_ops)  WHERE deleted_at IS NULL;
CREATE INDEX idx_books_author_trgm  ON books USING gin (author gin_trgm_ops) WHERE deleted_at IS NULL;
CREATE INDEX idx_books_title        ON books (title, created_at)          WHERE deleted_at IS NULL;
CREATE INDEX idx_books_published    ON books (published_date, created_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_books_rating       ON books (rating, created_at)         WHERE deleted_at IS NULL;
CREATE INDEX idx_books_review_count ON books (review_count, created_at)   WHERE deleted_at IS NULL;

-- 리뷰 목록: 책별/사용자별 필터 + 정렬(createdAt, rating)
CREATE INDEX idx_reviews_book_created ON reviews (book_id, created_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_reviews_user         ON reviews (user_id);
CREATE INDEX idx_reviews_rating       ON reviews (rating, created_at)  WHERE deleted_at IS NULL;

-- 좋아요: 사용자 삭제(CASCADE), 파워유저 집계
CREATE INDEX idx_review_likes_user_created ON review_likes (user_id, created_at);

-- 댓글 목록: 리뷰별 + 커서
CREATE INDEX idx_comments_review_created ON comments (review_id, created_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_comments_user_created   ON comments (user_id, created_at);

-- 알림 목록: 사용자별 + 커서 / 읽은 지 1주일 지난 알림 삭제 배치
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at);
CREATE INDEX idx_notifications_read_updated ON notifications (updated_at) WHERE confirmed = true;
CREATE INDEX idx_notifications_review       ON notifications (review_id);

-- 대시보드 조회: 기간 + 기준일 + 순위
CREATE INDEX idx_popular_books_rank   ON popular_books   (period, base_date, rank);
CREATE INDEX idx_popular_reviews_rank ON popular_reviews (period, base_date, rank);
CREATE INDEX idx_power_users_rank     ON power_users     (period, base_date, rank);
