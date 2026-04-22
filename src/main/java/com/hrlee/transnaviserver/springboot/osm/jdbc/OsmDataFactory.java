package com.hrlee.transnaviserver.springboot.osm.jdbc;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.osm.entity.NodeImpl;
import com.hrlee.transnaviserver.springboot.osm.OsmDataHolder;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import jakarta.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Designed for use only once by instantiated
 */
@NoArgsConstructor
public final class OsmDataFactory implements LoggAble, ResultSetExtractor<List<NodeWrapper>> {

    private final ArrayList<NodeWrapper> nodes = new ArrayList<>();
    private final HashMap<Long, WayWrapper> ways = new HashMap<>();

    @NonNull
    @Override
    public List<NodeWrapper> extractData(@NotNull ResultSet rs) throws SQLException, DataAccessException {
        long currentNodeId = -1;

        long id = -1;
        double latitude = -1.0d;
        double longitude = -1.0d;

        long wayId = -1;
        int orderInWay = -1;
        String wayTagKey = null;
        String wayTagValue = null;

        NodeWrapper currentNodeWrapper = null;
        WayWrapper currentWayWrapper = null;

        while(rs.next()) {
            id = rs.getLong("id");
            if(currentNodeId < 0 || currentNodeId != id) {
                latitude = rs.getDouble("latitude");
                longitude = rs.getDouble("longitude");

                nodes.add((currentNodeWrapper = new NodeWrapper(id, latitude, longitude)));
                currentNodeId = id;
            }

            wayId = rs.getLong("way_id");
            if(ways.containsKey(wayId))
                currentWayWrapper = ways.get(wayId);
            else {
                String highway = rs.getString("highway");
                ways.put(wayId, (currentWayWrapper = new WayWrapper(wayId, highway)));
            }

            orderInWay = rs.getInt("order_in_way");
            currentNodeWrapper.attachWay(currentWayWrapper);
            currentWayWrapper.attachNode(currentNodeWrapper, orderInWay);

            if((wayTagKey = rs.getString("way_tag_key")).equals("highway"))
                continue;
            wayTagValue = rs.getString("way_tag_value");
            currentWayWrapper.addTag(wayTagKey, wayTagValue);
        }

        return nodes;
    }


    @Deprecated(forRemoval = true)
    @Nullable
    private OsmDataHolder transferToWrapper(@NonNull List<NodeImpl> nodes) {
        if(nodes.isEmpty())
            return null;

        List<NodeWrapper> nodeWrappers = new ArrayList<>();
        List<WayWrapper> wayWrappers = new ArrayList<>();

        long currentNodeId = -1;
        NodeImpl nodeIt = null;
        for(int i=0; i<nodes.size(); i++) {
            nodeIt = nodes.get(i);
            if(currentNodeId != nodeIt.getId()) {
                NodeWrapper generatedNodeWrapper = new NodeWrapper(nodeIt);
                nodeWrappers.add(generatedNodeWrapper);

                generateAndAttachWay(wayWrappers, generatedNodeWrapper, nodeIt);
                currentNodeId = nodeIt.getId();
                continue;
            }
            generateAndAttachWay(wayWrappers, nodeWrappers.get(nodeWrappers.size() -1), nodeIt);
        }
        return new OsmDataHolder(nodeWrappers, wayWrappers);
    }

    private void generateAndAttachWay(List<WayWrapper> wayWrappers, NodeWrapper targetNodeWrapper, NodeImpl targetRawNode) {
        WayWrapper wayAttachable = findWay(wayWrappers, targetRawNode.getWayId());
        if(wayAttachable == null) {
            wayAttachable = new WayWrapper(targetRawNode, targetNodeWrapper);
            wayWrappers.add(wayAttachable);
        }

        targetNodeWrapper.attachWay(wayAttachable);
        wayAttachable.attachNode(targetRawNode, targetNodeWrapper);
        wayAttachable.addTags(targetRawNode);
    }

    @Nullable
    private WayWrapper findWay(List<WayWrapper> ways, long wayId) {
        for(int i=0; i<ways.size(); i++) {
            if(ways.get(i).getId() != wayId)
                continue;
            return ways.get(i);
        }
        return null;
    }

    @Nullable
    public NodeWrapper getNodeForDebugging(long nodeId) {
        for(int i=0; i<nodes.size(); i++) {
            if(nodes.get(i).getId() != nodeId)
                continue;
            return nodes.get(i);
        }
        return null;
    }
}
