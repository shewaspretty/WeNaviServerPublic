package com.hrlee.transnaviserver.springboot.osm.node;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public final class VirtualNode extends NodeWrapper {

    public VirtualNode(long id, Coordinate coordinate, NodeWrapper[] reachableNodes) {
        super(id, coordinate.getLatitude(), coordinate.getLongitude());
        insertIntoWay(reachableNodes);
    }

    private void insertIntoWay(NodeWrapper[] targetNodes) {
        List<WayWrapper> attachedWays = new ArrayList<>();
        for(int i=0; i<targetNodes.length; i++)
            attachedWays.addAll(targetNodes[i].getWays());

        for(int i=0; i<attachedWays.size(); i++) {
            if(!attachedWays.get(i).insertVirtualNodeIfPossible(targetNodes, this))
                continue;
            this.getWays().add(attachedWays.get(i));
            break;
        }
    }
}
