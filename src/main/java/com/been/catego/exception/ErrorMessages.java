package com.been.catego.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessages {

    FAIL_TO_LOAD_YOUTUBE_DATA("유튜브 데이터를 가져오는 과정에서 문제가 발생하였습니다."),
    NOT_FOUND_FOLDER("존재하지 않는 폴더입니다.");

    private final String message;
}
