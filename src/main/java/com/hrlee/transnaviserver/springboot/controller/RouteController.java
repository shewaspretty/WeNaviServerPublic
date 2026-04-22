package com.hrlee.transnaviserver.springboot.controller;

import com.hrlee.transnaviserver.springboot.dto.route.Route;
import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.service.route.RouteService;
import com.hrlee.transnaviserver.springboot.service.route.param.type.AbstractRouteType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    @GetMapping("/route/{type}")
    public ResponseEntity<Route> getRoute(@RequestParam("startLatitude")double startLatitude,
                                          @RequestParam("startLongitude")double startLongitude,
                                          @RequestParam("endLatitude")double endLatitude,
                                          @RequestParam("endLongitude")double endLongitude,
                                          @RequestParam(name = "options", required = false) int[] options,
                                          @PathVariable("type") String type) {

        AbstractRouteType routeType = RouteService.Type.generateRouteType(type, options);
        if(routeType == null)
            return ResponseEntity.notFound().build();

        Route route = routeService.getRoute(new Coordinate(startLatitude, startLongitude), new Coordinate(endLatitude, endLongitude), routeType);
        if(route == null)
            return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(route);
    }
}
