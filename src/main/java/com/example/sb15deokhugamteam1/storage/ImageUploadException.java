package com.example.sb15deokhugamteam1.storage;

/** 보관소(S3, 로컬 폴더) 파일 저장 실패. 도서 쪽에서 502 응답으로 변환 */
public class ImageUploadException extends RuntimeException {

    public ImageUploadException(String key, Throwable cause) {
        super("이미지 저장 실패: " + key, cause);
    }
}
