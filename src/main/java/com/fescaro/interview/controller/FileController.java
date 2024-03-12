package com.fescaro.interview.controller;

import com.fescaro.interview.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileController {

    private final FileService fileService;

    /**
     * 원본 파일 업로드 시에 원본 파일 저장 + 암호화 + 암호화된 파일 저장을 수행한다.
     * @param multipartFile 업로드한 원본 파일
     * @return 메인 페이지로 리다이렉트를 수행한다.
     * @throws Exception 파일 IO 에러 등의 경우 발생
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestPart(value = "file") MultipartFile multipartFile) throws Exception {
        fileService.fileUpload(multipartFile);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }
}
