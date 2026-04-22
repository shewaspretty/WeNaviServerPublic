package com.hrlee.transnaviserver.springboot.controller;

import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.token.AccessTokenResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.token.TokenSetResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.token.TokenSetRequest;
import com.hrlee.transnaviserver.springboot.service.user.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/token/all")
    public ResponseEntity<TokenSetResponse> getNewTokens(@RequestBody TokenSetRequest request) {
        return ResponseEntity.ok(new TokenSetResponse(tokenService.issueNewTokens(request)));
    }

    @GetMapping("/token/access")
    public ResponseEntity<AbstractRestResponse> getNewAccessToken() {
        return ResponseEntity.ok(tokenService.issueAccessToken());
    }
}
