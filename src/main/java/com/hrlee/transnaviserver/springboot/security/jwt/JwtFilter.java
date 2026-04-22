package com.hrlee.transnaviserver.springboot.security.jwt;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.security.AuthorityHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.ArrayList;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter implements LoggAble {

    private final JwtSecretKeyHolder jwtSecretKeyHolder;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        processFilter(request);
        filterChain.doFilter(request, response);
    }

    private void processFilter(HttpServletRequest request) {
        String rawToken = getRawTokenFromHeader(request);
        if(rawToken == null)
            return;

        Claims tokenClaims = verifyAndGetClaims(rawToken);
        if(tokenClaims == null)
            return;

        Authentication authentication = getAuthentication(rawToken, tokenClaims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nullable
    private String getRawTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(authHeader == null)
            return null;

        int authHeaderPtr = -1;
        while(++authHeaderPtr < authHeader.length() && authHeader.charAt(authHeaderPtr) != ' ') {}

        if(authHeaderPtr == authHeader.length())
            return null;

        if(!authHeader.substring(0, authHeaderPtr).equals("Bearer"))
            return null;

        return authHeader.substring(authHeaderPtr);
    }

    @Nullable
    public Claims verifyAndGetClaims(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(jwtSecretKeyHolder.getSecretKey()).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return null;
        }
        return claims;
    }

    private Authentication getAuthentication(String token, Claims tokenClaims) {
        ArrayList<SimpleGrantedAuthority> grantedAuthoritySet = new ArrayList<>();
        String usrId = tokenClaims.get("usrId").toString();

        if(usrId.equals("root"))
            grantedAuthoritySet.add(new SimpleGrantedAuthority(AuthorityHolder.ROOT_AUTHORITY));
        grantedAuthoritySet.add(new SimpleGrantedAuthority(AuthorityHolder.DEFAULT_USER_AUTHORITY));

        return new UsernamePasswordAuthenticationToken(usrId, tokenClaims.getSubject(), grantedAuthoritySet);
    }
}
