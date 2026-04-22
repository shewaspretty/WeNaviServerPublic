package com.hrlee.transnaviserver.springboot.osm;

import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
@Deprecated(forRemoval = true)
public final class OsmDataHolder {

    private final List<NodeWrapper> nodes;
    private final List<WayWrapper> ways;

    public NodeWrapper getNodeForDebugging(long nodeId) {
        for(int i=0; i<nodes.size(); i++) {
            if(nodes.get(i).getId() != nodeId)
                continue;
            return nodes.get(i);
        }
        return null;
    }
}
