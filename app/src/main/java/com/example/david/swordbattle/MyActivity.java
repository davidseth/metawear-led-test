package com.example.david.swordbattle;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;

import android.util.Log;

import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.MetaWearBleService;

import static com.mbientlab.metawear.MetaWearBoard.ConnectionStateHandler;
import static com.mbientlab.metawear.AsyncOperation.CompletionHandler;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Led;



public class MyActivity extends AppCompatActivity implements ServiceConnection {

    private Button connect;
    private Button connect2;
    private Button led_on;
    private Button led_off;

    private final String MW_MAC_ADDRESS= "E8:79:FE:30:7C:44"; //update with your board's MAC address
    private final String MW2_MAC_ADDRESS= "E3:A6:6C:ED:CE:11"; //update with your board's MAC address

    private static final String TAG = "MetaWear";

    private MetaWearBleService.LocalBinder serviceBinder;
    private Led ledModule; //Declare the ledModule
    private Led ledModule2; //Declare the ledModule
    private MetaWearBoard mwBoard1;
    private MetaWearBoard mwBoard2;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        ///< Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);

        Log.i(TAG, "log test"); //ADD THIS
        connect=(Button)findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked connect1");
                mwBoard1.connect(); //.connect() and .disconnect() are how we control connection state
            }
        });

        connect2=(Button)findViewById(R.id.connect2);
        connect2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Clicked connect2");
                mwBoard2.connect(); //.connect() and .disconnect() are how we control connection state
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ///< Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;
        retrieveBoard(MW_MAC_ADDRESS, mwBoard1);
        //retrieveBoard(MW2_MAC_ADDRESS, mwBoard2);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }

    private final ConnectionStateHandler stateHandler = new ConnectionStateHandler() {
        @Override
        public void connected() {
            Log.i(TAG, "Connected");
            try {
                ledModule = mwBoard1.getModule(Led.class);
            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }

            try {
                ledModule2 = mwBoard2.getModule(Led.class);
            } catch (UnsupportedModuleException e) {
                e.printStackTrace();
            }

            led_on=(Button)findViewById(R.id.led_on);
            led_on.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Turn on LED");
                    setBoardColour(ledModule);
                    //setBoardColour(ledModule2);

                }
            });

            led_off=(Button)findViewById(R.id.led_off);
            led_off.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Turn off LED");
                    ledModule.stop(true);
                    //ledModule2.stop(true);
                }
            });
        }

        @Override
        public void disconnected() {
            Log.i(TAG, "Connected Lost");
        }

        @Override
        public void failure(int status, Throwable error) {
            Log.e(TAG, "Error connecting", error);
        }
    };

    public void setBoardColour(Led ledModule) {
        ledModule.configureColorChannel(Led.ColorChannel.BLUE)
                .setRiseTime((short) 0).setPulseDuration((short) 1000)
                .setRepeatCount((byte) -1).setHighTime((short) 500)
                .setHighIntensity((byte) 16).setLowIntensity((byte) 16)
                .commit();
        ledModule.play(true);
    }

    public void retrieveBoard(String macaddress, MetaWearBoard board) {
        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(macaddress);

        // Create a MetaWear board object for the Bluetooth Device
        mwBoard1 = serviceBinder.getMetaWearBoard(remoteDevice);
        Log.i(TAG, "retrieveBoard");
        mwBoard1.setConnectionStateHandler(stateHandler);
    }

}
