package com.hrlee.transnaviserver.springboot.osm.way.fragment;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class FragmentWayContainer {

    @NonNull
    private final CoordinateTool coordinateTool;
    @NonNull
    private final Coordinate targetCoordinate;
    @NonNull
    private final ArrayList<FragmentWayComparable> fragmentWays = new ArrayList<>();

    private int nearestFragmentWayToTargetCoordinatePtr = -1;

    public static FragmentWayContainer create(@NonNull List<NodeWrapper> targetNodes, @NonNull Coordinate targetComparableCoordinate, CoordinateTool coordinateTool) {
        FragmentWayContainer returnable = new FragmentWayContainer(coordinateTool, targetComparableCoordinate);
        returnable.createFragmentWays(targetNodes, targetComparableCoordinate);
        return returnable;
    }

    public void cutInHalfFragmentWays() {
        cutInHalfFragmentWays(fragmentWays);
    }

    private void cutInHalfFragmentWays(List<FragmentWayComparable> fragmentWays) {
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

    // TODO: PERFORMANCE OPTIMIZATION NEEDED
    private void createFragmentWays(@NotNull List<NodeWrapper> targetNodes, Coordinate targetComparableCoordinate) {
        NodeWrapper nodeIt;
        List<NodeWrapper.ReachableNode> reachableNodes;

        for(int i=0; i<targetNodes.size(); i++) {
            nodeIt = targetNodes.get(i);
            reachableNodes = nodeIt.getReachableNodes();

            int reachableNodesPtr = -1;
            NodeWrapper.ReachableNode reachableNodesIt = null;
            boolean isReachableNodesItDuplicated = false;

            while(++reachableNodesPtr < reachableNodes.size()) {
                reachableNodesIt = reachableNodes.get(reachableNodesPtr);
                isReachableNodesItDuplicated = false;

                for(int j=0; j<fragmentWays.size(); j++) {
                    if(fragmentWays.get(j).equals(nodeIt.generateNewCoordinate(), reachableNodesIt.getNode().generateNewCoordinate())) {
                        isReachableNodesItDuplicated = true;
                        break;
                    }
                }

                if(isReachableNodesItDuplicated)
                    continue;
                fragmentWays.add(new FragmentWayComparable(nodeIt, reachableNodesIt.getNode(), reachableNodesIt.getConnectedWay(), targetComparableCoordinate, coordinateTool));
            }
        }
    }

    @Nullable
    public FragmentWayComparable getNearestFragmentWayToTargetCoordinate() {
        if(nearestFragmentWayToTargetCoordinatePtr < 0)
            adjustNearestFragmentWayToTargetCoordinatePtr(null);

        if(nearestFragmentWayToTargetCoordinatePtr < 0)
            return null;

        return fragmentWays.get(nearestFragmentWayToTargetCoordinatePtr);
    }

    public void adjustNearestFragmentWayToTargetCoordinatePtr(@Nullable FragmentWayComparable newInappropriateFragmentWay) {
        FragmentWayComparable fragmentWaysIt = null;
        int fragmentWaysPtr = 0;
        int adjustedNearestToTargetCoordinatePtr = -1;

        String newInappropriateFragmentWayName;
        if(newInappropriateFragmentWay == null)
            newInappropriateFragmentWayName = null;
        else
            newInappropriateFragmentWayName = newInappropriateFragmentWay.getOriginWay().getWayName();

        while(fragmentWaysPtr < fragmentWays.size()) {
            fragmentWaysIt = fragmentWays.get(fragmentWaysPtr);

            if(fragmentWaysIt == newInappropriateFragmentWay) {
                fragmentWays.remove(fragmentWaysPtr);
                continue;
            }

            if(newInappropriateFragmentWayName != null && newInappropriateFragmentWayName.equals(fragmentWaysIt.getOriginWay().getWayName())) {
                fragmentWays.remove(fragmentWaysPtr);
                continue;
            }

            if(adjustedNearestToTargetCoordinatePtr > -1
                    && fragmentWaysIt.getDistanceComparableMeter() >= fragmentWays.get(adjustedNearestToTargetCoordinatePtr).getDistanceComparableMeter()) {
                fragmentWaysPtr++;
                continue;
            }

            adjustedNearestToTargetCoordinatePtr = fragmentWaysPtr;
            fragmentWaysPtr++;
        }

        nearestFragmentWayToTargetCoordinatePtr = adjustedNearestToTargetCoordinatePtr;
    }
}
