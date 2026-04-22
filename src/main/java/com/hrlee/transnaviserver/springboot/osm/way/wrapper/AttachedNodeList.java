package com.hrlee.transnaviserver.springboot.osm.way.wrapper;

import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AttachedNodeList extends ArrayList<NodeWrapper> {

    public AttachedNodeList() {
        super();
    }

    @Override
    public NodeWrapper set(int index, NodeWrapper element) {
        int size = size();
        if(!(index < size)) {
            for(int i=0; i<(index +1) - size; i++)
                add(null);
        }
        return super.set(index, element);
    }
}
