package com.hrlee.transnaviserver.springboot.controller;

import com.hrlee.transnaviserver.springboot.dto.rest.*;
import com.hrlee.transnaviserver.springboot.dto.rest.user.nickname.UserNicknameEditRequest;
import com.hrlee.transnaviserver.springboot.dto.rest.user.nickname.UserNicknameResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.user.register.UserRegisterRequest;
import com.hrlee.transnaviserver.springboot.dto.rest.user.register.UserRegisterResponse;
import com.hrlee.transnaviserver.springboot.service.user.TokenService;
import com.hrlee.transnaviserver.springboot.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/user/registration")
    public ResponseEntity<UserRegisterResponse> registerNewUser(@RequestBody UserRegisterRequest request) {
        return ResponseEntity.ok().body(new UserRegisterResponse(userService.registerNewUser(request)));
    }

    @GetMapping("/user/nickname")
    public ResponseEntity<UserNicknameResponse> getUserNickname() {
        return ResponseEntity.ok(new UserNicknameResponse(userService.getUserNickname()));
    }

    @PutMapping("/user/nickname")
    public ResponseEntity<ErrorResponse> editUseNickname(@RequestBody UserNicknameEditRequest request) {
        return ResponseEntity.ok(userService.editUserNickname(request));
    }

}
