package com.excel.remotelycontrolappstv.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.RetryCounter;
import com.excel.excelclasslibrary.UtilMisc;
import com.excel.excelclasslibrary.UtilShell;
import com.excel.remotelycontrolappstv.secondgen.Receiver;

import java.util.Calendar;

public class ScheduleRebootService extends Service {
    Context context = this;
    final static String TAG = "ScheduleRebootService";
    RetryCounter retryCounter;
    Calendar cal;
    int alarm_hours, alarm_minutes;
    boolean skip = false;
    ConfigurationReader configurationReader;


    @Override
    public IBinder onBind( Intent intent ) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.i( TAG, "ScheduleRebootService started" );
        retryCounter = new RetryCounter( "schedule_reboot_counter" );

        if( ! getWasInternetTimeSyncSuccessful() ){
            setRetryCounter();
            return START_STICKY;
        }

        boolean is_schedule = intent.getBooleanExtra( "is_schedule", true );

        if( is_schedule )
            scheduleRebootAlarm( context );
        else
            postponeRebootAlarm( context );

        return START_NOT_STICKY;
    }

    private void readRebootScheduleFromBuildProp( Context context ){
        configurationReader = ConfigurationReader.reInstantiate();
        // Check current time
        // If current time is greater than 4am, then set alarm for next day 4am

        cal = Calendar.getInstance();
        String is_reboot_scheduled = configurationReader.getIsRebootScheduled();//UtilShell.executeShellCommandWithOp( "getprop is_reboot_scheduled" ).trim();
        Log.d( TAG, "is_reboot_scheduled ,"+is_reboot_scheduled+"," );
        if( is_reboot_scheduled.equals( "" ) || is_reboot_scheduled.equals( "0" ) ){
            // Means, this feature is not required, dont set the alarm
            skip = true;
            return;
        }

        String reboot_time = configurationReader.getRebootTime();//UtilShell.executeShellCommandWithOp( "getprop reboot_time" ).trim();
        if( reboot_time.equals( "" ) ){
            // Means, reboot time not set, use the default time as 4am
            alarm_hours		= 4;
            alarm_minutes	= 0;
        }
        else{
            String[] split_time = reboot_time.split( ":" );
            try{
                alarm_hours		= Integer.parseInt( split_time[ 0 ] );
            }
            catch( Exception e ){
                alarm_hours		= 4;
                e.printStackTrace();
            }

            try{
                alarm_minutes	= Integer.parseInt( split_time[ 1 ] );
            }
            catch( Exception e ){
                alarm_minutes	= 0;
                e.printStackTrace();
            }
        }



    }

    public void scheduleRebootAlarm( Context context ){
        readRebootScheduleFromBuildProp( context );

        if( skip )
            return;

        Log.d( TAG, "scheduleRebootAlarm()" );

        AlarmManager am1 =( AlarmManager ) context.getSystemService( Context.ALARM_SERVICE );
        Intent in1 = new Intent( "show_reboot_prompt" );
        PendingIntent pi1 = PendingIntent.getBroadcast( context, 0, in1, 0 );

        int current_hours = cal.get( Calendar.HOUR_OF_DAY );
        if( current_hours >= alarm_hours ){ // Then set alarm for the next day
            /*cal.set( Calendar.DAY_OF_MONTH, cal.get( Calendar.DATE ) );
            cal.set( Calendar.HOUR_OF_DAY, 12 );
            cal.set( Calendar.MINUTE, cal.get( Calendar.MINUTE )+1 );*/

            cal.add( Calendar.DATE, 1 ); // next day
            cal.set( Calendar.DAY_OF_MONTH, cal.get( Calendar.DATE ) );
            //cal.set( Calendar.MONTH, cal.get( Calendar.MONTH ) );
            cal.set( Calendar.HOUR_OF_DAY, alarm_hours );
            cal.set( Calendar.MINUTE, alarm_minutes );

            // am1.cancel( pi1 );
            Log.d( TAG, "millis : "+cal.getTimeInMillis());
            am1.set( AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi1 );
        }
        else{
            cal.set( Calendar.DAY_OF_MONTH, cal.get( Calendar.DATE ) );
            cal.set( Calendar.HOUR_OF_DAY, alarm_hours );
            cal.set( Calendar.MINUTE, alarm_minutes );

            am1.set( AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi1 );
        }

        Log.d( TAG,  "Alarm Day : "+cal.get( Calendar.DATE ) );
        Log.d( TAG,  "Alarm Month : "+cal.get( Calendar.MONTH ) );
        Log.d( TAG,  "Alarm Year : "+cal.get( Calendar.YEAR ) );
        Log.d( TAG,  "Alarm Hours : "+cal.get( Calendar.HOUR_OF_DAY ) );
        Log.d( TAG,  "Alarm Minutes : "+cal.get( Calendar.MINUTE ) );
        Log.d( TAG,  "Alarm Seconds : "+cal.get( Calendar.SECOND ) );
    }

    public void postponeRebootAlarm( Context context  ){
        readRebootScheduleFromBuildProp( context );

        Log.d( TAG, "postponeRebootAlarm()" );

        AlarmManager am1 =( AlarmManager ) context.getSystemService( Context.ALARM_SERVICE );
        Intent in1 = new Intent( "show_reboot_prompt" );
        PendingIntent pi1 = PendingIntent.getBroadcast( context, 0, in1, 0 );

        //int current_hours = cal.get( Calendar.HOUR_OF_DAY );
        cal.add( Calendar.HOUR, 1 ); // next hour
        cal.set( Calendar.DAY_OF_MONTH, cal.get( Calendar.DATE ) );
        am1.set( AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi1 );

        Log.d( TAG,  "Alarm Day : "+cal.get( Calendar.DATE ) );
        Log.d( TAG,  "Alarm Hours : "+cal.get( Calendar.HOUR_OF_DAY ) );
        Log.d( TAG,  "Alarm Minutes : "+cal.get( Calendar.MINUTE ) );
    }


    private void setRetryCounter(){
        final long time = retryCounter.getRetryTime();
        Log.d( TAG, "Retry Counter Set for "+time+" milliseconds" );

        new Handler( Looper.getMainLooper() ).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d( TAG, "Scheduling Reboot Alarm after "+(time/1000)+" seconds !" );
                // sendBroadcast( new Intent( "schedule_reboot" ) );
                UtilMisc.sendExplicitInternalBroadcast( context, "schedule_reboot", Receiver.class );
            }
        }, retryCounter.getRetryTime() );
    }

    public boolean getWasInternetTimeSyncSuccessful() {
        String d = UtilShell.executeShellCommandWithOp( "getprop wasInternetTimeSyncSuccessful" );
        return (d.trim().equals( "1" ) )?true:false;
    }
}
