package com.hrlee.transnaviserver.springboot.dto.rest.user.register;

import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class UserRegisterResponse extends AbstractRestResponse {

    public UserRegisterResponse(ErrorCode errorCode) {
        super(errorCode.errorMsg);
    }

    @RequiredArgsConstructor
    public static enum ErrorCode {
        SUCCESS(null),
        ERROR("사용자 등록에 실패 하였습니다."),
        DUPLICATED("중복된 사용자 ID 입니다. ");

        private final String errorMsg;

    }
}
