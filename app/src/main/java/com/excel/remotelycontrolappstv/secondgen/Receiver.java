package com.excel.remotelycontrolappstv.secondgen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.Constants;
import com.excel.excelclasslibrary.UtilMisc;
import com.excel.excelclasslibrary.UtilSharedPreferences;
import com.excel.excelclasslibrary.UtilShell;
import com.excel.remotelycontrolappstv.services.BroadcastAirplayService;
import com.excel.remotelycontrolappstv.services.ClearCacheService;
import com.excel.remotelycontrolappstv.services.GetBoxConfigService;
import com.excel.remotelycontrolappstv.services.GetLauncherConfigService;
import com.excel.remotelycontrolappstv.services.GetPreinstallAppsInfoService;
import com.excel.remotelycontrolappstv.services.ScheduleRebootService;
import com.excel.remotelycontrolappstv.services.UpdateBoxActiveStatusService;
import com.excel.remotelycontrolappstv.services.UpdateBoxBootStatusService;

import static com.excel.remotelycontrolappstv.util.Constants.IS_BOX_BOOTUP_TIME_UPDATED;
import static com.excel.remotelycontrolappstv.util.Constants.IS_PERMISSION_GRANTED;
import static com.excel.remotelycontrolappstv.util.Constants.PERMISSION_GRANTED_NO;
import static com.excel.remotelycontrolappstv.util.Constants.PERMISSION_SPFS;

public class Receiver extends BroadcastReceiver {

    final static String TAG = "Receiver";
    ConfigurationReader configurationReader;
    SharedPreferences spfs;

    @Override
    public void onReceive( Context context, Intent intent ) {
        String action = intent.getAction();
        Log.d( TAG, "actionR : " + action );

        // Receiver sw = (SystemWriteManager) Receiver.this.context.getSystemService("system_write");

        /*MainActivity ma = new MainActivity();

        // Check permissions before executing any broadcast, this is to prevent the app from hanging
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if ( ma.checkPermissions() ) {
                // permissions  granted.
                Log.d( TAG, "All permissions have been granted, just proceed !" );
            }
            else{
                startRemotelyControlAppsTV( context );
                return;
            }
        }*/

        // Check permissions before executing any broadcast, this is to prevent the app from hanging
        spfs = (SharedPreferences) UtilSharedPreferences.createSharedPreference( context, PERMISSION_SPFS );
        String is_permission_granted = UtilSharedPreferences.getSharedPreference( spfs, IS_PERMISSION_GRANTED, PERMISSION_GRANTED_NO ).toString().trim();
        Log.d( TAG, "Permission granted : "+is_permission_granted );

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if( is_permission_granted.equals( "yes" ) ){
                Log.d( TAG, "All permissions have been granted, just proceed !" );
            }
            else{
                startRemotelyControlAppsTV( context );
                return;
            }
        }


        configurationReader = ConfigurationReader.reInstantiate();
        Toast.makeText( context, intent.getAction(), Toast.LENGTH_SHORT ).show();

