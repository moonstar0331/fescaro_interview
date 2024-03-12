package com.fescaro.interview.service;

import com.fescaro.interview.dto.FileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MainService {
    Page<FileDto> findAllFiles(Pageable pageable);
}
