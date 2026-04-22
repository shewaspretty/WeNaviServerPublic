package com.hrlee.transnaviserver.springboot.dto.rest.token;

import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class AccessTokenResponse extends AbstractRestResponse {

    @Nullable
    private final String accessToken;

    public AccessTokenResponse(@Nullable String accessToken) {
        if(accessToken == null) {
            this.accessToken = null;
            setErrorMsg("refresh token expired");
            return;
        }
        this.accessToken = accessToken;
    }
}
