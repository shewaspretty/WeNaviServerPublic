package com.hrlee.transnaviserver.springboot.dto.route;

import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

@Getter
public class RouteNode {

    private final double latitude;
    private final double longitude;

    private final long etaMs;
    private final HashMap<String, String> tags;

    public RouteNode(NodeWrapper nodeWrapper, long etaMs, HashMap<String, String> tags) {
        latitude = nodeWrapper.getLatitude();
        longitude = nodeWrapper.getLongitude();
        this.etaMs = etaMs;
        this.tags = tags;
    }
}
