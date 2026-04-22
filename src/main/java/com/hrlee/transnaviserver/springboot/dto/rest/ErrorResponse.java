package com.hrlee.transnaviserver.springboot.dto.rest;

public class ErrorResponse extends AbstractRestResponse {

    public ErrorResponse(String errorMsg) {
        super(errorMsg);
    }
}
