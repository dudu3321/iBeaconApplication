package com.example.user.ibeaconapplication;


import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface iBeaconService {
    @GET("/iBeacon/Info?UUID={UUID}&Major={Major}&Minor={Minor}")
     void getiBeaconInfo(@Query("UUID") Integer UUID,@Query("Major") Integer Major,@Query("Minor") Integer Minor, Callback<iBeaconInfo> callback);


}
