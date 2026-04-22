package com.hrlee.transnaviserver.springboot.retrofit.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class BusStopData {

    @SerializedName("currentCount")
    private int currentCnt;

    @SerializedName("data")
    private List<BusStop> data;
}
