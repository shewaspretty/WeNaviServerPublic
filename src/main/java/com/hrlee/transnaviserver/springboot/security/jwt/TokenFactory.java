package com.hrlee.transnaviserver.springboot.security.jwt;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TokenFactory {

    private static final String ISSUER = "weNaviServer";

    private final JwtSecretKeyHolder jwtSecretKeyHolder;

    public String[] generateNewTokens(String usrId) {
        return new String[]{generateToken(Type.ACCESS, usrId), generateToken(Type.REFRESH, usrId)};
    }

    public String generateAccessToken(String usrId) {
        return generateToken(Type.ACCESS, usrId);
    }

    private String generateToken(Type tokenType, String usrId) {
        Date currentTime = new Date(System.currentTimeMillis());
        Date expirationTime = (Date)currentTime.clone();

        expirationTime.setTime(currentTime.getTime() + tokenType.expiration.toMillis());

        Map<String, Object> claims = new HashMap<>();
        claims.put("usrId", usrId);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(ISSUER)
                .setIssuedAt(currentTime)
                .setExpiration((Date)expirationTime.clone())
                .setSubject(tokenType.subject)
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS512, jwtSecretKeyHolder.getSecretKey())
                .compact();
    }

    @RequiredArgsConstructor
    public static enum Type {
        ACCESS("ACCESS", Duration.ofMinutes(30)),
        REFRESH("REFRESH", Duration.ofDays(15));

        @Getter
        private final String subject;
        private final Duration expiration;
    }

}
