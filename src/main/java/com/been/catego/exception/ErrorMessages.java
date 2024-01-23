package com.been.catego.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessages {

    IO_EXCEPTION("입출력 오류 발생");

    private final String message;
}
