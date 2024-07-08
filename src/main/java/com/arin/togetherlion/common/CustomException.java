package com.arin.togetherlion.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomException extends RuntimeException {
    ErrorCode errorCode;
}
