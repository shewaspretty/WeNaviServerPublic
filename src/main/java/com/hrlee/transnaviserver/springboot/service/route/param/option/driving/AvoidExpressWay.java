package com.hrlee.transnaviserver.springboot.service.route.param.option.driving;

import com.hrlee.transnaviserver.springboot.service.route.param.HighwayTagValueAdjustable;
import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOption;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AvoidExpressWay implements RouteOption, HighwayTagValueAdjustable {

    @Override
    public String getHighwayConditionQuery() {
        return " AND " + HIGHWAY_TAG_VALUE + "!=\"motorway\"";
    }

    @Override
    public long getAdditionalEtaMsByTurn() {
        return 0;
    }

    @Override
    public String getWayTagConditionQuery() {
        return "";
    }

    @Override
    public boolean isWayTagConditionPassed(Map<String, String> wayTags) { return true; }

}
