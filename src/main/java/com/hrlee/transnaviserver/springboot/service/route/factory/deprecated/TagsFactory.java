package com.hrlee.transnaviserver.springboot.service.route.factory.deprecated;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.dto.route.Tag;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.osm.node.NodeWrapper;
import com.hrlee.transnaviserver.springboot.osm.way.wrapper.WayWrapper;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Deprecated
public final class TagsFactory implements LoggAble {

    private final List<NodeWrapper> nodes;
    private final CoordinateTool coordinateTool;

    private List<Tag> generatedTags = null;

    private final String descriptionStraightForwardAndArrives = "목적지 도착";

    @AllArgsConstructor
    private static enum DescriptionDirection {
        STRAIGHT_FORWARD("계속 직진", 0, 0),
        U_TURN("U턴", 180, 180),
        BEAR_LEFT("왼쪽 방향 직진", 360 - 30, 360),
        BEAR_RIGHT("오른쪽 방향 직진", 0, 0 + 30),
        LEFT("좌회전", 360 - 110, 360 - 30),
        RIGHT("우회전", 0 + 30, 0+ 110),
        SHARP_LEFT("큰 좌회전", 180, 360 - 110),
        SHARP_RIGHT("큰 우회전", 0 + 110, 180),
        ARRIVE("목적지 도착", 0 ,0);

        private final String printable;
        private final double minStandardAzimuth;
        private final double maxStandardAzimuth;

        @Nullable
        private static DescriptionDirection getByStandardAzimuth(double azimuth) {
            DescriptionDirection[] values = DescriptionDirection.values();
            DescriptionDirection valueIt = null;

            for(int i=0; i<values.length; i++) {
                valueIt = values[i];
                if(!(valueIt.minStandardAzimuth <= azimuth) || !(azimuth <= valueIt.maxStandardAzimuth))
                    continue;
                return valueIt;
            }
            return null;
        }
    }

    @Nullable
    public List<Tag> generate(RouteSerializerDeprecated.CurrentPointHolder currentPointHolder) {
        if(!currentPointHolder.isWayChanged() && generatedTags != null)
            if(!currentPointHolder.isNextInterSectionLastNode())
                return generatedTags;

        List<Tag> returnAble = null;
        if(currentPointHolder.isWayChanged()) {
            returnAble = generateWayTags(currentPointHolder);
            generatedTags = new ArrayList<>(returnAble);
        } else
            returnAble = new ArrayList<>(generatedTags);

        return generateDescriptionTags(currentPointHolder, returnAble);
    }

    @NonNull
    private List<Tag> generateWayTags(RouteSerializerDeprecated.CurrentPointHolder currentPointHolder) {
        WayWrapper targetWay = currentPointHolder.getCurrentWay();

        ArrayList<Tag> returnAble = new ArrayList<>();
        returnAble.add(new Tag("type", targetWay.getHighway()));

        String wayName = targetWay.getWayName();
        if(wayName != null)
            returnAble.add(new Tag("roadName", wayName));
        return returnAble;
    }

    @NonNull
    private List<Tag> generateDescriptionTags(@NonNull RouteSerializerDeprecated.CurrentPointHolder currentPointHolder, List<Tag> returnAble) {
        NodeWrapper currentNode = currentPointHolder.getCurrentNode();
        NodeWrapper nextInterSection = currentPointHolder.getNextInterSection();
        NodeWrapper nextNodeOfNextInterSection = currentPointHolder.getNextNodeOfNextInterSection();

        if(nextInterSection == null) {
            getLogger().error("nextInterSection NULL, description will NOT be included on this");
            return generatedTags;
        }

        DescriptionDirection descriptionDirection = null;
        String description = null;

        if(nextNodeOfNextInterSection == null) {
            if(!currentPointHolder.isNextInterSectionLastNode()) {
                getLogger().error("nextNode of nextInterSection NULL, description will NOT be included on this");
                return generatedTags;
            }
            if(!currentPointHolder.isLast())
                return generatedTags;

            descriptionDirection = DescriptionDirection.ARRIVE;
            description = descriptionDirection.printable;
        } else {
            descriptionDirection = getDescriptionDirectionByAzimuth(currentNode, nextInterSection, nextNodeOfNextInterSection);
            if(descriptionDirection == null) {
                getLogger().error("description direction NULL, description will NOT be included on this");
                return generatedTags;
            }

            double distanceToNextInterSection = coordinateTool.getDistanceMeter(currentNode.generateNewCoordinate(), nextInterSection.generateNewCoordinate());
            description = (int)distanceToNextInterSection + "m " + descriptionDirection.printable;
        }

        returnAble.add(new Tag("description", description));
        returnAble.add(new Tag("descriptionCode", String.valueOf(descriptionDirection.ordinal())));

        return returnAble;
    }

    @Nullable
    private DescriptionDirection getDescriptionDirectionByAzimuth(@NonNull NodeWrapper currentNode, @NonNull NodeWrapper nextInterSection, @NonNull NodeWrapper nextNodeOfNextInterSection) {
        double currentWayAzimuth = coordinateTool.getAzimuth(currentNode.generateNewCoordinate(), nextInterSection.generateNewCoordinate());
        double wayAfterInterSectionAzimuth = coordinateTool.getAzimuth(nextInterSection.generateNewCoordinate(), nextNodeOfNextInterSection.generateNewCoordinate());

        currentWayAzimuth = coordinateTool.convertToStandardAzimuth(currentWayAzimuth);
        wayAfterInterSectionAzimuth = coordinateTool.convertToStandardAzimuth(wayAfterInterSectionAzimuth);

        double nextInterSectionAzimuthByCurrentWayAzimuth = wayAfterInterSectionAzimuth - currentWayAzimuth;
        return DescriptionDirection.getByStandardAzimuth(coordinateTool.convertToStandardAzimuth(nextInterSectionAzimuthByCurrentWayAzimuth));
    }

    @Deprecated
    @Nullable
    private DescriptionDirection getDescriptionDirectionByAzimuth(NodeWrapper currentNode, NodeWrapper nextInterSectionIndex) {
        return null;
        /*
        double currentStandardAzimuth = coordinateTool.getAzimuth(currentNode.getCoordinate(), nodes.get(nextInterSectionIndex).getCoordinate());
        double interSectionStandardAzimuth = coordinateTool.getAzimuth(nodes.get(nextInterSectionIndex).getCoordinate(), nodes.get(nextInterSectionIndex + 1).getCoordinate());

        currentStandardAzimuth = coordinateTool.convertToStandardAzimuth(currentStandardAzimuth);
        interSectionStandardAzimuth = coordinateTool.convertToStandardAzimuth(interSectionStandardAzimuth);

        double azimuth = currentStandardAzimuth - interSectionStandardAzimuth;
        if(azimuth < 0)
            azimuth -= (azimuth * 2);

        DescriptionDirection[] descriptionDirections = DescriptionDirection.values();
        for(int i=0; i<descriptionDirections.length; i++) {
            if (!(descriptionDirections[i].minStandardAzimuth <= azimuth && azimuth <= descriptionDirections[i].maxStandardAzimuth))
                continue;
            return descriptionDirections[i];
        }

        getLogger().error("Could not find suitable direction description by azimuth");
        return null;*/
    }
}
