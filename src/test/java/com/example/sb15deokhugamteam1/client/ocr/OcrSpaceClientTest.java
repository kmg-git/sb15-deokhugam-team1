package com.example.sb15deokhugamteam1.client.ocr;

import com.example.sb15deokhugamteam1.exception.book.OcrFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OcrSpaceClientTest {

    private static final String URL = "https://api.ocr.space/parse/image";
    private static final byte[] IMAGE = {1, 2, 3};

    private RestClient.Builder builder;
    private MockRestServiceServer server;
    private OcrSpaceClient client;

    @BeforeEach
    void setUp() {
        builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OcrSpaceClient(builder.build(), new OcrSpaceProperties("test-key", null));
    }

    @Test
    void 이미지를_키와_함께_보내고_읽은_글자를_합쳐서_돌려준다() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", "test-key"))
                .andExpect(content().string(containsString("filename=\"book.jpg\"")))
                .andRespond(withSuccess("""
                        {"ParsedResults":[{"ParsedText":"ISBN 978-89-6626-095-9"},{"ParsedText":"인사이트"}],
                         "OCRExitCode":1,"IsErroredOnProcessing":false,"ProcessingTimeInMilliseconds":"500"}
                        """, MediaType.APPLICATION_JSON));

        String text = client.parseText(IMAGE, "book.jpg");

        assertThat(text).isEqualTo("ISBN 978-89-6626-095-9\n인사이트");
        server.verify();
    }

    @Test
    void 읽은_결과가_없으면_빈_문자열() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("""
                        {"OCRExitCode":1,"IsErroredOnProcessing":false}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.parseText(IMAGE, "book.jpg")).isEmpty();
    }

    @Test
    void OCR_처리_오류면_OcrFailedException() {
        server.expect(requestTo(URL))
                .andRespond(withSuccess("""
                        {"OCRExitCode":3,"IsErroredOnProcessing":true,
                         "ErrorMessage":["File size exceeds the maximum permissible file size limit of 1024 KB"]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.parseText(IMAGE, "book.jpg"))
                .isInstanceOf(OcrFailedException.class);
    }

    @Test
    void 서버_오류면_OcrFailedException() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.parseText(IMAGE, "book.jpg"))
                .isInstanceOf(OcrFailedException.class);
    }

    @Test
    void 키가_없으면_호출하지_않고_OcrFailedException() {
        OcrSpaceClient noKey = new OcrSpaceClient(builder.build(), new OcrSpaceProperties(" ", null));

        assertThatThrownBy(() -> noKey.parseText(IMAGE, "book.jpg"))
                .isInstanceOf(OcrFailedException.class);
        server.verify();
    }
}
