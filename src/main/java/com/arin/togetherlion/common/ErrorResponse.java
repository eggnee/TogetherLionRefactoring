package com.arin.togetherlion.common;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;

    public static ErrorResponse from(ErrorCode e){
        return new ErrorResponse(e.getMessage());
    }
}