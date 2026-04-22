package com.hrlee.transnaviserver.springboot.service.route.dijkstra;

public interface PriorityQueueElement {

    public int getIndexInPriorityQueue();
    public boolean setIndexInPriorityQueue(int index);
}
