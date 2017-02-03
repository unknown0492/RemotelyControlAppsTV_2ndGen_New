package com.excel.remotelycontrolappstv.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.RetryCounter;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilURL;

public class UpdateBoxBootStatusService extends Service{

    /**
     * AsyncTask was not working in this, so had to use Traditional Threads
     *
     */

    final static String TAG = "UpdateBoxBootStatus";
    Context context;
    ConfigurationReader configurationReader;
    RetryCounter retryCounter;

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.d( TAG, "UpdateBoxBootStatusService started" );
        context = this;
        configurationReader = ConfigurationReader.getInstance();
        retryCounter = new RetryCounter( "boot_status_retry_count" );

        update();

        return START_STICKY;
    }

    private void update(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                        UtilURL.getURLParamsFromPairs( new String[][]{
                                { "mac_address", UtilNetwork.getMacAddress( context ) },
                                { "what_do_you_want", "update_box_bootup_time" },
                                { "ip", UtilNetwork.getLocalIpAddressIPv4( context ) },
                                { "firmware_version", configurationReader.getFirmwareName() }
                        } ));

                if( result == null ){
                    Log.d( TAG, "retryTimer Set for Box BootUp Time" );
                    setRetryTimer();
                    return;
                }
                retryCounter.reset();

                Log.d( TAG, "response : "+result );
            }
        }).start();
    }

    private void setRetryTimer(){
        final long time = retryCounter.getRetryTime();

        Log.d( TAG, "time : " + time );

        new Handler( Looper.getMainLooper() ).postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.d( TAG, "updating BootUp Time after 10 seconds !" );

                update();

            }

        }, time );

    }

}
