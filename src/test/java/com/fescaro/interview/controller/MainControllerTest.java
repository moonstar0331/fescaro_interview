package com.fescaro.interview.controller;

import com.fescaro.interview.service.MainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MainControllerTest {

    private final MockMvc mvc;

    @MockBean
    private MainService mainService;

    public MainControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @DisplayName("[GET] 메인 페이지 요청 - 정상 호출")
    @Test
    void 루트경로_메인페이지호출() throws Exception {
        // given
        given(mainService.findAllFiles(any(Pageable.class))).willReturn(Page.empty());

        // when & then
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("curPage"))
                .andExpect(model().attributeExists("totalPage"))
                .andExpect(model().attributeExists("fileInfos"))
                .andDo(print());
    }
}