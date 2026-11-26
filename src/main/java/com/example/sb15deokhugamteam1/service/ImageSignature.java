package com.example.sb15deokhugamteam1.service;

/**
 * 파일 맨 앞 몇 바이트(시그니처)로 진짜 이미지인지 확인한다
 * Content-Type 은 보내는 쪽이 마음대로 적을 수 있어서 믿을 수 없다
 * OCR.space 가 받는 형식만 허용: JPEG, PNG, GIF, BMP, TIFF, WEBP
 */
final class ImageSignature {

    private ImageSignature() {
    }

    static boolean isSupported(byte[] b) {
        return startsWith(b, 0xFF, 0xD8, 0xFF)                     // JPEG
                || startsWith(b, 0x89, 'P', 'N', 'G')              // PNG
                || startsWith(b, 'G', 'I', 'F', '8')               // GIF
                || startsWith(b, 'B', 'M')                         // BMP
                || startsWith(b, 'I', 'I', 0x2A, 0x00)             // TIFF (인텔)
                || startsWith(b, 'M', 'M', 0x00, 0x2A)             // TIFF (모토로라)
                || (startsWith(b, 'R', 'I', 'F', 'F') && b.length >= 12
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P'); // WEBP
    }

    private static boolean startsWith(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((bytes[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
