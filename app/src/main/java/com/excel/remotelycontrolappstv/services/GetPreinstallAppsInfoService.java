package com.excel.remotelycontrolappstv.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.excel.configuration.PreinstallAppsManager;
import com.excel.excelclasslibrary.RetryCounter;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilURL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Sohail on 02-11-2016.
 */

public class GetPreinstallAppsInfoService extends Service {

    Context context;
    final static String TAG = "GetPreinstallAppsInfo";
    RetryCounter retryCounter;

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.d( TAG, "GetPresintallAppsInfoService started" );
        context = this;
        retryCounter = new RetryCounter( "preinstall_apps_retry_count" );

        /*UpdateApps updateApps = new UpdateApps();
        updateApps.execute( "" );*/
        updateApps();

        return START_STICKY;
    }

    private void updateApps(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String s = UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                        UtilURL.getURLParamsFromPairs( new String[][]{
                                { "mac_address", UtilNetwork.getMacAddress( context ) },
                                { "what_do_you_want", "get_preinstall_apps_info" }
                        } ));

                if( s == null ){
                    Log.d( TAG, "Failed to retrieve List of Preinstall Apps" );
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
                        Log.e( TAG, "Failed to update preinstall apps" );
                        setRetryTimer();
                        return;
                    }
                    else if( type.equals( "success" ) ){
                        PreinstallAppsManager pam = new PreinstallAppsManager();
                        pam.writePreinstallAppsFile( jsonObject.getJSONArray( "info" ) );

                        // Send Broadcast to DataDownloader to download new preinstall apks from CMS and install them, if their md5 is different
                        sendBroadcast( new Intent( "download_preinstall_apps" ) );

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

    /*class UpdateApps extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground( String... params ) {

            return UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                    UtilURL.getURLParamsFromPairs( new String[][]{
                            { "mac_address", UtilNetwork.getMacAddress( context ) },
                            { "what_do_you_want", "get_preinstall_apps_info" }
                    } ));
        }

        @Override
        protected void onPostExecute( String s ) {
            super.onPostExecute( s );

            if( s == null ){
                Log.d( TAG, "Failed to retrieve List of Preinstall Apps" );
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
                    Log.e( TAG, "Failed to update preinstall apps" );
                    setRetryTimer();
                    return;
                }
                else if( type.equals( "success" ) ){
                    PreinstallAppsManager pam = new PreinstallAppsManager();
                    pam.writePreinstallAppsFile( jsonObject.getJSONArray( "info" ) );

                    retryCounter.reset();
                }
            }
            catch( Exception e ){
                e.printStackTrace();
                setRetryTimer();
            }

        }
    }*/

    private void setRetryTimer(){
        final long time = retryCounter.getRetryTime();

        Log.d( TAG, "time : " + time );

        new Handler( Looper.getMainLooper() ).postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.d( TAG, "updating preinstall apps after "+(time/1000)+" seconds !" );
                /*UpdateApps updateApps = new UpdateApps();
                updateApps.execute( "" );*/
                updateApps();

            }

        }, time );

    }


}
