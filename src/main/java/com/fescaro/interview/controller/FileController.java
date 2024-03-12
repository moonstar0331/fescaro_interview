package com.fescaro.interview.controller;

import com.fescaro.interview.dto.FileDownloadDto;
import com.fescaro.interview.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
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
        fileService.uploadProcess(multipartFile);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    /**
     * 원본 및 암호화된 파일 다운로드를 수행한다.
     * @param fileName 다운로드 하고자 하는 원본 및 암호화 파일명
     * @param type 다운로드 하고자 하는 파일의 타입
     * @return 다운로드 하는 파일의 리소스를 반환한다.
     * @throws MalformedURLException 잘못된 프로토콜 또는 파일의 경로의 경우 발생
     */
    @GetMapping("/download/{fileName}")
    @ResponseBody
    public ResponseEntity<Resource> download(@PathVariable String fileName, @RequestParam String type) throws MalformedURLException {
        FileDownloadDto fileDownloadDto = fileService.fileDownload(fileName, type);
        String contentDisposition = "attachment; filename=\"" + fileDownloadDto.getFileName() + "\"";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(fileDownloadDto.getUrlResource());
    }
}
