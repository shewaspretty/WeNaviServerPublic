package com.hrlee.transnaviserver.springboot.osm.node.virtual;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.fragment.FragmentWayComparable;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public final class VirtualNode extends NodeWrapper {

    @Getter(value = AccessLevel.PACKAGE)
    private final FragmentWayComparable originFragmentWay;

    VirtualNode(long id, FragmentWayComparable configurable) {
        super(id, configurable.getNearestCoordinateToTarget());
        originFragmentWay = configurable;
        insertIntoWay(configurable);
    }

    void detach() {
        ArrayList<WayWrapper> ways = getWays();
        ways.get(0).removeVirtualNode();
        ways.clear();
    }

    @Deprecated
    private void insertIntoWay(NodeWrapper[] targetNodes) {
        /*List<WayWrapper> attachedWays = new ArrayList<>();
        for(int i=0; i<targetNodes.length; i++)
            attachedWays.addAll(targetNodes[i].getWays());

        for(int i=0; i<attachedWays.size(); i++) {
            if(!attachedWays.get(i).insertVirtualNodeIfPossible(targetNodes, this))
                continue;
            this.getWays().add(attachedWays.get(i));
            break;
        }*/
    }

    private void insertIntoWay(FragmentWayComparable fragmentWayComparable) {
        if(!fragmentWayComparable.getOriginWay().insertVirtualNodeIfPossible(fragmentWayComparable.getOriginNodes(), this))
            return;
        attachWay(fragmentWayComparable.getOriginWay());
    }
}
