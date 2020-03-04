package com.excel.remotelycontrolappstv.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.RetryCounter;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilURL;
import com.excel.remotelycontrolappstv.secondgen.R;

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
                context.sendBroadcast( new Intent( "get_box_configuration" ) );

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
