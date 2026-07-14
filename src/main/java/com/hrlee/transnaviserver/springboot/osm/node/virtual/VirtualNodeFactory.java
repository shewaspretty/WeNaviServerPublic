package com.hrlee.transnaviserver.springboot.osm.node.virtual;

import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.fragment.FragmentWayComparable;
import com.hrlee.transnaviserver.springboot.osm.way.fragment.FragmentWayContainer;
import com.hrlee.transnaviserver.springboot.service.route.finder.NearbyNodeFinder;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class VirtualNodeFactory {

    private FragmentWayContainer startFragmentWayContainer = null;
    private FragmentWayContainer destinationFragmentWayContainer = null;

    @RequiredArgsConstructor
    public static enum Type {
        START_VIRTUAL_NODE(-1),
        DESTINATION_VIRTUAL_NODE(-2);

        private final long id;
    }

    @NonNull
    public static VirtualNodeFactory create(List<NodeWrapper> allNodes, CoordinateTool coordinateTool, Coordinate start, Coordinate destination) {
        VirtualNodeFactory virtualNodeFactory = new VirtualNodeFactory();

        NearbyNodeFinder nearbyNodeFinder = new NearbyNodeFinder(allNodes, coordinateTool);
        NearbyNodeFinder.Result nearbyNodes = nearbyNodeFinder.getNearbyNodes(start, destination);

        virtualNodeFactory.startFragmentWayContainer = FragmentWayContainer.create(nearbyNodes.getNodesNearStart(), start, coordinateTool);
        virtualNodeFactory.destinationFragmentWayContainer = FragmentWayContainer.create(nearbyNodes.getNodesNearDestination(), destination, coordinateTool);

        virtualNodeFactory.startFragmentWayContainer.cutInHalfFragmentWays();
        virtualNodeFactory.destinationFragmentWayContainer.cutInHalfFragmentWays();

        return virtualNodeFactory;
    }

    @Nullable
    public VirtualNode createNewVirtualNode(Type type, @Nullable VirtualNode existingVirtualNode) {
        if(existingVirtualNode != null) {
            if(type == Type.START_VIRTUAL_NODE)
                startFragmentWayContainer.adjustNearestFragmentWayToTargetCoordinatePtr(existingVirtualNode.getOriginFragmentWay());
            else
                destinationFragmentWayContainer.adjustNearestFragmentWayToTargetCoordinatePtr(existingVirtualNode.getOriginFragmentWay());
            existingVirtualNode.detach();
        }

        FragmentWayComparable configurable = null;
        if(type == Type.START_VIRTUAL_NODE)
            configurable = startFragmentWayContainer.getNearestFragmentWayToTargetCoordinate();
        else
            configurable = destinationFragmentWayContainer.getNearestFragmentWayToTargetCoordinate();

        if(configurable == null)
            return null;

        return new VirtualNode(type.id, configurable);
    }
}
