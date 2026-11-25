package com.example.sb15deokhugamteam1.exception.book;

import java.util.Map;

/** OCR.space 호출 자체가 실패했을 때 (키 없음, 인증 실패, 타임아웃 등) → 502 */
public class OcrFailedException extends BookException {

    public OcrFailedException() {
        super(BookErrorCode.OCR_FAILED, Map.of());
    }
}
