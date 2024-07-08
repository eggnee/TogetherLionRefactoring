package com.arin.togetherlion.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e){
        return ResponseEntity.status(e.getErrorCode().getHttpStatus()).body(ErrorResponse.from(e.getErrorCode()));
    }
}
