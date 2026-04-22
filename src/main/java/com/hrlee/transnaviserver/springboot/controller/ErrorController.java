package com.hrlee.transnaviserver.springboot.controller;

import com.hrlee.transnaviserver.springboot.LoggAble;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.hrlee.transnaviserver.springboot.dto.rest.ErrorResponse;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorController implements LoggAble {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> onHandleException(Exception e) {
        getLogger().error(e.toString());
        return ResponseEntity.internalServerError().build();
    }
}
