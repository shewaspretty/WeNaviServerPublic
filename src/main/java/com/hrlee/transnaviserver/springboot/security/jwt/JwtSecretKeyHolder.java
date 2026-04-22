package com.hrlee.transnaviserver.springboot.security.jwt;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class JwtSecretKeyHolder {

    @Value("${secure.jwt-secure-key}")
    @Getter
    @Setter
    private String secretKey;

}
