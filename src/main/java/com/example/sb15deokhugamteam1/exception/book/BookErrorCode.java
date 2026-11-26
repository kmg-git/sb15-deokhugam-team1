package com.example.sb15deokhugamteam1.exception.book;

import com.example.sb15deokhugamteam1.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BookErrorCode implements ErrorCode {

    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_01", "도서를 찾을 수 없습니다."),
    DUPLICATE_ISBN(HttpStatus.CONFLICT, "BOOK_02", "이미 등록된 ISBN입니다."),
    INVALID_ISBN(HttpStatus.BAD_REQUEST, "BOOK_03", "ISBN 형식이 올바르지 않습니다."),
    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "BOOK_04", "지원하지 않는 정렬 기준입니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "BOOK_05", "커서 값이 올바르지 않습니다."),
    BOOK_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK_06", "해당 ISBN의 도서 정보를 찾을 수 없습니다."),
    BOOK_INFO_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "BOOK_07", "도서 정보를 가져오지 못했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_IMAGE(HttpStatus.BAD_REQUEST, "BOOK_08", "이미지 파일이 비어 있거나 읽을 수 없습니다."),
    ISBN_NOT_RECOGNIZED(HttpStatus.BAD_REQUEST, "BOOK_09", "이미지에서 ISBN을 인식하지 못했습니다. 바코드가 잘 보이게 다시 찍어주세요."),
    OCR_FAILED(HttpStatus.BAD_GATEWAY, "BOOK_10", "이미지 인식 서비스에 연결하지 못했습니다. 잠시 후 다시 시도해주세요."),
    THUMBNAIL_UPLOAD_FAILED(HttpStatus.BAD_GATEWAY, "BOOK_11", "표지 이미지를 저장하지 못했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    BookErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
