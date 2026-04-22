package com.hrlee.transnaviserver.springboot.service.route.handler;

import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;

public interface WayChangedDetectable {

    public default boolean isWayChanged(WayWrapper prevWay, WayWrapper currentWay) {
        if(currentWay == prevWay)
            return false;

        if(currentWay.getId() == prevWay.getId())
            return false;

        String currentWayName = currentWay.getWayName();
        String comparableWayName = prevWay.getWayName();

        if(currentWayName == null || comparableWayName == null)
            return true;

        return !currentWayName.equals(comparableWayName);
    }
}
