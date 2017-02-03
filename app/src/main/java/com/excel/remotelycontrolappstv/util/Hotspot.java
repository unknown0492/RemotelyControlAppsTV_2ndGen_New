package com.excel.remotelycontrolappstv.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;

public class Hotspot {
	
	final static String DEFAULT_SSID 	 = "AndroidAP";
	final static String DEFAULT_PASSWORD = "123456789";
	final static String TAG 			 = "Hotspot";
	
	/**
     * Turn on or off Hotspot.
     * 
     * @param context
     * @param isTurnToOn
     */
    public static void turnOnOffHotspot( Context context, boolean isTurnToOn ) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiApControl apControl = WifiApControl.getApControl(wifiManager);
        
        if ( apControl != null ) {
 
                 // TURN OFF YOUR WIFI BEFORE ENABLE HOTSPOT
            //if (isWifiOn(context) && isTurnToOn) {
            //  turnOnOffWifi(context, false);
            //}
        	Log.i( TAG, "inside turnOnOffHotspot" );

			ConfigurationReader configurationReader = ConfigurationReader.reInstantiate();
			String ssid = configurationReader.getSSID();
			String hotspot_password = configurationReader.getHotspotPassword();

			if( ssid.equals( "-" ) ) ssid = DEFAULT_SSID;
			if( hotspot_password.equals( "-" ) ) hotspot_password = DEFAULT_PASSWORD;

			WifiConfiguration wifi_config = apControl.getWifiApConfiguration();
        	wifi_config.SSID = ssid;
        	wifi_config.preSharedKey = hotspot_password;
        	
        	Log.i( TAG, "SSID : " + ssid );
        	Log.i( TAG, "Password : " + hotspot_password );
        	
        	// If wIfi AP is enabled and there is a need to Change the credentials,
        	// So, first disable the ap, change the credentials, then turn on the ap back
        	if( apControl.isWifiApEnabled() && isTurnToOn ){
        		apControl.setWifiApEnabled( wifi_config,
                        false );
        		try{
        			Thread.sleep( 3000 );
        		}
        		catch( Exception e ){
        			e.printStackTrace();
        		}
        		Log.i( TAG, "AP already enabled and requested for parameter change" );
        	}
        	else if( !apControl.isWifiApEnabled() && !isTurnToOn ){
        		apControl.setWifiApEnabled( wifi_config,
                        true );
        		try{
        			Thread.sleep( 3000 );
        		}
        		catch( Exception e ){
        			e.printStackTrace();
        		}
        		Log.i( TAG, "AP already DISABLED and requested for parameter change" );
        	}

            apControl.setWifiApEnabled( wifi_config,
                    isTurnToOn );
            
            
            // Check if Airplay is enabled/disabled
            /*String airplay_enabled = Functions.readData( "OTS", "airplay_enabled.txt" ).trim();
            Log.i( TAG, "airplay_enabled : "+airplay_enabled );
            if( airplay_enabled.equals( "1" ) ){
            	context.sendBroadcast( new Intent( "broadcast_airplay" ) );
            }*/
        }
    }
    

}
