package com.example.baby_doyeop.controller;

import com.example.baby_doyeop.dto.BookResponse;
import com.example.baby_doyeop.dto.CreateBookRequest;
import com.example.baby_doyeop.exception.CategoryNotFoundException;
import com.example.baby_doyeop.exception.DuplicateBookTitleException;
import com.example.baby_doyeop.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    private static final BookResponse NEW_BOOK =
            new BookResponse(4L, "봄의 정원", "시집", "문학", true);

    @Test
    void GET_books는_DTO_필드명으로_응답한다() throws Exception {
        given(bookService.getBooks(null)).willReturn(List.of(NEW_BOOK));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(4))
                .andExpect(jsonPath("$[0].categoryName").value("문학"))
                .andExpect(jsonPath("$[0].isAvailable").value(true))
                .andExpect(jsonPath("$[0].category_id").doesNotExist());
    }

    @Test
    void POST_books는_201과_생성된_도서를_반환한다() throws Exception {
        given(bookService.createBook(any(CreateBookRequest.class))).willReturn(NEW_BOOK);

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"title\":\"봄의 정원\",\"description\":\"시집\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(4))
                .andExpect(jsonPath("$.title").value("봄의 정원"));
    }

    @Test
    void 빈_제목과_없는_categoryId는_400과_필드별_오류를_반환한다() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("title")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("categoryId")));

        verify(bookService, never()).createBook(any());
    }

    @Test
    void 숫자가_아닌_categoryId는_400을_반환한다() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":\"문학\",\"title\":\"새 책\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
    }

    @Test
    void 존재하지_않는_카테고리는_404를_반환한다() throws Exception {
        given(bookService.createBook(any(CreateBookRequest.class)))
                .willThrow(new CategoryNotFoundException(999L));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":999,\"title\":\"새 책\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    void 중복_제목은_409를_반환한다() throws Exception {
        given(bookService.createBook(any(CreateBookRequest.class)))
                .willThrow(new DuplicateBookTitleException("달빛 도서관"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"categoryId\":1,\"title\":\"달빛 도서관\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_BOOK_TITLE"));
    }
}
