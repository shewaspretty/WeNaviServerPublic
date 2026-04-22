package com.hrlee.transnaviserver.springboot.dto.rest.user.nickname;

import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class UserNicknameResponse extends AbstractRestResponse {

    @Nullable
    private final String nickname;

    public UserNicknameResponse(@Nullable String nickname) {
        this.nickname = nickname;
    }

}
