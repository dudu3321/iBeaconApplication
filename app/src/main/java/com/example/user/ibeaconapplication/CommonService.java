package com.example.user.ibeaconapplication;

import android.widget.Toast;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by M on 2017/12/7.
 */

public class CommonService {

    RestService restService;

    public void ReadyAPIService(){
        restService = new RestService();
    }

    public void CallAPI(){
        restService.getService().getAllBeacons(new Callback<List<iBeacon>>() {
            @Override
            public void success(List<iBeacon> beacons, Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
                //Toast.makeText(MainActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
