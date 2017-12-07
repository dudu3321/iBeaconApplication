package com.example.user.ibeaconapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
/**
 * Created by M on 2017/12/7.
 */

public class BackgroundService extends Service{

    public class LocalBinder extends Binder //宣告一個繼承 Binder 的類別 LocalBinder
    {
        BackgroundService getService()
        {
            return  BackgroundService.this;
        }
    }

    private LocalBinder mLocBin = new LocalBinder();

    public void myMethod()
    {
        Log.d( "test", "myMethod()");
    }

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return mLocBin;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        // TODO Auto-generated method stub
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // TODO Auto-generated method stub
    }
}
