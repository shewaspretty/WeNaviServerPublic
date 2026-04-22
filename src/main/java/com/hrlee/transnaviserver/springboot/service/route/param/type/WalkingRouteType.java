package com.hrlee.transnaviserver.springboot.service.route.param.type;

import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOption;
import com.hrlee.transnaviserver.springboot.service.route.param.option.walking.AvoidCycleWay;
import com.hrlee.transnaviserver.springboot.service.route.param.option.walking.AvoidPedestrianRoad;
import com.hrlee.transnaviserver.springboot.service.route.param.option.walking.AvoidStairs;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

public class WalkingRouteType extends AbstractRouteType {

    public WalkingRouteType(@Nullable int[] optionsOrdinals) {
        super(optionsOrdinals);
    }

    @Override
    public String getHighwayConditionRawQuery() {
        return HIGHWAY_TAG_VALUE + "!=\"motorway\" " +
                " AND " + HIGHWAY_TAG_VALUE + "!=\"trunk\"";
    }

    @Override
    protected RouteOption[] getRouteOptions() {
        return new RouteOption[]{new AvoidPedestrianRoad(), new AvoidStairs(), new AvoidCycleWay()};
    }
}
