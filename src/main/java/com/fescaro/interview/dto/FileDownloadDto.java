package com.fescaro.interview.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.UrlResource;

@Getter
@Setter
@Builder
public class FileDownloadDto {
    private UrlResource urlResource;
    private String fileName;
}
