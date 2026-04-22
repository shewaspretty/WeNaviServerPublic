package com.hrlee.transnaviserver.springboot.service.route.dijkstra;

import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class PriorityQueue {

    private final ArrayList<NodeWeight> queue = new ArrayList<>();

    private void percolateUp(int targetIndex) {
        int current = targetIndex;
        int parent = -1;

        while(current > 0) {
            if(current == 1)
                parent = 0;
            else
                parent = (current -1) / 2;

            if(queue.get(current).getEtaMsForWeight() > queue.get(parent).getEtaMsForWeight())
                break;

            swap(current, parent);
            current = parent;
        }
    }

    private void swap(int index1, int index2) {
        NodeWeight tmp = queue.get(index1);
        set(index1, queue.get(index2));
        set(index2, tmp);
    }

    private void percolateDown(int targetIndex) {
        int current = targetIndex;
        int[] childrenIndex = null;

        while(true) {
            childrenIndex = new int[]{(2 * current) + 1, (2 * current) + 2};
            if(!(childrenIndex[0] < queue.size() && childrenIndex[1] < queue.size()))
                break;

            int minChildrenIndexPtr = 0;
            if(queue.get(childrenIndex[0]).getEtaMsForWeight() > queue.get(childrenIndex[1]).getEtaMsForWeight())
                minChildrenIndexPtr = 1;

            if(queue.get(childrenIndex[minChildrenIndexPtr]).getEtaMsForWeight() > queue.get(current).getEtaMsForWeight())
                break;

            swap(current, childrenIndex[minChildrenIndexPtr]);
            current = childrenIndex[minChildrenIndexPtr];
        }
    }

    public void insert(NodeWeight insert) {
        queue.add(insert);
        insert.setIndexInPriorityQueue(queue.size() -1);
        percolateUp(queue.size() -1);
    }

    @Nullable
    public NodeWeight pop() {
        if(queue.isEmpty())
            return null;
        NodeWeight returnAble = queue.get(0);

        set(0, queue.get(queue.size() -1));
        queue.remove(queue.size() -1);
        percolateDown(0);

        returnAble.setIndexInPriorityQueue(-1);
        return returnAble;
    }

    private void set(int index, NodeWeight value) {
        queue.set(index, value);
        value.setIndexInPriorityQueue(index);
    }

    public void onValuePriorityUpdated(@NotNull NodeWeight updated) {
        int targetIndex = updated.getIndexInPriorityQueue();
        percolateUp(targetIndex);
        percolateDown(targetIndex);
    }

    public int getSize() {
        return queue.size();
    }
}
