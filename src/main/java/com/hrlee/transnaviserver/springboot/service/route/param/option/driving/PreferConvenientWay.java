package com.hrlee.transnaviserver.springboot.service.route.param.option.driving;

import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOption;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PreferConvenientWay implements RouteOption {

    @Override
    public String getHighwayConditionQuery() {
        return "";
    }

    @SuppressWarnings("PointlessArithmeticExpression")
    @Override
    public long getAdditionalEtaMsByTurn() {
        return 1 * 90 * 1000;
    }

    @Override
    public String getWayTagConditionQuery() {
        return "";
    }

    @Override
    public boolean isWayTagConditionPassed(Map<String, String> wayTags) { return true; }


}
