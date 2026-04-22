package com.hrlee.transnaviserver.springboot.entity.jpa.busstop;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Deprecated
@Entity(name = "bus_stop")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class BusStop {

    @Id
    @Column(name = "station_code")
    private String stationCode;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "city_code", nullable = false)
    private int cityCode;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "city_name", nullable = false)
    private String cityName;
}
