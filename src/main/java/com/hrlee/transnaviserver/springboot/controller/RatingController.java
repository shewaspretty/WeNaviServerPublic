package com.hrlee.transnaviserver.springboot.controller;

import com.hrlee.transnaviserver.springboot.dto.rest.*;
import com.hrlee.transnaviserver.springboot.dto.rest.rate.RatingRequest;
import com.hrlee.transnaviserver.springboot.service.rating.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/place/{poiId}/rate")
    public ResponseEntity<AbstractRestResponse> addOrUpdateRating(@PathVariable String poiId,
                                                                  @RequestBody RatingRequest request) {
        return ResponseEntity.ok().body(ratingService.addOrUpdateRating(request, Integer.parseInt(poiId)));
    }
    @GetMapping("/place/{poiId}/rate")
    public ResponseEntity<AbstractRestResponse> getRatings(@PathVariable("poiId") String poiId) {
        return ResponseEntity.ok().body(ratingService.getRatingsByPoiId(Integer.parseInt(poiId)));
    }

    @DeleteMapping("/user/{usrId}/rate/place/{poiId}")
    public ResponseEntity<ErrorResponse> deleteRating(@PathVariable("usrId")String usrId, @PathVariable("poiId") String poiId) {

        return ResponseEntity.ok().body(ratingService.removeRating(usrId, Integer.parseInt(poiId)));
    }
}
