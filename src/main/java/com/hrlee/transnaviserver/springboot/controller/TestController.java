package com.hrlee.transnaviserver.springboot.controller;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.service.route.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final RouteService routeService;

    @GetMapping("/debug/test")
    public ResponseEntity<String> getTest() throws IOException {
        return ResponseEntity.ok("");
    }
}
