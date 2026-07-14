package com.hrlee.transnaviserver.springboot.osm.way.fragment;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public final class FragmentWayHolderDeprecated {
    private final List<FragmentWayComparable> fragmentWays;
    private ArrayList<Integer> indexOrderAscByNearestTarget = null;

    private int currentIndexOrderHeadPtr = -1;

    private FragmentWayHolderDeprecated(CoordinateTool coordinateTool, List<NodeWrapper> nodes, List<Integer> nodeIndexOrder, Coordinate target) {
        fragmentWays = null; //new FragmentWayFactory(nodes, nodeIndexOrder, coordinateTool).getComparableList(target);
    }

    public static FragmentWayHolderDeprecated generateFragmentWays(CoordinateTool coordinateTool, List<NodeWrapper> nodes, List<Integer> nodeIndexOrder, Coordinate target) {
        return new FragmentWayHolderDeprecated(coordinateTool, nodes, nodeIndexOrder, target);
    }

    @Nullable
    public Coordinate getNearestCoordinateToTarget() {
        if(indexOrderAscByNearestTarget == null) {
            cutInHalfFragmentWays();
            createIndexOrderByNearestTarget();
        }

        if(indexOrderAscByNearestTarget == null || indexOrderAscByNearestTarget.isEmpty())
            return null;
        if(++currentIndexOrderHeadPtr >= indexOrderAscByNearestTarget.size())
            return null;

        return fragmentWays.get(indexOrderAscByNearestTarget.get(currentIndexOrderHeadPtr)).getNearestCoordinateToTarget();
    }

    private void cutInHalfFragmentWays() {
        FragmentWayComparable fragmentWaysIt = null;
        while(true) {
            int fragmentWaysPtr = -1;
            int cutInHalfDisabledCnt = 0;

            while(++fragmentWaysPtr < fragmentWays.size()) {
                fragmentWaysIt = fragmentWays.get(fragmentWaysPtr);
                if(fragmentWaysIt.isCutInHalfDisabled()) {
                    cutInHalfDisabledCnt++;
                    continue;
                }
                fragmentWaysIt.cutInHalfByDistanceFromTarget();
            }
            if(cutInHalfDisabledCnt == fragmentWays.size())
                break;
        }
    }

    private void createIndexOrderByNearestTarget() {
        indexOrderAscByNearestTarget = new ArrayList<>(fragmentWays.size());
        FragmentWayComparable indexOrderIt = null;

        for(int i=0; i<fragmentWays.size(); i++) {
            int indexOrderPtr = -1;
            while(++indexOrderPtr < indexOrderAscByNearestTarget.size()) {
                indexOrderIt = fragmentWays.get(indexOrderAscByNearestTarget.get(indexOrderPtr));
                if(fragmentWays.get(i).getDistanceComparableMeter() > indexOrderIt.getDistanceComparableMeter())
                    continue;
            }

            if(indexOrderPtr == indexOrderAscByNearestTarget.size()) {
                indexOrderAscByNearestTarget.add(i);
                continue;
            }
            indexOrderAscByNearestTarget.add(indexOrderPtr, i);
        }
    }
}
