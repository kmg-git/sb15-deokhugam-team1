package com.example.sb15deokhugamteam1.storage;

/**
 * 이미지 파일 보관소. S3 버킷 설정 시 S3, 미설정 시 로컬 폴더에 저장
 */
public interface ImageStorage {

    /** key 위치에 파일 저장, 브라우저에서 열 수 있는 주소 반환 */
    String upload(String key, byte[] bytes, String contentType);

    /** upload 가 반환한 주소의 파일 삭제. 이 보관소 주소가 아니면 무시 */
    void delete(String url);
}
