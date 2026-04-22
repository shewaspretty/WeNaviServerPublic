package com.hrlee.transnaviserver.springboot.osm.way.fragment;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class FragmentWayFactory {

    private final List<NodeWrapper> nodes;
    private final List<Integer> indexOrderByDistance;

    private final CoordinateTool coordinateTool;

    // TODO: PERFORMANCE OPTIMIZATION NEEDED
    public List<FragmentWayComparable> getComparableList(Coordinate targetComparableCoordinate) {
        List<FragmentWayComparable> returnAble = new ArrayList<>();

        NodeWrapper nodeIt;
        for(int i=0; i<indexOrderByDistance.size(); i++) {
            nodeIt = nodes.get(indexOrderByDistance.get(i));
            List<NodeWrapper.ReachableNode> reachableNodes = nodeIt.getReachableNodes();

            int reachableNodesPtr = -1;
            NodeWrapper reachableNodesIt = null;

            while(++reachableNodesPtr < reachableNodes.size()) {
                reachableNodesIt = reachableNodes.get(reachableNodesPtr).getNode();
                boolean isReachableNodesItDuplicated = false;

                for(int j=0; j<returnAble.size(); j++) {
                    if(returnAble.get(j).equals(nodeIt.generateNewCoordinate(), reachableNodesIt.generateNewCoordinate())) {
                        isReachableNodesItDuplicated = true;
                        break;
                    }
                }

                if(isReachableNodesItDuplicated)
                    continue;
                returnAble.add(new FragmentWayComparable(nodeIt, reachableNodesIt, targetComparableCoordinate, coordinateTool));
            }
        }
        return returnAble;
    }
}
