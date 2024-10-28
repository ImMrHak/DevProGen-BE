package com.devprogen.application.generator.record.response;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public record GeneratedProjectDTO(
        HttpHeaders headers,
        Long contentLength,
        MediaType mediaType,
        InputStreamResource projectGenerated
) {
}
