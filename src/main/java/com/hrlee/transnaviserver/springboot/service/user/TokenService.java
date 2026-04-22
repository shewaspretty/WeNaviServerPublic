package com.hrlee.transnaviserver.springboot.service.user;

import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.ErrorResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.token.AccessTokenResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.token.TokenSetRequest;
import com.hrlee.transnaviserver.springboot.entity.User;
import com.hrlee.transnaviserver.springboot.repository.jdbc.UserRepository;
import com.hrlee.transnaviserver.springboot.security.jwt.TokenFactory;
import com.hrlee.transnaviserver.springboot.entity.jpa.UserRegistered;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SimplePropertyRowMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenFactory tokenFactory;
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Nullable
    public String[] issueNewTokens(TokenSetRequest request) {
        if(request.isCorrupt())
            return null;

        String usrId = request.getId();
        String password = request.getPassword();

        User usrFound = userRepository.getUser(usrId);
        if(usrFound == null)
            return null;

        if(!bCryptPasswordEncoder.matches(password, usrFound.getPassword()))
            return null;

        return tokenFactory.generateNewTokens(usrId);
    }

    @Nullable
    public AbstractRestResponse issueAccessToken() {
        if(SecurityContextHolder.getContext().getAuthentication().getCredentials().equals(TokenFactory.Type.ACCESS.getSubject()))
            return new ErrorResponse("인증 오류");
        return new AccessTokenResponse(tokenFactory.generateAccessToken(SecurityContextHolder.getContext().getAuthentication().getName()));
    }
}
