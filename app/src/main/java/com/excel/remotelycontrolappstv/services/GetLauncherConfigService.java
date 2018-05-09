package com.excel.remotelycontrolappstv.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.excel.configuration.LauncherConfigManager;
import com.excel.excelclasslibrary.RetryCounter;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilURL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Sohail on 02-11-2016.
 */

public class GetLauncherConfigService extends Service {

    Context context;
    final static String TAG = "GetLauncherConfig";
    RetryCounter retryCounter;

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.d( TAG, "GetLauncherConfigService started" );
        context = this;
        retryCounter = new RetryCounter( "launcher_config_retry_count" );

        /*GetLauncherConfig glc = new GetLauncherConfig();
        glc.execute( "" );*/
        getLauncherConfig();

        return START_STICKY;
    }

    private void getLauncherConfig(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String s = UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                        UtilURL.getURLParamsFromPairs( new String[][]{
                                { "what_do_you_want", "get_launcher_config" },
                                { "mac_address", UtilNetwork.getMacAddress( context ) }
                        } ));

                if( s == null ){
                    Log.d( TAG, "Failed to retrieve Launcher Config" );
                    setRetryTimer();
                    return;
                }

                Log.d( TAG, "response : "+s );
                JSONArray jsonArray = null;
                JSONObject jsonObject = null;

                try{
                    jsonArray = new JSONArray( s );
                    jsonObject = jsonArray.getJSONObject( 0 );

                    String type = jsonObject.getString( "type" );
                    if ( type.equals( "error" ) ){
                        Log.e( TAG, "Failed to retrieve Launcher Config" );
                        setRetryTimer();
                        return;
                    }
                    else if( type.equals( "success" ) ){
                        String launcher_config_json = jsonObject.getString( "info" );
                        LauncherConfigManager launcherConfigManager = new LauncherConfigManager();
                        launcherConfigManager.writeLauncherConfigFile( launcher_config_json );

                        // Store this data into the database on /mnt/sdcard
                        storeData();

                        // Send broadcast to the Launcher to restart itself
                        context.sendBroadcast( new Intent( "receive_update_launcher_config" ) );
                        //LocalBroadcastManager.getInstance( context ).sendBroadcast( new Intent( "update_launcher_config" ) );



                        Log.i( TAG, launcher_config_json );

                        retryCounter.reset();
                    }
                }
                catch( Exception e ){
                    e.printStackTrace();
                    setRetryTimer();
                }

            }
        }).start();


    }

    public void storeData(){



    }

    private void setRetryTimer(){
        final long time = retryCounter.getRetryTime();

        Log.d( TAG, "time : " + time );

        new Handler( Looper.getMainLooper() ).postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.d( TAG, "downloading launcher config after "+(time/1000)+" seconds !" );
                /*GetLauncherConfig glc = new GetLauncherConfig();
                glc.execute( "" );*/
                getLauncherConfig();

            }

        }, time );

    }


}
