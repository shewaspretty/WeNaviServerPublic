package com.hrlee.transnaviserver.springboot.retrofit.repo;

import com.hrlee.transnaviserver.springboot.retrofit.dto.BusStopData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BusStopDataRepo {
    @GET("/api/15067528/v1/uddi:eb02ec03-6edd-4cb0-88b8-eda22ca55e80")
    public Call<BusStopData> getBusStopData(@Query("serviceKey")String serviceKey,
                                            @Query("page")int page, @Query("perPage")int contentsNumPerPage);
}
