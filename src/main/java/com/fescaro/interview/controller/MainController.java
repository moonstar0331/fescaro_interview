package com.fescaro.interview.controller;

import com.fescaro.interview.dto.FileDto;
import com.fescaro.interview.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final MainService mainService;

    /**
     * 메인 페이지를 반환하고, 페이징을 수행한다.
     * @param pageable 페이징 수행을 위한 값
     * @param model Thymeleaf에서 View를 그리기 위해 필요한 값들을 추가
     * @return 반환하고자 하는 View의 이름을 반환한다.
     */
    @GetMapping("/")
    public String home(@PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<FileDto> fileInfos = mainService.findAllFiles(pageable);
        model.addAttribute("curPage", pageable.getPageNumber());
        model.addAttribute("totalPage", fileInfos.getTotalPages());
        model.addAttribute("fileInfos", fileInfos);
        return "index";
    }
}
