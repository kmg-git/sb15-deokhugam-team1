package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** 표지 이미지 S3(또는 로컬 폴더) 저장 실패 → 502 */
public class ThumbnailUploadException extends BookException {

    public ThumbnailUploadException(Throwable cause) {
        super(BookErrorCode.THUMBNAIL_UPLOAD_FAILED, Map.of());
        initCause(cause);
    }
}
