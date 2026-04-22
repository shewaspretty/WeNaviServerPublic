package com.hrlee.transnaviserver.springboot.dto.rest.token;

import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class TokenSetResponse extends AbstractRestResponse {

    @Nullable
    private final String accessToken;
    @Nullable
    private final String refreshToken;

    public TokenSetResponse(String[] tokens) {
        if(tokens == null || tokens.length != 2) {
            setErrorMsg("사용자를 찾을 수 없습니다");
            accessToken = null;
            refreshToken = null;
            return;
        }

        accessToken = tokens[0];
        refreshToken = tokens[1];
    }
}
