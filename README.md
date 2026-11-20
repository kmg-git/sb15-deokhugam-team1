# {팀 이름}

[팀 협업 문서](https://example.com/team-notion)

## 팀원 구성

[{팀원 1 이름}](https://github.com/github-id-1)  
[{팀원 2 이름}](https://github.com/github-id-2)  
[{팀원 3 이름}](https://github.com/github-id-3)  
[{팀원 4 이름}](https://github.com/github-id-4)  
[{팀원 5 이름}](https://github.com/github-id-5)  
[{팀원 6 이름}](https://github.com/github-id-6)

---

## 프로젝트 소개

- **프로젝트명**: {프로젝트 이름}
- **프로젝트 설명**: {누가 어떤 문제를 해결하기 위해 사용하는 서비스인지 한두 문장으로 작성}
- **프로젝트 기간**: YYYY.MM.DD ~ YYYY.MM.DD

---

## 기술 스택

- **Backend**: Java, Spring Boot, Spring Data JPA
- **Database**: {사용한 데이터베이스}
- **Infra / CI·CD**: {사용한 배포 및 자동화 도구}
- **공동 도구**: Git, GitHub, Notion, Discord

---

## 팀원별 구현 기능 상세

<!-- 이미지 또는 GIF를 저장한 뒤 주석을 해제하고 경로를 수정하세요. -->

### {팀원 1 이름}

- **담당 도메인**: {도메인명}

<!-- ![{기능명} 시연](docs/images/member-1.gif) -->

- **{구현 기능 1}**
  - {기능의 동작과 직접 구현한 내용}
  - {주요 API나 처리 방식}
- **{구현 기능 2}**
  - {기능의 동작과 직접 구현한 내용}

### {팀원 2 이름}

- **담당 도메인**: {도메인명}

<!-- ![{기능명} 시연](docs/images/member-2.gif) -->

- **{구현 기능 1}**
  - {기능의 동작과 직접 구현한 내용}
  - {주요 API나 처리 방식}
- **{구현 기능 2}**
  - {기능의 동작과 직접 구현한 내용}

### {팀원 3 이름}

- **담당 도메인**: {도메인명}

<!-- ![{기능명} 시연](docs/images/member-3.gif) -->

- **{구현 기능 1}**
  - {기능의 동작과 직접 구현한 내용}
  - {주요 API나 처리 방식}
- **{구현 기능 2}**
  - {기능의 동작과 직접 구현한 내용}

### {팀원 4 이름}

- **담당 도메인**: {도메인명}

<!-- ![{기능명} 시연](docs/images/member-4.gif) -->

- **{구현 기능 1}**
  - {기능의 동작과 직접 구현한 내용}
  - {주요 API나 처리 방식}
- **{구현 기능 2}**
  - {기능의 동작과 직접 구현한 내용}

### {팀원 5 이름}

- **담당 도메인**: {도메인명}

<!-- ![{기능명} 시연](docs/images/member-5.gif) -->

- **{구현 기능 1}**
  - {기능의 동작과 직접 구현한 내용}
  - {주요 API나 처리 방식}
- **{구현 기능 2}**
  - {기능의 동작과 직접 구현한 내용}

### {팀원 6 이름}

- **담당 도메인**: {도메인명}

<!-- ![{기능명} 시연](docs/images/member-6.gif) -->

- **{구현 기능 1}**
  - {기능의 동작과 직접 구현한 내용}
  - {주요 API나 처리 방식}
- **{구현 기능 2}**
  - {기능의 동작과 직접 구현한 내용}

---

## 파일 구조

<!-- 아래 예시를 실제 프로젝트의 패키지 구조에 맞게 수정하세요. -->

```text
.
├── docs/
│   └── images/
├── src/
│   ├── main/
│   │   ├── java/com/example/project/
│   │   │   ├── global/
│   │   │   └── domain/
│   │   │       └── {도메인명}/
│   │   │           ├── controller/
│   │   │           ├── service/
│   │   │           ├── repository/
│   │   │           ├── entity/
│   │   │           └── dto/
│   │   └── resources/
│   └── test/
├── build.gradle
└── README.md
```

---

## ERD 구조도
```mermaid
erDiagram
    users ||--o{ reviews : "작성"
    books ||--o{ reviews : "대상 도서"
    reviews ||--o{ comments : "댓글"
    users ||--o{ comments : "작성"
    reviews ||--o{ review_likes : "좋아요"
    users ||--o{ review_likes : "누름"
    users ||--o{ notifications : "수신"
    reviews ||--o{ notifications : "대상 리뷰"
    books ||..o{ popular_books : "스냅샷 (FK 없음)"
    reviews ||..o{ popular_reviews : "스냅샷 (FK 없음)"
    users ||..o{ power_users : "스냅샷 (FK 없음)"

    users {
        uuid id PK
        varchar email UK
        varchar nickname
        varchar password "해시 저장"
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at "논리 삭제, 1일 뒤 배치로 물리 삭제"
    }

    books {
        uuid id PK
        varchar title
        varchar author
        text description
        varchar publisher
        date published_date
        varchar isbn UK "nullable, 수정 불가"
        varchar thumbnail_url "nullable, S3"
        int review_count "집계 컬럼"
        double rating "집계 컬럼, 평균 평점"
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    reviews {
        uuid id PK
        uuid book_id FK
        uuid user_id FK "book_id + user_id 부분 유니크 (deleted_at IS NULL)"
        text content
        int rating
        int like_count "집계 컬럼"
        int comment_count "집계 컬럼"
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    review_likes {
        uuid id PK
        uuid review_id FK
        uuid user_id FK "review_id + user_id 유니크"
        timestamptz created_at "기간별 집계 기준"
    }

    comments {
        uuid id PK
        uuid review_id FK
        uuid user_id FK
        text content
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    notifications {
        uuid id PK
        uuid user_id FK "수신자 = 리뷰 작성자"
        uuid review_id FK
        varchar message
        boolean confirmed
        timestamptz created_at
        timestamptz updated_at "확인 시각, 1주일 뒤 배치 삭제"
    }

    popular_books {
        uuid id PK
        uuid book_id "FK 없음"
        varchar title "복사"
        varchar author "복사"
        varchar thumbnail_url "복사"
        varchar period "DAILY, WEEKLY, MONTHLY, ALL_TIME"
        date base_date "period + base_date + book_id 유니크"
        bigint rank
        double score
        bigint review_count "기간 내"
        double rating "기간 내 평균"
        timestamptz created_at
    }

    popular_reviews {
        uuid id PK
        uuid review_id "FK 없음"
        uuid book_id "FK 없음"
        varchar book_title "복사"
        varchar book_thumbnail_url "복사"
        uuid user_id "FK 없음"
        varchar user_nickname "복사"
        text review_content "복사"
        double review_rating "복사"
        varchar period
        date base_date "period + base_date + review_id 유니크"
        bigint rank
        double score
        bigint like_count "기간 내"
        bigint comment_count "기간 내"
        timestamptz created_at
    }

    power_users {
        uuid id PK
        uuid user_id "FK 없음"
        varchar nickname "복사"
        varchar period
        date base_date "period + base_date + user_id 유니크"
        bigint rank
        double score
        double review_score_sum
        bigint like_count "기간 내"
        bigint comment_count "기간 내"
        timestamptz created_at
    }

```

## 구현 홈페이지

[서비스 접속](https://example.com)

---

## 프로젝트 회고록

- [팀 회고록](https://example.com/retrospective)
- [발표 자료](https://example.com/presentation)

