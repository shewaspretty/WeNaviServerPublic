package com.hrlee.transnaviserver.springboot.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorityHolder {

    public static final String DEFAULT_USER_AUTHORITY = "DEFAULT_USER";
    public static final String ROOT_AUTHORITY = "ADMIN_USER";
}
