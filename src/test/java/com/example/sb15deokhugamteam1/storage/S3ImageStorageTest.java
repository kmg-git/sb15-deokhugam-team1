package com.example.sb15deokhugamteam1.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class S3ImageStorageTest {

    @Mock
    S3Client s3Client;

    private S3ImageStorage storage() {
        return new S3ImageStorage(s3Client, "deokhugam-thumbnails", "ap-northeast-2");
    }

    @Test
    void 버킷에_올리고_공개_주소를_돌려준다() {
        String url = storage().upload("books/a.png", new byte[]{1, 2}, "image/png");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().bucket()).isEqualTo("deokhugam-thumbnails");
        assertThat(captor.getValue().key()).isEqualTo("books/a.png");
        assertThat(captor.getValue().contentType()).isEqualTo("image/png");
        assertThat(url).isEqualTo("https://deokhugam-thumbnails.s3.ap-northeast-2.amazonaws.com/books/a.png");
    }

    @Test
    void S3_오류면_ImageUploadException() {
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .willThrow(SdkClientException.create("no credentials"));

        assertThatThrownBy(() -> storage().upload("books/a.png", new byte[]{1}, "image/png"))
                .isInstanceOf(ImageUploadException.class);
    }

    @Test
    void 자기_버킷_주소면_키를_뽑아서_지운다() {
        storage().delete("https://deokhugam-thumbnails.s3.ap-northeast-2.amazonaws.com/books/a.png");

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo("deokhugam-thumbnails");
        assertThat(captor.getValue().key()).isEqualTo("books/a.png");
    }

    @Test
    void 다른_주소는_지우지_않고_삭제_실패도_예외를_던지지_않는다() {
        storage().delete("https://other.com/books/a.png");
        storage().delete(null);
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));

        given(s3Client.deleteObject(any(DeleteObjectRequest.class))).willThrow(SdkClientException.create("down"));
        storage().delete("https://deokhugam-thumbnails.s3.ap-northeast-2.amazonaws.com/books/a.png");
    }
}
