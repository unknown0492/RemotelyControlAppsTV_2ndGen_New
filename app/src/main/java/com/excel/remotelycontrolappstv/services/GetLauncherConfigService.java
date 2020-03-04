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

import com.excel.configuration.LauncherConfigManager;
import com.excel.excelclasslibrary.RetryCounter;
import com.excel.excelclasslibrary.UtilMisc;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilURL;
import com.excel.remotelycontrolappstv.secondgen.R;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.excel.excelclasslibrary.Constants.APPSTVLAUNCHER_PACKAGE_NAME;
import static com.excel.excelclasslibrary.Constants.APPSTVLAUNCHER_RECEIVER_NAME;

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
                        // context.sendBroadcast( new Intent( "receive_update_launcher_config" ) );
                        UtilMisc.sendExplicitExternalBroadcast( context, "receive_update_launcher_config", APPSTVLAUNCHER_PACKAGE_NAME, APPSTVLAUNCHER_RECEIVER_NAME );
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
