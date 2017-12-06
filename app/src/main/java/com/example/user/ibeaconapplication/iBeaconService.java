package com.example.user.ibeaconapplication;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface iBeaconService {
    @GET("/iBeacon")
    void getAllBeacons(Callback<List<iBeacon>> callback);

    @GET("/iBeacon?UUID={UUID}&Major={Major}&Minor={Minor}")
    void getBeaconInfo(@Query("UUID") String UUID,@Query("Major") String Major,@Query("Minor") String Minor, Callback<iBeacon> callback);


}
