package com.excel.remotelycontrolappstv.util;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;
import com.excel.excelclasslibrary.UtilShell;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Hotspot {
	
	final static String DEFAULT_SSID 	 = "AndroidAP";
	final static String DEFAULT_PASSWORD = "123456789";
	final static String TAG 			 = "Hotspot";

	final static String HOTSPOT_SDMC_BRIDGE_KEY = "persist.sdmc.tether.running";

	/**
     * Turn on or off Hotspot.
     * 
     * @param context
     * @param isTurnToOn
     */
    public static void turnOnOffHotspot(final Context context, boolean isTurnToOn ) {
        WifiManager wifiManager = (WifiManager) context.getSystemService( Context.WIFI_SERVICE );
        final WifiApControl apControl = WifiApControl.getApControl( wifiManager );

        // Check if Chromecast Mode is ON/OFF from the config file
        ConfigurationReader configurationReader = ConfigurationReader.reInstantiate();

        boolean isChromecastModeOn = (configurationReader.getChromecastModeOn().equals( "1")?true:false);
        Log.d( TAG, String.valueOf( isChromecastModeOn ) + "," + configurationReader.getChromecastModeOn() );

        if( apControl != null ){
            /*
            //TURN OFF YOUR WIFI BEFORE ENABLE HOTSPOT
            if (isWifiOn(context) && isTurnToOn) {
                turnOnOffWifi(context, false);
            }
            */

            String ssid = configurationReader.getSSID();
            String hotspot_password = configurationReader.getHotspotPassword();

            if( ssid.equals( "-" ) ) ssid = DEFAULT_SSID;
            if( hotspot_password.equals( "-" ) ) hotspot_password = DEFAULT_PASSWORD;

            final WifiConfiguration wifi_config = apControl.getWifiApConfiguration();
            wifi_config.SSID = ssid;
            wifi_config.preSharedKey = hotspot_password;

            /*
            * Case - 1 : Hotspot already ON on the box, but another request comes from CMS to Turn ON the Hotspot --> Change of SSID/Password
            * Case - 2 : Hotspot already OFF on the box, but another request comes from the CMS to Turn OFF the Hotspot --> Change of SSID/Password
            * Case - 3 : Hotspot ON on the box, request comes from CMS to Turn OFF the Hotspot
            * Case - 4 : Hotspot OFF on the box, request comes from CMS to Turn ON the Hotspot
            *
            **/


            // For traditional Hotspot Feature
            if( !isChromecastModeOn ) {
                // Case - 1
                if (apControl.isWifiApEnabled() && isTurnToOn) {
                    // Need to Turn OFF, then Turn ON the Hotspot again so that SSID and Password would change
                    apControl.setWifiApEnabled(wifi_config, false);
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            apControl.setWifiApEnabled(wifi_config, true);
                        }

                    }, 3000);
                }
                // Case - 2
                else if (!apControl.isWifiApEnabled() && !isTurnToOn) {
                    // Need not do anything, because, the new SSID/Password will automatically reflect when Hotspot request for Turn ON comes
                }
                // Case - 3
                else if (apControl.isWifiApEnabled() && !isTurnToOn) {
                    apControl.setWifiApEnabled(wifi_config, false);
                }
                // Case - 4
                else if (!apControl.isWifiApEnabled() && isTurnToOn) {
                    apControl.setWifiApEnabled(wifi_config, true);
                }
            }
            else{
                // For Bridged Hotspot feature
                Log.d( TAG, "inside bridged feature ! " );
                // Case - 1
                if ( apControl.isWifiApEnabled() && isTurnToOn ) {
                    // Need to Turn OFF, then Turn ON the Hotspot again so that SSID and Password would change
                    setProperty( HOTSPOT_SDMC_BRIDGE_KEY, "0" );
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            setProperty( HOTSPOT_SDMC_BRIDGE_KEY, "1" );
                            new Handler().postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    apControl.setWifiApEnabled(wifi_config, true);
                                }

                            }, 1000);
                        }

                    }, 10000 );
                }
                // Case - 2
                else if (!apControl.isWifiApEnabled() && !isTurnToOn) {
                    // Need not do anything, because, the new SSID/Password will automatically reflect when Hotspot request for Turn ON comes
                }
                // Case - 3
                else if (apControl.isWifiApEnabled() && !isTurnToOn) {
                    setProperty( HOTSPOT_SDMC_BRIDGE_KEY, "0" );
                    apControl.setWifiApEnabled(wifi_config, false);
                }
                // Case - 4
                else if (!apControl.isWifiApEnabled() && isTurnToOn) {
                    setProperty( HOTSPOT_SDMC_BRIDGE_KEY, "1" );
                    apControl.setWifiApEnabled(wifi_config, true);
                }

                // Send a broadcast to update the box active status, it will update the new IP of the box into the CMS
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        Log.d( TAG, "Running after 10 secs, update box active status" );
                        context.sendBroadcast( new Intent( "update_box_active_status" ) );
                    }

                }, 10000 );
            }

        }

        /*
        if ( apControl != null ) {
 

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

            // Case - 1 : Hotspot was enabled in the CMS, now there is another request to enable Hotspot, Therefore this request is more likely for the change of ssid/password
            if( apControl.isWifiApEnabled() && isTurnToOn ){
                UtilShell.executeShellCommandWithOp( "setprop persist.sdmc.tether.running 0" );
                try{
                    Log.d( TAG, "Sleeping for 10 secs" );
                    Thread.sleep( 10000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 10 secs, turning oFF Hotspot" );
                apControl.setWifiApEnabled( wifi_config, false );
                Log.d( TAG, "Hotspot Disabled" );
                try{
                    Log.d( TAG, "Sleeping for 2 secs" );
                    Thread.sleep( 2000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 2 secs" );

                UtilShell.executeShellCommandWithOp( "setprop persist.sdmc.tether.running 1" );
                try{
                    Log.d( TAG, "Sleeping for 10 secs" );
                    Thread.sleep( 10000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 10 secs, turning oN Hotspot" );
                apControl.setWifiApEnabled( wifi_config, true );
                Log.d( TAG, "Hotspot Enabled" );

            }
            // Case - 2 : Hotspot was disabled in the CMS, and Hotspot was Turned OFF on the box, and request for enabling comes
            else if( !apControl.isWifiApEnabled() && isTurnToOn ){
                UtilShell.executeShellCommandWithOp( "setprop persist.sdmc.tether.running 1" );
                try{
                    Log.d( TAG, "Sleeping for 10 secs" );
                    Thread.sleep( 10000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 10 secs, turning oN Hotspot" );
                apControl.setWifiApEnabled( wifi_config, true );
                Log.d( TAG, "Hotspot Enabled" );
            }
            // Case - 3 : Hotspot was enabled in the CMS, and Hotspot was Turned OM on the box, and request for disabling comes
            else if( apControl.isWifiApEnabled() && !isTurnToOn ){
                UtilShell.executeShellCommandWithOp( "setprop persist.sdmc.tether.running 0" );
                try{
                    Log.d( TAG, "Sleeping for 10 secs" );
                    Thread.sleep( 10000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 10 secs, turning oFF Hotspot" );
                apControl.setWifiApEnabled( wifi_config, false );
                Log.d( TAG, "Hotspot Disabled" );
            }
            // Case - 4 : Hotspot was DISABLED in the CMS, now there is another request to DISABLE Hotspot, Therefore this request is more likely for the change of ssid/password
            else if( apControl.isWifiApEnabled() && isTurnToOn ){
                UtilShell.executeShellCommandWithOp( "setprop persist.sdmc.tether.running 1" );
                try{
                    Log.d( TAG, "Sleeping for 10 secs" );
                    Thread.sleep( 10000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 10 secs, turning oN Hotspot" );
                apControl.setWifiApEnabled( wifi_config, true );
                Log.d( TAG, "Hotspot Enabled" );

                UtilShell.executeShellCommandWithOp( "setprop persist.sdmc.tether.running 0" );
                try{
                    Log.d( TAG, "Sleeping for 10 secs" );
                    Thread.sleep( 10000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 10 secs, turning oFF Hotspot" );
                apControl.setWifiApEnabled( wifi_config, false );
                Log.d( TAG, "Hotspot Disabled" );
                try{
                    Log.d( TAG, "Sleeping for 2 secs" );
                    Thread.sleep( 2000 );
                }
                catch( Exception e ){
                    e.printStackTrace();
                }
                Log.d( TAG, "wakeUp after 2 secs" );

            }*/















        	/*
        	// If wIfi AP is enabled and there is a need to Change the credentials,
        	// So, first disable the ap, change the credentials, then turn on the ap back
        	if( apControl.isWifiApEnabled() && isTurnToOn ){
        		apControl.setWifiApEnabled( wifi_config, false );
        		try{
        			Thread.sleep( 3000 );
        		}
        		catch( Exception e ){
        			e.printStackTrace();
        		}
        		Log.i( TAG, "AP already enabled and requested for parameter change" );
        	}
        	else if( !apControl.isWifiApEnabled() && !isTurnToOn ){
        		apControl.setWifiApEnabled( wifi_config, true );
        		try{
        			Thread.sleep( 3000 );
        		}
        		catch( Exception e ){
        			e.printStackTrace();
        		}
        		Log.i( TAG, "AP already DISABLED and requested for parameter change" );
        	}

            apControl.setWifiApEnabled( wifi_config, isTurnToOn );
            */
            

        // }
    }

    public static void setProperty(String key,String value) {
        Log.i(TAG, "key:"+key+" value:"+value);
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method method = cls.getMethod("set",String.class,String.class);
            method.invoke(null, key,value);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            Log.i(TAG, e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.i(TAG, e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            Log.i(TAG, e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.i(TAG, e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Log.i(TAG, e.toString());
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

}
