package com.hrlee.transnaviserver.springboot.dto.rest.user.nickname;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
public class UserNicknameEditRequest {

    /**
     * without making it final is INTENDED as Jackson tends to use DefaultConstructor and setter even the constructor with parameters matched exists when trying to deserialize the class having one parameter.
     */
    private String nickname;
}
