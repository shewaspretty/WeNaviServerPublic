package com.hrlee.transnaviserver.springboot.service.route.param.option.walking;

import com.hrlee.transnaviserver.springboot.service.route.param.HighwayTagValueAdjustable;
import com.hrlee.transnaviserver.springboot.service.route.param.option.RouteOption;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class AvoidCycleWay implements RouteOption, HighwayTagValueAdjustable {

    private static final String WAY_TAG_KEY_QUERY = "w_t.tag_key";
    private static final String WAY_TAG_VALUE_QUERY = "w_t.tag_value";

    @Override
    public String getHighwayConditionQuery() {
        return "";
    }

    @Override
    public long getAdditionalEtaMsByTurn() {
        return 0;
    }

    @Override
    public String getWayTagConditionQuery() {
        return "AND (" + HIGHWAY_TAG_VALUE + "!=\"cycleway\"" + " OR " + "(" + WAY_TAG_KEY_QUERY + "=\"foot\"" + " AND " + WAY_TAG_VALUE_QUERY + "=\"designated\"" + ") )";
    }

    @Override
    public boolean isWayTagConditionPassed(Map<String, String> wayTags) {
        if(!wayTags.containsKey("highway"))
            return false;

        if(wayTags.get("highway").equals("cycleway")) {
            if(wayTags.containsKey("foot") && wayTags.get("foot").equals("designated"))
                return true;
            return false;
        }
        return true;
    }
}
