package com.hrlee.transnaviserver.springboot.service.route.param.type;

import com.hrlee.transnaviserver.springboot.service.route.param.HighwayTagValueAdjustable;
import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOption;
import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOptionHolder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;

public abstract class AbstractRouteType implements HighwayTagValueAdjustable, RouteOption {

    @Nonnull
    private final RouteOptionHolder routeOptionHolder;

    protected AbstractRouteType(@Nullable int[] activatedOptionsOrdinals) {
        routeOptionHolder = new RouteOptionHolder(getRouteOptions(), activatedOptionsOrdinals);
    }

    protected abstract String getHighwayConditionRawQuery();

    protected abstract RouteOption[] getRouteOptions();

    @Override
    public String getHighwayConditionQuery() {
        return getHighwayConditionRawQuery() +
                " AND " + HIGHWAY_TAG_VALUE + "!=\"construction\"" + routeOptionHolder.getHighwayConditionQuery();
    }

    @Override
    public long getAdditionalEtaMsByTurn() {
        return routeOptionHolder.getAdditionalEtaMsByTurn();
    }

    @Deprecated
    @Override
    public String getWayTagConditionQuery() {
        return routeOptionHolder.getWayTagConditionQuery();
    }

    @Override
    public boolean isWayTagConditionPassed(Map<String, String> wayTags) { return routeOptionHolder.isWayTagConditionPassed(wayTags); }
}
