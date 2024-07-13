package com.arin.togetherlion.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    CANT_JOIN(HttpStatus.BAD_REQUEST, "공동구매에 이미 참여중 입니다."),
    NO_PERMISSION(HttpStatus.UNAUTHORIZED, "삭제 권한이 없습니다.");

    private final HttpStatus httpStatus;    // HttpStatus
    private final String message;            // 설명
}