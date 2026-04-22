package com.hrlee.transnaviserver.springboot.service.route.param.option.walking;

import com.hrlee.transnaviserver.springboot.service.route.param.HighwayTagValueAdjustable;
import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOption;

import java.util.Map;

public class AvoidStairs implements RouteOption, HighwayTagValueAdjustable {

    @Override
    public String getHighwayConditionQuery() {
        return " AND " + HIGHWAY_TAG_VALUE + "!=\"stairs\"";
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
