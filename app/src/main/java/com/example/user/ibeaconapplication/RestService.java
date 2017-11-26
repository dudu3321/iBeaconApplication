package com.example.user.ibeaconapplication;

/**
 * Created by user on 2017/11/27.
 */

public class RestService {
    private static final String URL = "http://localhost:8086/api/";
    private retrofit.RestAdapter restAdapter;
    private iBeaconService apiService;

    public RestService()
    {
        restAdapter = new retrofit.RestAdapter.Builder()
                .setEndpoint(URL)
                .setLogLevel(retrofit.RestAdapter.LogLevel.FULL)
                .build();

        apiService = restAdapter.create(iBeaconService.class);
    }

    public iBeaconService getService()
    {
        return apiService;
    }
}
