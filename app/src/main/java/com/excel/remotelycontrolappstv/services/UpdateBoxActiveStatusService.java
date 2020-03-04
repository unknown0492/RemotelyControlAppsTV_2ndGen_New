package com.excel.remotelycontrolappstv.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilURL;
import com.excel.remotelycontrolappstv.secondgen.R;

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

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(this, "test" );
        notificationBuilder.setSmallIcon( R.drawable.ic_launcher );
        notificationManager.notify(0, notificationBuilder.build());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( "test",TAG, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel( channel );

            Notification notification = new Notification.Builder(getApplicationContext(),"test").build();
            startForeground(1, notification);
        }
        else {
            // startForeground(1, notification);
        }
    }

    private void updateTime(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d( TAG, "Updating box active status with IP : " + UtilNetwork.getLocalIpAddressIPv4( context ) );
                String s = UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                        UtilURL.getURLParamsFromPairs( new String[][]{
                                { "mac_address", UtilNetwork.getMacAddress( context ) },
                                { "ip", UtilNetwork.getLocalIpAddressIPv4( context ) },
                                { "what_do_you_want", "update_box_active_status" }
                        } ));

                if( s == null ){
                    Log.d( TAG, "Failed to update Box Active Status" );
                    //setRetryTimer();
                    return;
                }

                Log.d( TAG, "response : "+s );

                //Receiver.setBoxBootupTimeUpdated( true );

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
