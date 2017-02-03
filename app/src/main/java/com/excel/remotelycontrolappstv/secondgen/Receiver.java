package com.excel.remotelycontrolappstv.secondgen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.excel.excelclasslibrary.UtilShell;
import com.excel.remotelycontrolappstv.services.BroadcastAirplayService;
import com.excel.remotelycontrolappstv.services.GetBoxConfigService;
import com.excel.remotelycontrolappstv.services.GetLauncherConfigService;
import com.excel.remotelycontrolappstv.services.GetPreinstallAppsInfoService;
import com.excel.remotelycontrolappstv.services.UpdateBoxActiveStatusService;
import com.excel.remotelycontrolappstv.services.UpdateBoxBootStatusService;


public class Receiver extends BroadcastReceiver {

    final static String TAG = "Receiver";

    @Override
    public void onReceive( Context context, Intent intent ) {
        String action = intent.getAction();
        Log.d( TAG, "action : " + action );

        if( action.equals( "android.net.conn.CONNECTIVITY_CHANGE" ) || action.equals( "connectivity_changed" ) ){

            // 1. First time in order to receive broadcasts, the app should be started at least once
            startRemotelyControlAppsTV( context );

            if( ! isConnectivityBroadcastFired() ) {

                // 2. Start UDP Listening service
                context.sendBroadcast(new Intent("start_listening_service"));

                // 3. Get Preinstall Apps Information
                context.sendBroadcast(new Intent("get_preinstall_apps_info"));

                // 4. Get Launcher Config
                context.sendBroadcast(new Intent("get_launcher_config"));

                // 5. Download Wallpapers (To be handed under DataDownloader)
                context.sendBroadcast(new Intent("get_wallpapers"));

                // 6. Get Box Configuration (appstv_data/configuration)
                context.sendBroadcast(new Intent("get_box_configuration"));

                setConnectivityBroadcastFired( true );
            }

        }
        else if( action.equals( "android.intent.action.BOOT_COMPLETED" ) || action.equals( "boot_completed" ) ){
            context.sendBroadcast( new Intent( "update_box_bootup_time" ) );
        }
        else if( action.equals( "start_listening_service" ) ){
            startListeningService( context );
        }
        else if( action.equals( "update_box_bootup_time" ) ){
            updateBoxBootUpTime( context );
        }
        else if( action.equals( "update_box_active_status" ) ){
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
    }

    private void startRemotelyControlAppsTV( Context context ){
        // Start this app activity
        Intent in = new Intent( context, MainActivity.class );
        in.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
        context.startActivity( in );
    }

    private void startListeningService( Context context ){
        Intent in = new Intent( context, ListeningService.class );
        context.startService( in );
    }

    private void updateBoxBootUpTime( Context context ){
        Intent in = new Intent( context, UpdateBoxBootStatusService.class );
        context.startService( in );
    }

    private void updateBoxActiveStatus( Context context ){
        Intent in = new Intent( context, UpdateBoxActiveStatusService.class );
        context.startService( in );
    }

    private void getPreinstallAppsInfo( Context context ){
        Intent in = new Intent( context, GetPreinstallAppsInfoService.class );
        context.startService( in );
    }

    private void getLauncherConfig( Context context ){
        Intent in = new Intent( context, GetLauncherConfigService.class );
        context.startService( in );
    }

    private void getBoxConfiguration( Context context ){
        Intent in = new Intent( context, GetBoxConfigService.class );
        context.startService( in );
    }

    private void broadcastAirplayCredentials( Context context ){
        Intent in = new Intent( context, BroadcastAirplayService.class );
        context.startService( in );
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
}
