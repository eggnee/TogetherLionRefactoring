package com.arin.togetherlion.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    CANT_JOIN(HttpStatus.BAD_REQUEST, "ACCOUNT-001", "작성자는 본인의 공동구매에 참여할 수 없습니다."),
    NO_PERMISSION(HttpStatus.UNAUTHORIZED, "ACCOUNT-002", "삭제 권한이 없습니다.");

    private final HttpStatus httpStatus;	// HttpStatus
    private final String code;				// ACCOUNT-001
    private final String message;			// 설명

    }