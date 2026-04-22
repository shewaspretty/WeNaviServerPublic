package com.hrlee.transnaviserver.springboot.dto.rest.token;

import com.hrlee.transnaviserver.springboot.CorruptAble;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TokenSetRequest implements CorruptAble {
    private final String id;
    private final String password;

    @Override
    public boolean isCorrupt() {
        return id == null || password == null;
    }
}
