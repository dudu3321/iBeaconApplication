package com.example.user.ibeaconapplication;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.ibeaconapplication.models.iBeacon;
import com.example.user.ibeaconapplication.services.BackgroundService.BackgroundService;
import com.example.user.ibeaconapplication.services.WebAPIService.RestService;
import com.example.user.ibeaconapplication.services.iBeaconService.iBeaconService;

import org.altbeacon.beacon.Beacon;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static BluetoothAdapter mBluetoothAdapter = null;
    private final int REQUEST_ENABLE_BT=1;
    Button startScanningButton;
    Button stopScanningButton;
    Button getAPIListButton;
    Button getOneBeaconButton;
    Button startService;
    Button stopService;
    Button serviceTrigger;
    TextView peripheralTextView;
    TextView apiListTextView;
    RestService restService;

    BackgroundService mService;
    Boolean mBound;
    iBeaconService ibeaconService;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restService = new RestService();
        ibeaconService = (iBeaconService)getApplication();

        //region Buttion Onclick Event
        peripheralTextView = findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());

        apiListTextView = findViewById(R.id.textViewAPIList);
        apiListTextView.setMovementMethod(new ScrollingMovementMethod());

        startScanningButton = findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StartScanBeacon();
            }
        });

        stopScanningButton = findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                StopScanBeacon();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        getOneBeaconButton = findViewById(R.id.buttonOneBeacon);
        getOneBeaconButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GetOneBeacon();
            }
        });

        getAPIListButton = findViewById(R.id.getAPIListButton);
        getAPIListButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GetBeaconList();
            }
        });

        startService = findViewById(R.id.buttonStartService);
        startService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopService.setVisibility(View.VISIBLE);
                startService.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(MainActivity.this, BackgroundService.class);
                bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                mBound = true;
            }
        });

        stopService = findViewById(R.id.buttonStopService);
        stopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService.setVisibility(View.VISIBLE);
                stopService.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(MainActivity.this, BackgroundService.class);
                stopService(intent);
                mBound = false;
            }
        });

        serviceTrigger = findViewById(R.id.buttonServiceTrigger);
        serviceTrigger.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mBound){
                   mService.SetTrigger();
                }
            }
        });
        //endregion

        // region bluetooth auth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // 如果裝置不支援藍芽
            Toast.makeText(this, "Device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
            return;

        }
        // 如果藍芽沒有開啟
        if (!mBluetoothAdapter.isEnabled()) {
            // 發出一個intent去開啟藍芽，
            Intent mIntentOpenBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(mIntentOpenBT, REQUEST_ENABLE_BT);
        }
        //endregion
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    protected void StartScanBeacon(){
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
        ibeaconService.StartScan();
        timer = new Timer(true);
        timer.schedule(new MyTimerTask(), 1000, 1000);

    }

    protected void StopScanBeacon(){
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        ibeaconService.StopScan();
        timer.cancel();
    }

    protected void GetBeaconList(){
        restService.getService().GetAllBeacons(new Callback<List<iBeacon>>() {
            @Override
            public void success(List<iBeacon> beacons, Response response) {
                String str = "";
                for(iBeacon e : beacons)
                    str += "Major : " + e.Major + ", APIUrl : " + e.APIUrl + "\n";
                    apiListTextView.setText(str);
            }

            @Override
            public void failure(RetrofitError error) {
                //Toast.makeText(MainActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void GetOneBeacon(){
        iBeacon o = new iBeacon("1", "1", "3");
        restService.getService().GetBeaconInfo( o, new Callback<iBeacon>() {
            @Override
            public void success(iBeacon beacon, Response response) {
                apiListTextView.setText("Major : " + beacon.Major + ", APIUrl : " + beacon.APIUrl + "\n");
            }

            @Override
            public void failure(RetrofitError error) {
                //Toast.makeText(MainActivity.this, error.getMessage().toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BackgroundService.bgServiceBinder binder = (BackgroundService.bgServiceBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public class MyTimerTask extends TimerTask
    {
        public void run()
        {
            Beacon beaconNow = ibeaconService.GetBeaconNearbyNow();
            if(beaconNow!= null){
                String uuid = beaconNow.getId1().toString();
                String major = beaconNow.getId2().toString();
                String minor = beaconNow.getId3().toString();
            }
        }
    };
}