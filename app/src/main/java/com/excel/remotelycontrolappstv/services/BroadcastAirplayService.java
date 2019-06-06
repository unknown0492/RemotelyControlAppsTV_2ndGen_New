package com.excel.remotelycontrolappstv.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;

import java.util.Date;

public class BroadcastAirplayService extends Service {

	final public static String TAG = "BroadcastAirplayService";
	final public static String AIRPLAY_DEFAULT_USERNAME = "AndroidAP";
	final public static String AIRPLAY_DEFAULT_PASSWORD = "123456789";
	
	Context context = this;
	
	@Override
	public IBinder onBind( Intent intent ) {
		return null;
	}

	@Override
	public int onStartCommand( Intent intent, int flags, int startId ) {
		Log.i( TAG, "BroadcastAirplay Service started at "+new Date().toString() );

        String json = intent.getStringExtra( "json" );
        if( json != null ){
            // Save the JSON data received from ListeningService file when broadcast is sent
            // This json data contains information about Airplay enabled, ssid and password
            //...
            //...
            //...
        }

		ConfigurationReader configurationReader = ConfigurationReader.reInstantiate();


		// Read the UserID and password from the Tethering Hotspot Text File
		String nickname = configurationReader.getSSID();
		String password = "";//configurationReader.getHotspotPassword();

		String is_airplay_enabled = configurationReader.getAirplayEnabled();

		if( is_airplay_enabled.equals( "1" ) ) {
			String is_airplay_ssid_same_as_tethering_ssid = configurationReader.getIsAirplaySSIDSameAsTetheringSSID();
			if( !is_airplay_ssid_same_as_tethering_ssid.equals( "1" ) ){
				nickname = configurationReader.getAirplaySSID();
                password = configurationReader.getAirplayPassword();
                if( password.equals( "-" ) )
                    password = "";
			}

			//disableAirplay();
			//disableDLNA();

			updateAirplay();
			updateDLNA();
			updateCredentials( nickname, password );
		}
		else{
			// Disable the Airplay
			Log.e( TAG, "Airplay is disabled for this box" );
            //disableAirplay();
            //disableDLNA();
		}

		return START_STICKY;
	}
	
	public void updateAirplay(){
		Log.i( TAG, "Inside updateAirplay" );
		Intent in = new Intent( "com.waxrain.airpin.UPDATE_AIRPLAY" );
		in.putExtra( "accessid", "PRJ:AirPlayer|ID:760ba1810b647cb0" );
		in.putExtra( "option", 1 );
		in.putExtra( "audioen", 1 );
		in.putExtra( "bindif", "wlan0" );
		//in.putExtra( "bindif", "eth0" );
		sendBroadcast( in );
	}
	
	public void updateDLNA(){
		Log.i( TAG, "Inside updateDLNA" );
		Intent in = new Intent( "com.waxrain.airpin.UPDATE_DLNA" );
		in.putExtra( "accessid", "PRJ:AirPlayer|ID:760ba1810b647cb0" );
		in.putExtra( "option", 1 );
		in.putExtra( "audioen", 1 );
		in.putExtra( "bindif", "wlan0" );
		//in.putExtra( "bindif", "eth0" );
		sendBroadcast( in );
	}
	
	public void updateCredentials( String nickname, String password ){
		Log.i( TAG, "Inside updateCredentials" );
		Intent in = new Intent( "com.waxrain.airpin.UPDATE_NICKNAME" );
		in.putExtra( "accessid", "PRJ:AirPlayer|ID:760ba1810b647cb0" );
		in.putExtra( "nickname", nickname );
		sendBroadcast( in );
		
		in = new Intent( "com.waxrain.airpin.UPDATE_PASSWORD" );
		in.putExtra( "accessid", "PRJ:AirPlayer|ID:760ba1810b647cb0" );
		in.putExtra( "password", password );
		sendBroadcast( in );
	}

    public void disableDLNA(){
        Log.i( TAG, "Inside disableDLNA" );
        Intent in = new Intent( "com.waxrain.airpin.UPDATE_DLNA" );
        in.putExtra( "accessid", "PRJ:AirPlayer|ID:760ba1810b647cb0" );
        in.putExtra( "option", 0 ); // 0 is to disable, 1 is to enable
        in.putExtra( "audioen", 0 ); // 0 is to disable, 1 is to enable
        in.putExtra( "bindif", "wlan0" );
        sendBroadcast( in );
    }

    public void disableAirplay(){
        Log.i( TAG, "Inside disableAirplay" );
        Intent in = new Intent( "com.waxrain.airpin.UPDATE_AIRPLAY" );
        in.putExtra( "accessid", "PRJ:AirPlayer|ID:760ba1810b647cb0" );
        in.putExtra( "option", 0 ); // 0 is to disable, 1 is to enable
        in.putExtra( "audioen", 0 ); // 0 is to disable, 1 is to enable
        in.putExtra( "bindif", "wlan0" );
    }

}
