package com.hrlee.transnaviserver.springboot.osm.entity;

import com.hrlee.transnaviserver.springboot.osm.node.Node;
import lombok.*;

@Getter
@Setter
@Deprecated
public final class NodeImpl extends WayTag implements Node {

    private final long id;
    private final double latitude;
    private final double longitude;
    private final long wayId;
    private final int orderInWay;
    @NonNull
    private final String highway;

    public NodeImpl(long id, double latitude, double longitude, long wayId, int orderInWay, @NonNull String highway, @NonNull String wayTagKey, @NonNull String wayTagValue) {
        super(wayTagKey, wayTagValue);

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.wayId = wayId;
        this.orderInWay = orderInWay;
        this.highway = highway;
    }
}
