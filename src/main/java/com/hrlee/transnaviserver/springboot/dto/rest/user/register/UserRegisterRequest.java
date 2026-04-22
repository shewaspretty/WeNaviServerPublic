package com.hrlee.transnaviserver.springboot.dto.rest.user.register;

import com.hrlee.transnaviserver.springboot.CorruptAble;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserRegisterRequest implements CorruptAble {
    private final String id;
    private final String password;

    @Override
    public boolean isCorrupt() {
        return id == null || id.equals("root") || password == null;
    }
}
