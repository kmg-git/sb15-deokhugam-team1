package com.example.sb15deokhugamteam1.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IsbnExtractorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "ISBN 978-89-6626-095-9",
            "9 788966 260959",
            "값 33,000원\nISBN978-8966260959 93000",
            "9788966260959"
    })
    void 여러_모양의_ISBN을_찾는다(String text) {
        assertThat(IsbnExtractor.extract(text)).contains("9788966260959");
    }

    @Test
    void 체크_숫자가_틀린_후보는_건너뛰고_맞는_것을_찾는다() {
        String text = "9788966260958 잘못 읽힘\nISBN 979-11-6224-187-5";

        assertThat(IsbnExtractor.extract(text)).contains("9791162241875");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"클린 코드 로버트 C. 마틴", "1234567890123", "9788966260958"})
    void ISBN이_없으면_빈_값(String text) {
        assertThat(IsbnExtractor.extract(text)).isEmpty();
    }
}
