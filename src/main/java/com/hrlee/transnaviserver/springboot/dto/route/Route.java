package com.hrlee.transnaviserver.springboot.dto.route;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hrlee.transnaviserver.springboot.service.route.serialize.ListReverseJsonSerializer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class Route {

    @NonNull
    @JsonSerialize(using = ListReverseJsonSerializer.class)
    private final List<RouteNode> nodes;
    private final long etaMs;

    @JsonSerialize(using = ListReverseJsonSerializer.class)
    private final List<String> summary;
    private final String totalDistance;

    public long getDuringTimeMs() { return etaMs - nodes.get(nodes.size() -1).getEtaMs(); }
}
