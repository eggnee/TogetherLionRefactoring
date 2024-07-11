package com.arin.togetherlion.copurchasing.controller;

import com.arin.togetherlion.common.CustomException;
import com.arin.togetherlion.common.ErrorCode;
import com.arin.togetherlion.copurchasing.domain.dto.CopurchasingCreateRequest;
import com.arin.togetherlion.copurchasing.domain.dto.ParticipationCreateRequest;
import com.arin.togetherlion.copurchasing.domain.dto.ParticipationDeleteRequest;
import com.arin.togetherlion.copurchasing.service.CopurchasingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CopurchasingController.class)
@MockBean(JpaMetamodelMappingContext.class)
class CopurchasingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CopurchasingService copurchasingService;

    @InjectMocks
    private CopurchasingController copurchasingController;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("/coupurchasing 유효한 post 요청 시 201 응답을 반환한다.")
    void postSuccess() throws Exception {
        Long copurchasingId = 1L;
        CopurchasingCreateRequest validRequest = CopurchasingCreateRequest.builder()
                .title("title")
                .productTotalCost(10000)
                .shippingCost(5000)
                .productUrl("url")
                .productMinNumber(1)
                .productMaxNumber(10)
                .deadlineDate(LocalDateTime.now().plusDays(3))
                .tradeDate(LocalDateTime.now().plusDays(7))
                .writerId(1L)
                .build();

        // 모킹된 서비스 동작 정의
        when(copurchasingService.create(any(CopurchasingCreateRequest.class))).thenReturn(copurchasingId);

        // 요청 및 응답 검증
        mockMvc.perform(post("/copurchasings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/copurchasings/" + copurchasingId));
    }

    @Test
    @DisplayName("/coupurchasing 유효한 post 요청이 아닐 시 400 응답을 반환한다.")
    void postFail() throws Exception {
        Long copurchasingId = 1L;
        CopurchasingCreateRequest invalidRequest = CopurchasingCreateRequest.builder()
                .productTotalCost(10000)
                .shippingCost(5000)
                .productMinNumber(1)
                .deadlineDate(LocalDateTime.now().plusDays(3))
                .tradeDate(LocalDateTime.now().plusDays(7))
                .writerId(1L)
                .build();

        // 요청 및 응답 검증
        mockMvc.perform(post("/copurchasings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("/coupurchasing 유효한 delete 요청 시 204 응답을 반환한다.")
    void deleteSuccess() throws Exception {
        Long copurchasingId = 1L;
        Long userId = 2L;

        // 서비스 계층 모킹 설정
        doNothing().when(copurchasingService).delete(userId, copurchasingId);

        mockMvc.perform(delete("/copurchasings/{copurchasingId}", copurchasingId)
                        .contentType("application/json")
                        .content(userId.toString()))
                .andExpect(status().isNoContent());

        verify(copurchasingService).delete(userId, copurchasingId);
    }

    @Test
    @DisplayName("/coupurchasing 유효한 delete 요청이 아닐 시 401 응답을 반환한다.")
    void deleteFail() throws Exception {
        Long copurchasingId = 1L;
        Long userId = 2L;

        doThrow(new CustomException(ErrorCode.NO_PERMISSION)).when(copurchasingService).delete(anyLong(), anyLong());

        mockMvc.perform(delete("/copurchasings/{copurchasingId}", copurchasingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userId.toString()))
                .andExpect(status().isUnauthorized());

        verify(copurchasingService).delete(userId, copurchasingId);
    }

    @Test
    @DisplayName("/coupurchasing/participate 유효한 post 요청 시 201 응답을 반환한다.")
    void participateSuccess() throws Exception {
        Long copurchasingId = 1L;
        Long participantId = 2L;
        ParticipationCreateRequest validRequest = ParticipationCreateRequest.builder()
                .copurchasingId(copurchasingId)
                .purchaseNumber(1)
                .participantId(participantId)
                .build();

        // 모킹된 서비스 동작 정의
        when(copurchasingService.participationCreate(any(ParticipationCreateRequest.class))).thenReturn(copurchasingId);

        // 요청 및 응답 검증
        mockMvc.perform(post("/copurchasings/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/copurchasings/" + copurchasingId));
    }

    @Test
    @DisplayName("/coupurchasing/participate 유효한 post 요청이 아닐 시 201 요청을 반환한다. (@Valid 유효성 검사)")
    void participateFail() throws Exception {
        Long copurchasingId = 1L;
        Long participantId = 2L;
        ParticipationCreateRequest invalidRequest = ParticipationCreateRequest.builder()
                .copurchasingId(copurchasingId)
                .participantId(participantId)
                .build();

        // 요청 및 응답 검증
        mockMvc.perform(post("/copurchasings/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("/coupurchasing/participate 유효한 delete 요청 시 204 응답을 반환한다.")
    void deleteParticipationSuccess() throws Exception {
        Long participationId = 1L;
        Long deleterId = 2L;
        ParticipationDeleteRequest validRequest = ParticipationDeleteRequest.builder()
                .participationId(participationId)
                .deleterId(deleterId)
                .build();

        // 서비스 계층 모킹 설정
        doNothing().when(copurchasingService).participationDelete(validRequest);

        mockMvc.perform(delete("/copurchasings/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNoContent());

        verify(copurchasingService).participationDelete(refEq(validRequest));
    }

    @Test
    @DisplayName("/coupurchasing/participate 유효하지 않은 delete 요청 시 204 응답을 반환한다.")
    void deleteParticipationFail() throws Exception {
        Long participationId = 1L;
        Long deleterId = 2L;
        ParticipationDeleteRequest validRequest = ParticipationDeleteRequest.builder()
                .participationId(participationId)
                .deleterId(deleterId)
                .build();

        // 서비스 계층 모킹 설정
        doThrow(new CustomException(ErrorCode.NO_PERMISSION)).when(copurchasingService).participationDelete(any(ParticipationDeleteRequest.class));

        mockMvc.perform(delete("/copurchasings/participate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());

        verify(copurchasingService).participationDelete(any(ParticipationDeleteRequest.class));
    }
}