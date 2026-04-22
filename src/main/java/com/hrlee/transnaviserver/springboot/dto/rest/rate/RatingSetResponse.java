package com.hrlee.transnaviserver.springboot.dto.rest.rate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RatingSetResponse extends AbstractRestResponse {

    private final float avgGrade;
    @Nullable
    private final List<RatingExposable> ratings;

    private final int gradeByUser;
    @Nullable
    private final String commentByUser;
    private final int userRatingIndex;

    @RequiredArgsConstructor
    @Getter
    public static class RatingExposable {
        @NonNull
        private final String nickname;
        private final int grade;
        @NonNull
        private final String comment;
        private final long postTime;
    }
}
