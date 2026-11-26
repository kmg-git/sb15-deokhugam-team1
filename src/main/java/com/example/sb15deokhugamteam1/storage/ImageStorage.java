package com.example.sb15deokhugamteam1.storage;

/**
 * 이미지 파일 보관소. S3 버킷이 설정돼 있으면 S3, 아니면 내 컴퓨터 폴더에 저장한다
 */
public interface ImageStorage {

    /** key 위치에 파일을 저장하고, 브라우저에서 열 수 있는 주소를 돌려준다 */
    String upload(String key, byte[] bytes, String contentType);

    /** upload 가 돌려준 주소의 파일을 지운다. 이 보관소 주소가 아니면 아무것도 하지 않는다 */
    void delete(String url);
}
