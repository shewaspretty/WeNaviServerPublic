package com.hrlee.transnaviserver.springboot.service.rating;

import com.hrlee.transnaviserver.springboot.dto.rest.rate.RatingSetResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Designed to be used once by instantiated.
 */
@RequiredArgsConstructor
public class PoiIdRatingsFactory implements ResultSetExtractor<RatingSetResponse> {

    @Nullable
    private final String currentUserId;

    @Nullable
    @Override
    public RatingSetResponse extractData(ResultSet rs) throws SQLException, DataAccessException {
        ArrayList<RatingSetResponse.RatingExposable> ratingExposableList = new ArrayList<>();

        float avgGrade = -1.0f;
        int gradeByUser = -1;
        String commentByUser = null;
        int userRatingIndex = -1;

        String userId = null;
        String nickname = null;
        int poiId = -1;
        int grade = -1;
        String comment = null;
        long postTime = -1;

        while(rs.next()) {
            userId = rs.getString("user_id");
            nickname = rs.getString("nickname");
            avgGrade = rs.getFloat("avg_grade");
            poiId = rs.getInt("poi_id");
            grade = rs.getInt("grade");
            comment = rs.getString("comment");
            postTime = rs.getLong("post_time");

            ratingExposableList.add(new RatingSetResponse.RatingExposable(nickname, grade, comment, postTime));

            if(currentUserId == null)
                continue;

            if(gradeByUser > -1 || commentByUser != null)
                continue;
            if(!currentUserId.equals(userId))
                continue;

            gradeByUser = grade;
            commentByUser = comment;
            userRatingIndex = ratingExposableList.size() -1;
        }

        if(ratingExposableList.isEmpty())
            return null;

        String roundedDownAvgGrade = new BigDecimal(String.valueOf(avgGrade)).setScale(1, RoundingMode.HALF_UP).toString();
        return new RatingSetResponse(Float.parseFloat(roundedDownAvgGrade), ratingExposableList, gradeByUser, commentByUser, userRatingIndex);
    }
}
