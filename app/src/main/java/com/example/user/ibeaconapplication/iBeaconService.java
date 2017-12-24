package com.example.user.ibeaconapplication;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;

public interface iBeaconService {
    @GET("/iBeacon")
    void GetAllBeacons(Callback<List<iBeacon>> callback);

    @POST("/iBeacon")
    void GetBeaconInfo(@Body iBeacon beacon, Callback<iBeacon> callback);

    @PUT("/iBeacon")
    void SaveNewBeacon(@Body iBeacon beacon, Callback<iBeacon> callback);
}
