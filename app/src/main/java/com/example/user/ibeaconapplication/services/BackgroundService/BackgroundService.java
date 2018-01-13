package com.example.user.ibeaconapplication.services.BackgroundService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.user.ibeaconapplication.models.iBeacon;
import com.example.user.ibeaconapplication.services.WebAPIService.RestService;
import com.example.user.ibeaconapplication.services.iBeaconService.iBeaconService;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.alt236.bluetoothlelib.device.beacon.ibeacon.IBeaconDevice;

/**
 * Created by M on 2017/12/7.
 */

public class BackgroundService extends Service {

    private final IBinder mBinder = new bgServiceBinder();
    private boolean callFlag = false;
    private String userName = "";
    Timer timer;
    iBeaconService ibeaconService;
    IBeaconDevice beaconNow;
    RestService restService;

    public class bgServiceBinder extends Binder {
        public BackgroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BackgroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        timer = new Timer(true);
        timer.schedule(new MyTimerTask(), 1000, 1000);
        ibeaconService = (iBeaconService)getApplication();
        ibeaconService.StartScan();
        restService = new RestService();
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(timer != null) timer.cancel();
        ibeaconService.StopScan();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer != null) timer.cancel();
    }

    public class MyTimerTask extends TimerTask
    {
        public void run()
        {
            if(callFlag){
                beaconNow = ibeaconService.GetBeaconNearbyNow();
                if(beaconNow != null) TriggerEvent();
            }
        }
    };

    public void SetTrigger() {
        callFlag = true;
    }

    public void TriggerEvent(){
        String uuid = beaconNow.getUUID();
        String major = Integer.toString(beaconNow.getMajor());
        String minor = Integer.toString(beaconNow.getMinor()) ;
        GetTriggerediBeacon(uuid, major, minor);

        callFlag = false;
    }

    private void GetTriggerediBeacon(String uuid, String major, String minor)
    {
        restService.getService().GetBeaconInfo(new iBeacon(uuid, major, minor), new Callback<iBeacon>() {
            @Override
            public void success(iBeacon beacon, Response response) {
                if(beacon != null){
                    UpdateTriggerEvent(beacon);
                }
            }

            @Override
            public void  failure(RetrofitError error){

            }
        });
    }

    private void UpdateTriggerEvent(final iBeacon beacon)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(BackgroundService.this);
        String url = beacon.APIUrl;
        Map map = new HashMap();
        map.put("id",beacon.SerialNo);
        map.put("userName", userName);
        JSONObject jsonBody = new JSONObject(map);

        JsonObjectRequest mJsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, jsonBody.toString(),
            new com.android.volley.Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    SetNotification(beacon.SerialNo);
                }
            },
            new com.android.volley.Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }
        );

        requestQueue.add(mJsonObjectRequest);
        requestQueue.start();
    }

    public void SetNotification(long id){
         NotificationManager manager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(BackgroundService.this)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

        Notification notification = mBuilder.build();
        manager.notify((int)id, notification);
    }

    public void SetUserName(String inputVal){
        if(inputVal != null && inputVal.trim().length() > 0)
            userName = inputVal;
        else
            userName = "Kervin";
    }
}
