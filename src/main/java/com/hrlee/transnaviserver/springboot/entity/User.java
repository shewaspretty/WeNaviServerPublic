package com.hrlee.transnaviserver.springboot.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class User {

    private String id;
    private String password;
    private String nickname;
}
