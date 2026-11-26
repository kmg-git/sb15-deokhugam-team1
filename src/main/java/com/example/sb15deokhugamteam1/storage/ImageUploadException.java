package com.example.sb15deokhugamteam1.storage;

/** 보관소(S3, 로컬 폴더)에 파일을 쓰지 못했을 때. 도서 쪽에서 502 응답으로 바꾼다 */
public class ImageUploadException extends RuntimeException {

    public ImageUploadException(String key, Throwable cause) {
        super("이미지 저장 실패: " + key, cause);
    }
}
