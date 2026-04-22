package com.hrlee.transnaviserver.springboot.service.route.param.type;

import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOption;
import com.hrlee.transnaviserver.springboot.service.route.param.option.driving.AvoidExpressWay;
import com.hrlee.transnaviserver.springboot.service.route.param.option.driving.PreferConvenientWay;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class DrivingRouteType extends AbstractRouteType {

    public DrivingRouteType(@Nullable int[] optionsOrdinals) {
        super(optionsOrdinals);
    }

    @Override
    public String getHighwayConditionRawQuery() {
        return HIGHWAY_TAG_VALUE + "!=\"cycleway\"" +
                " AND " + HIGHWAY_TAG_VALUE + "!=\"footway\"" +
                " AND " + HIGHWAY_TAG_VALUE + "!=\"path\"" +
                " AND " + HIGHWAY_TAG_VALUE + "!=\"pedestrian\"";
    }

    @Override
    protected RouteOption[] getRouteOptions() {
        return new RouteOption[]{new AvoidExpressWay(), new PreferConvenientWay()};
    }
}
