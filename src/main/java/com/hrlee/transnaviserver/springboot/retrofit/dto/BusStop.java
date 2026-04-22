package com.hrlee.transnaviserver.springboot.retrofit.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class BusStop {

    @SerializedName("경도")
    private double longitude;

    @SerializedName("위도")
    private double latitude;

    @SerializedName("관리도시명")
    private String cityName;

    @SerializedName("도시코드")
    private int cityCode;

    @SerializedName("정류장명")
    private String stationName;

    @SerializedName("정류장번호")
    private String stationCode;
}
