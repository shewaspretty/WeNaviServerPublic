package com.hrlee.transnaviserver.springboot.dto.rest.rate;

import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RatingSuccessResponse extends AbstractRestResponse {
    private final int grade;
    private final String comment;
    private final long postedTimeMs;

}
