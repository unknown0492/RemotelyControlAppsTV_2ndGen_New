package com.excel.remotelycontrolappstv.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;
import com.excel.remotelycontrolappstv.util.Hotspot;

public class HotspotStarterService extends Service{
    Context context = this;
	final static String TAG = "HotspotStarterService";

    public IBinder onBind(Intent intent){
        return null;
    }

    public int onStartCommand( Intent intent, int i, int j ){
    	Log.i( TAG, "HotspotStarterService : inside" );

        String json = intent.getStringExtra( "json" );
        if( json != null ){
            // Save the JSON data received from ListeningService file when broadcast is sent
            // This json data contains information about Hotspot enabled, ssid and password
            //...
            //...
            //...
        }

		ConfigurationReader configurationReader = ConfigurationReader.reInstantiate();
		String hotspot_enabled = configurationReader.getHotspotEnabled();

		if( hotspot_enabled.equals( "1" ) ){
			Log.i( TAG, "Turning On Hotspot" );
    		Hotspot.turnOnOffHotspot( this, true );

			/*String airplay_enabled = configurationReader.getAirplayEnabled();
			if( airplay_enabled.equals( "1" ) ){
				*//*String is_airplay_ssid_same_as_tethering_ssid = configurationReader.getIsAirplaySSIDSameAsTetheringSSID();
                if( is_airplay_ssid_same_as_tethering_ssid.equals( "1" ) ){

                }*//*
                sendBroadcast( new Intent( "broadcast_airplay_credentials" ) );

			}*/

    	}
    	else if( hotspot_enabled.equals( "0" ) ){
			Log.d( TAG, "Turning Off Hotspot" );
			Hotspot.turnOnOffHotspot( this, false );
    	}

		/*Intent in = new Intent( this, BroadcastAirplayService.class );
		startService( in );*/
		sendBroadcast( new Intent( "broadcast_airplay_credentials" ) );
		//if( configurationReader.getAirplayEnabled().equals( "1" ) )
        //	sendBroadcast( new Intent( "broadcast_airplay_credentials" ) );
    	//else{
    	//	Log.e( TAG, "Airplay has been disabled for this box !" );
		//}
        return START_NOT_STICKY;
    }
}
