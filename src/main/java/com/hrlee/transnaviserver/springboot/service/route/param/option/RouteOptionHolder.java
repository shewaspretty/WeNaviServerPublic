package com.hrlee.transnaviserver.springboot.service.route.param.option;

import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteOptionHolder implements RouteOption {

    @Nullable
    private final List<RouteOption> activatedOptions;

    public RouteOptionHolder(RouteOption[] routeOptions, @Nullable int[] activatedOrdinals) {
        if(activatedOrdinals == null) {
            activatedOptions = null;
            return;
        }

        activatedOptions = new ArrayList<>();
        int activatedOrdinalsIt = -1;

        for(int i=0; i<activatedOrdinals.length; i++) {
            activatedOrdinalsIt = activatedOrdinals[i];
            if (activatedOrdinalsIt < 0 || activatedOrdinalsIt >= routeOptions.length)
                continue;

            activatedOptions.add(routeOptions[activatedOrdinalsIt]);
        }
    }

    @Override
    public String getHighwayConditionQuery() {
        if(activatedOptions == null)
            return "";

        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<activatedOptions.size(); i++)
            stringBuilder.append(activatedOptions.get(i).getHighwayConditionQuery());
        return stringBuilder.toString();
    }

    @Override
    public long getAdditionalEtaMsByTurn() {
        if(activatedOptions == null)
            return 0;

        long returnAble = 0;
        for(int i=0; i<activatedOptions.size(); i++)
            returnAble += activatedOptions.get(i).getAdditionalEtaMsByTurn();
        return returnAble;
    }

    @Deprecated
    @Override
    public String getWayTagConditionQuery() {
        if(activatedOptions == null)
            return "";

        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<activatedOptions.size(); i++)
            stringBuilder.append(activatedOptions.get(i).getWayTagConditionQuery());
        return stringBuilder.toString();
    }

    @Override
    public boolean isWayTagConditionPassed(Map<String, String> wayTags) {
        if(activatedOptions == null)
            return true;

        for(int i=0; i<activatedOptions.size(); i++) {
            if(activatedOptions.get(i).isWayTagConditionPassed(wayTags))
                continue;
            return false;
        }
        return true;
    }
}
