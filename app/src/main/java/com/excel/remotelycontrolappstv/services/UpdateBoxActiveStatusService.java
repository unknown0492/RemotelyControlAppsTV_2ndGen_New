package com.excel.remotelycontrolappstv.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilURL;

public class UpdateBoxActiveStatusService extends Service{

    final static String TAG = "UpdateBoxActiveStatus";
    Context context;
    //ConfigurationReader configurationReader;

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.d( TAG, "UpdateBoxActiveStatusService started" );

        context = this;
        //configurationReader = ConfigurationReader.getInstance();
        /*UpdateTime ut = new UpdateTime();
        ut.execute("");*/

        /*if( UtilNetwork.isConnectedToInternet( context ) ){
            Log.d( TAG, "internet connected" );
            UpdateTime ut = new UpdateTime();
            ut.execute("");
            //update();
        }
        else{
            Log.d( TAG, "internet NOT connected" );
            // setRetryTimer();
        }*/

        updateTime();

        return START_STICKY;
    }

    private void updateTime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String s = UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                        UtilURL.getURLParamsFromPairs( new String[][]{
                                { "mac_address", UtilNetwork.getMacAddress( context ) },
                                { "what_do_you_want", "update_box_active_status" }
                        } ));

                if( s == null ){
                    Log.d( TAG, "Failed to update Box Active Status" );
                    //setRetryTimer();
                    return;
                }

                Log.d( TAG, "response : "+s );
            }
        }).start();
    }

    /*class UpdateTime extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground( String... params ) {

            return UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                    UtilURL.getURLParamsFromPairs( new String[][]{
                            { "mac_address", UtilNetwork.getMacAddress( context ) },
                            { "what_do_you_want", "update_box_active_status" }
                    } ));
        }

        @Override
        protected void onPostExecute( String s ) {
            super.onPostExecute( s );

            if( s == null ){
                Log.d( TAG, "Failed to update Box Active Status" );
                //setRetryTimer();
                return;
            }

            Log.d( TAG, "response : "+s );
        }
    }*/
}
