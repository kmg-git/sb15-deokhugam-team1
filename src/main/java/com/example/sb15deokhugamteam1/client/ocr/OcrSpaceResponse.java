package com.example.sb15deokhugamteam1.client.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OCR.space 응답에서 필요한 값만 받는다
 * ErrorMessage 는 문자열 또는 문자열 배열로 와서 Object 로 받는다
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OcrSpaceResponse(
        @JsonProperty("ParsedResults") List<ParsedResult> parsedResults,
        @JsonProperty("IsErroredOnProcessing") boolean erroredOnProcessing,
        @JsonProperty("ErrorMessage") Object errorMessage
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParsedResult(
            @JsonProperty("ParsedText") String parsedText
    ) {
    }
}
