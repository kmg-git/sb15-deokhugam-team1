package com.example.sb15deokhugamteam1.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/** 로컬 폴더 보관소일 때만 저장 이미지를 /files/** 주소로 제공 */
@Configuration
public class LocalFileWebConfig implements WebMvcConfigurer {

    private final boolean s3Enabled;
    private final String localDir;

    public LocalFileWebConfig(@Value("${storage.s3.bucket:}") String bucket,
                              @Value("${storage.local-dir:uploads}") String localDir) {
        this.s3Enabled = bucket != null && !bucket.isBlank();
        this.localDir = localDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (s3Enabled) {
            return;
        }
        String location = Path.of(localDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler(LocalImageStorage.URL_PREFIX + "**")
                .addResourceLocations(location.endsWith("/") ? location : location + "/");
    }
}