        //if( action.equals( "android.net.conn.CONNECTIVITY_CHANGE" ) ){ // Doesnt work anymore because it is deprecated in Android 7.0+
        if( action.equals( "connectivity_change" ) ){

            // 1. First time in order to receive broadcasts, the app should be started at least once
            startRemotelyControlAppsTV( context );

            // Execute these broadcasts only when the OTS IS COMPLETED
            String is_ots_completed = configurationReader.getIsOtsCompleted().trim();
            if( is_ots_completed.equals( "0" ) ) {
                Log.d( TAG, "OTS has not been completed, RemotelyControlAppsTV Broadcasts will not execute !" );
                return;
            }

            if( ! isConnectivityBroadcastFired() ) {

                // 2. Start UDP Listening service
                UtilMisc.sendExplicitInternalBroadcast( context, "start_listening_service", Receiver.class );

                // 6. Get Box Configuration (appstv_data/configuration)
                // context.sendBroadcast( new Intent( "get_box_configuration" ) );  // Commented this because, after box bootuptime broadcast, it will send this broadcast inside the service

                // . Clear Application Cache
                /*Intent in2 = new Intent( context, Receiver.class );
                in2.setAction( "clear_application_cache" );
                context.sendBroadcast( in2 );*/
                UtilMisc.sendExplicitInternalBroadcast( context, "clear_application_cache", Receiver.class );

                // 3. Get Preinstall Apps Information
                /*Intent in3 = new Intent( context, Receiver.class );
                in3.setAction( "get_preinstall_apps_info" );
                context.sendBroadcast( in3 );*/
                UtilMisc.sendExplicitInternalBroadcast( context, "get_preinstall_apps_info", Receiver.class );

                // 4. Get Launcher Config
                /*Intent in4 = new Intent( context, Receiver.class );
                in4.setAction( "get_launcher_config" );
                context.sendBroadcast( in4 );*/
                UtilMisc.sendExplicitInternalBroadcast( context, "get_launcher_config", Receiver.class );

                // 5. Download Wallpapers (To be handed under DataDownloader)
                /*Intent in5 = new Intent( context, Receiver.class );
                in5.setAction( "get_wallpapers" );
                context.sendBroadcast( in5 );*/
                UtilMisc.sendExplicitExternalBroadcast( context, "get_wallpapers", Constants.DATADOWNLOADER_PACKAGE_NAME, Constants.DATADOWNLOADER_RECEIVER_NAME );
                //UtilMisc.sendExplicitInternalBroadcast( context, "get_wallpapers", Receiver.class );

                // 7. Schedule Reboot Alarm
                /*Intent in6 = new Intent( context, Receiver.class );
                in6.setAction( "schedule_reboot" );
                context.sendBroadcast( in6 );*/
                UtilMisc.sendExplicitInternalBroadcast( context, "schedule_reboot", Receiver.class );

                setConnectivityBroadcastFired( true );
            }
        }
        else if( action.equals( "android.intent.action.BOOT_COMPLETED" ) || action.equals( "boot_completed" ) ){
            // context.sendBroadcast( new Intent( "update_box_bootup_time" ) );
            UtilMisc.sendExplicitInternalBroadcast( context, "update_box_bootup_time", Receiver.class );
        }
        else if( action.equals( "start_listening_service" ) ){
            startListeningService( context );
        }
        else if( action.equals( "update_box_bootup_time" ) ){
            updateBoxBootUpTime( context );
        }
        else if( action.equals( "update_box_active_status" ) ){
            //context.sendBroadcast( new Intent( "update_box_bootup_time" ) );
            updateBoxActiveStatus( context );
        }
        else if( action.equals( "get_preinstall_apps_info" ) ){
            getPreinstallAppsInfo( context );
        }
        else if( action.equals( "get_launcher_config" ) ){
            getLauncherConfig( context );
        }
        else if( action.equals( "get_box_configuration" ) ){
            getBoxConfiguration( context );
        }
        else if( action.equals( "broadcast_airplay_credentials" ) ){
            broadcastAirplayCredentials( context );
        }
        else if( action.equals( "schedule_reboot" ) ){
            scheduleReboot( context, true );
        }
        else if( action.equals( "postpone_reboot" ) ){
            scheduleReboot( context, false );
        }
        else if( action.equals( "clear_application_cache" ) ){
            clearApplicationCache( context );
        }
        else if( action.equals( "execute_script" ) ){
            executePushedScripts( context, intent );
        }
    }



    private void startRemotelyControlAppsTV( Context context ){
        // Start this app activity
        Intent in = new Intent( context, MainActivity.class );
        in.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity( in );
    }

    private void startListeningService( Context context ){
        Intent in = new Intent( context, ListeningService.class );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void updateBoxBootUpTime( Context context ){

        if( ! isBoxBootupTimeUpdated() ){
            Log.i( TAG, "Updating Box Bootup Time" );
            Intent in = new Intent( context, UpdateBoxBootStatusService.class );
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
                context.startForegroundService( in );
            }
            else{
                context.startService( in );
            }
        }
    }

    private void updateBoxActiveStatus( Context context ){
        Intent in = new Intent( context, UpdateBoxActiveStatusService.class );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void getPreinstallAppsInfo( Context context ){
        Intent in = new Intent( context, GetPreinstallAppsInfoService.class );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void getLauncherConfig( Context context ){
        Intent in = new Intent( context, GetLauncherConfigService.class );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void getBoxConfiguration( Context context ){
        Intent in = new Intent( context, GetBoxConfigService.class );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void broadcastAirplayCredentials( Context context ){
        Intent in = new Intent( context, BroadcastAirplayService.class );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void scheduleReboot( Context context, boolean is_schedule ){
        Intent in = new Intent( context, ScheduleRebootService.class );
        in.putExtra( "is_schedule", is_schedule );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void clearApplicationCache( Context context ){
        Intent in = new Intent( context, ClearCacheService.class );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
            context.startForegroundService( in );
        }
        else{
            context.startService( in );
        }
    }

    private void setConnectivityBroadcastFired( boolean is_it ){
        String s = (is_it)?"1":"0";
        Log.d( TAG, "setConnectivityBroadcastFired() : " + s );
        UtilShell.executeShellCommandWithOp( "setprop rc_br_fired " + s );
    }

    private boolean isConnectivityBroadcastFired(){
        String is_it = UtilShell.executeShellCommandWithOp( "getprop rc_br_fired" ).trim();
        return ( is_it.equals( "0" ) || is_it.equals( "" ) )?false:true;
    }

    private void executePushedScripts( Context context, Intent in ){
        String[] scripts = in.getStringArrayExtra( "scripts" );
        for( int i = 0 ; i < scripts.length ; i++ ){
            Log.d( TAG, "Executing script " + scripts[ i ] );
            UtilShell.executeShellCommandWithOp( scripts[ i ] );
        }
    }


    public static void setBoxBootupTimeUpdated( boolean is_it ){
        String s = (is_it)?"1":"0";
        Log.d( TAG, "setBoxBootupTimeUpdated() : " + s );
        UtilShell.executeShellCommandWithOp( "setprop " + IS_BOX_BOOTUP_TIME_UPDATED + " " + s );
    }

    public static boolean isBoxBootupTimeUpdated(){
        String is_it = UtilShell.executeShellCommandWithOp( "getprop "+IS_BOX_BOOTUP_TIME_UPDATED ).trim();
        return ( is_it.equals( "0" ) || is_it.equals( "" ) )?false:true;
    }
}
