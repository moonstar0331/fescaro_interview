package com.fescaro.interview.service.impl;

import com.fescaro.interview.dto.FileDto;
import com.fescaro.interview.repository.FileRepository;
import com.fescaro.interview.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    private final FileRepository fileRepository;

    /**
     * 모든 파일 정보들을 페이징 처리하여 반환한다.
     * @param pageable
     * @return 엔티티 정보를 담고 있는 DTO 리스트를 반환
     */
    @Override
    public Page<FileDto> findAllFiles(Pageable pageable) {
        return fileRepository.findAll(pageable).map(FileDto::from);
    }
}
