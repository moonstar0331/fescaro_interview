package com.fescaro.interview.service;

import com.fescaro.interview.dto.FileDto;
import com.fescaro.interview.repository.FileRepository;
import com.fescaro.interview.service.impl.MainServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MainServiceTest {

    @InjectMocks private MainServiceImpl mainService;

    @Mock private FileRepository fileRepository;

    @DisplayName("암호화 이력을 조회하면, 페이징 된 암호화 이력을 반환한다.")
    @Test
    void test() {
        // given
        Pageable pageable = Pageable.ofSize(5);
        given(fileRepository.findAll(pageable)).willReturn(Page.empty());

        // when
        Page<FileDto> files = mainService.findAllFiles(pageable);

        // then
        assertThat(files).isEmpty();
        then(fileRepository).should().findAll(pageable);
    }
}