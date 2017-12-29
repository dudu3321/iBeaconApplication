package com.example.user.ibeaconapplication;

import java.util.Collection;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.altbeacon.beacon.*;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

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
    BackgroundService backgroundService;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    public static final String TAG = "BeaconsEverywhere";
    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restService = new RestService();
        backgroundService = new BackgroundService();

        //region Buttion Onclick Event
        peripheralTextView = (TextView) findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());

        apiListTextView = (TextView) findViewById(R.id.textViewAPIList);
        apiListTextView.setMovementMethod(new ScrollingMovementMethod());

        startScanningButton = (Button) findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanningButton.setVisibility(View.INVISIBLE);
                stopScanningButton.setVisibility(View.VISIBLE);
                beaconManager.bind(MainActivity.this);
            }
        });

        stopScanningButton = (Button) findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                beaconManager.unbind(MainActivity.this);
                startScanningButton.setVisibility(View.VISIBLE);
                stopScanningButton.setVisibility(View.INVISIBLE);
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        getOneBeaconButton = (Button) findViewById(R.id.buttonOneBeacon);
        getOneBeaconButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GetOneBeacon();
            }
        });

        getAPIListButton = (Button) findViewById(R.id.getAPIListButton);
        getAPIListButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GetBeaconList();
                Log.d("Debug Log", "getAPIListButton");
            }
        });

        startService = (Button) findViewById(R.id.buttonStartService);
        startService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BackgroundService.class);
                startService(intent);
            }
        });

        stopService = (Button) findViewById(R.id.buttonStopService);
        stopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BackgroundService.class);
                stopService(intent);
            }
        });

        serviceTrigger = (Button) findViewById(R.id.buttonServiceTrigger);
        serviceTrigger.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("Debug Log", "buttonServiceTrigger");
                backgroundService.callFlag = true;
            }
        });
        //endregion

        //region Bluetooth Auth


        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));


        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

        //endregion
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        final Region region = new Region("myBeaons", null, null, null);

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "didEnterRegion");
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "didExitRegion");
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                for(final Beacon oneBeacon : beacons) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            peripheralTextView.append( "uuid: " + oneBeacon.getId1() + "/" + oneBeacon.getId2() + "/" + oneBeacon.getId3() + "/n");
                        }
                    });
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    public void StartScanBeacon(){}
    public void StopScanBeacon(){}

    public void GetBeaconList(){
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

    public void GetOneBeacon(){
        iBeacon o = new iBeacon();
        o.Minor = "3";
        o.UUID = "1";
        o.Major = "1";
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
}