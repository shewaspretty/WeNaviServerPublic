package com.hrlee.transnaviserver.springboot.dto.rest;

import lombok.Getter;

@Getter
public abstract class AbstractRestResponse {

    private String errorMsg;

    protected AbstractRestResponse() { errorMsg = null; }

    protected AbstractRestResponse(String errorMsg) {
        if(errorMsg == null) {
            return;
        }
        this.errorMsg = errorMsg;
    }

    protected void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
