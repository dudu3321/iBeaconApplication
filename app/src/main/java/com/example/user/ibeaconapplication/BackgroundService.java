package com.example.user.ibeaconapplication;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
/**
 * Created by M on 2017/12/7.
 */

public class BackgroundService extends Service{

    private Handler handler = new Handler();
    public boolean callFlag = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP);

        super.onStart(intent, startId);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(ServiceTask);
        Log.i("Service event","Stop Service");
        super.onDestroy();
    }

    private Runnable ServiceTask = new Runnable(){
        public void run(){
            if(callFlag){
                Log.i("Click event","onClick");
                callFlag = false;
            }
        }
    };
}
