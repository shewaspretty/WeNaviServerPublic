package com.hrlee.transnaviserver.springboot.dto.route;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class Tag {

    private final String key;
    private final String value;
}
