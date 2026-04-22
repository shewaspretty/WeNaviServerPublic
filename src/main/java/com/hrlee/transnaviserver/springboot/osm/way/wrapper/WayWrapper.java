package com.hrlee.transnaviserver.springboot.osm.way.wrapper;

import com.hrlee.transnaviserver.springboot.CorruptAble;
import com.hrlee.transnaviserver.springboot.osm.entity.NodeImpl;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.node.VirtualNode;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@RequiredArgsConstructor
@Getter
public class WayWrapper implements CorruptAble {

    private final long id;
    private final AttachedNodeList nodes = new AttachedNodeList();
    private final HashMap<String, String> wayTags = new HashMap<>();
    private final String highway;

    public WayWrapper(@NotNull NodeImpl rawNode, NodeWrapper node) {
        id = rawNode.getWayId();
        highway = rawNode.getHighway();
        attachNode(rawNode, node);
        addTags(rawNode);
    }

    @Nullable
    public NodeWrapper[] getReachableNodes(NodeWrapper centerNode) {
        int centerNodePtr = getNodePtrInWay(centerNode);
        if(centerNodePtr < 0)
            return null;
        NodeWrapper[] returnAble = new NodeWrapper[2];

        NodeWrapper prevNode = null;
        NodeWrapper nextNode = null;

        if(centerNodePtr -1 >= 0 && (prevNode = nodes.get(centerNodePtr -1)) != null
                && !(wayTags.containsKey("oneway") && wayTags.get("oneway").equals("yes")) && !isRoundabout())
            returnAble[0] = prevNode;

        if(centerNodePtr +1 < nodes.size() && (nextNode = nodes.get(centerNodePtr +1)) != null)
            returnAble[1] = nextNode;
        return returnAble;
    }

    public boolean insertVirtualNodeIfPossible(NodeWrapper[] nodesExistsCondition, VirtualNode insertable) {
        int insertIndex = -1;
        for(int i=0; i<nodesExistsCondition.length; i++) {
            int existsConditionNodePtr = getNodePtrInWay(nodesExistsCondition[i]);
            if(existsConditionNodePtr < 0)
                return false;
            if(insertIndex > -1 && insertIndex > existsConditionNodePtr)
                continue;
            insertIndex = existsConditionNodePtr;
        }
        if(insertIndex < 0)
            return false;

        nodes.add(insertIndex, insertable);
        return true;
    }

    public void attachNode(@NotNull NodeImpl rawNode, NodeWrapper node) {
        attachNode(node, rawNode.getOrderInWay());
    }

    public void attachNode(NodeWrapper node, int orderInWay) {
        if(orderInWay < nodes.size() && nodes.get(orderInWay) != null)
            return;

        nodes.set(orderInWay, node);
    }

    public void addTags(@NotNull NodeImpl rawNode) {
        if(rawNode.getWayTagKey().equals("highway"))
            return;
        addTag(rawNode.getWayTagKey(), rawNode.getWayTagValue());
    }

    public void addTag(String key, String value) {
        if(wayTags.containsKey(key))
            return;
        wayTags.put(key, value);
    }

    public int getNodePtrInWay(NodeWrapper targetNode) {
        NodeWrapper nodesIt = null;
        for(int i=0; i<nodes.size(); i++) {
            nodesIt = nodes.get(i);
            if(nodesIt == null)
                continue;
            if(nodesIt.getId() != targetNode.getId())
                continue;
            return i;
        }
        return -1;
    }

    @Deprecated
    @Override
    public boolean isCorrupt() {
        for(int i=0; i<nodes.size(); i++)
            if(nodes.get(i) == null)
                return true;
        return false;
    }

    @Nullable
    public String getWayName() {
        String returnAble = null;

        if((returnAble = wayTags.get("name")) != null)
            return returnAble;
        if((returnAble = wayTags.get("name:ko")) != null)
            return returnAble;
        return null;
    }

    public boolean isRoundabout() {
        return wayTags.containsKey("junction") && wayTags.get("junction").equals("roundabout");
    }
}
