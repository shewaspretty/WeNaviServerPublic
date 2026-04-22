package com.hrlee.transnaviserver.springboot.service.rating;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.dto.rest.AbstractRestResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.ErrorResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.rate.RatingSetResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.rate.RatingSuccessResponse;
import com.hrlee.transnaviserver.springboot.dto.rest.rate.RatingRequest;
import com.hrlee.transnaviserver.springboot.service.user.UserService;
import lombok.NonNull;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SimplePropertyRowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RatingService implements LoggAble {

    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;

    @NonNull
    public AbstractRestResponse addOrUpdateRating(RatingRequest request, int poiId) {
        if(request.isCorrupt())
            return new ErrorResponse("잘못된 정보 입니다.");

        String currentUsrId = userService.getUserIdInCurrentSecurityContext();
        if(currentUsrId == null)
            return new ErrorResponse("인증 오류");

        long currentTimeMs = System.currentTimeMillis();
        jdbcTemplate.update(
                "INSERT INTO place_rate VALUES( " +
                        "\"" + currentUsrId + "\" , " + poiId + " , " + request.getGrade() + " , \"" + request.getComment() + "\" , " + currentTimeMs +
                        ") " +
                        "ON DUPLICATE KEY UPDATE grade = VALUES(grade), comment = VALUES(comment), post_time = VALUES(post_time)");
        return new RatingSuccessResponse(request.getGrade(), request.getComment(), currentTimeMs);
    }

    @NonNull
    public AbstractRestResponse getRatingsByPoiId(int poiId) {
        if(poiId < 0)
            return new ErrorResponse("잘못된 접근 입니다.");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = null;
        if(authentication != null)
            currentUserId = authentication.getName();

        RatingSetResponse ratingSetResponse =
                jdbcTemplate.query("WITH PlaceRate AS (" +
                        "SELECT *, AVG(grade) OVER() AS avg_grade FROM place_rate WHERE poi_id=" + poiId + " ORDER BY post_time DESC " +
                        ")" +
                        "SELECT p.*, user.nickname FROM PlaceRate AS p " +
                        "INNER JOIN user ON user.id=p.user_id ", new PoiIdRatingsFactory(currentUserId));

        if(ratingSetResponse == null)
            return new RatingSetResponse(-1.0f, null, -1, null, -1);
        return ratingSetResponse;
    }

    @NonNull
    public ErrorResponse removeRating(String usrId, int poiId) {
        if(usrId == null )
            return new ErrorResponse("인증 오류");

        String currentSessionUsrId = userService.getUserIdInCurrentSecurityContext();
        if(currentSessionUsrId == null)
            return new ErrorResponse("인증 오류");

        if(!currentSessionUsrId.equals(usrId))
            return new ErrorResponse("인증 오류");

        if(jdbcTemplate.update("DELETE FROM place_rate WHERE user_id=\"" + usrId + "\" AND poi_id=" + poiId) == 1)
            return new ErrorResponse(null);
        return new ErrorResponse("삭제할 평점이 없습니다. ");
    }

}
