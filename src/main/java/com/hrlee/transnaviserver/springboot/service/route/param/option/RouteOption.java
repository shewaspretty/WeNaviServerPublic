package com.hrlee.transnaviserver.springboot.service.route.param.option;

import jakarta.annotation.Nullable;

import java.util.Map;

public interface RouteOption {

    public String getHighwayConditionQuery();

    public long getAdditionalEtaMsByTurn();

    @Deprecated
    public String getWayTagConditionQuery();

    public boolean isWayTagConditionPassed(Map<String, String> wayTags);
}
