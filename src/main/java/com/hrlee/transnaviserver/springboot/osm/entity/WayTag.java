package com.hrlee.transnaviserver.springboot.osm.entity;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Deprecated
public class WayTag {

    @NonNull
    private final String wayTagKey;
    @NonNull
    private final String wayTagValue;
}
