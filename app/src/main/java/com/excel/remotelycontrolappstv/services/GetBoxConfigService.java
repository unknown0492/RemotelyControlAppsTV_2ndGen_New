package com.excel.remotelycontrolappstv.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.excel.configuration.ConfigurationReader;
import com.excel.configuration.ConfigurationWriter;
import com.excel.customitems.CustomItems;
import com.excel.excelclasslibrary.RetryCounter;
import com.excel.excelclasslibrary.UtilArray;
import com.excel.excelclasslibrary.UtilFile;
import com.excel.excelclasslibrary.UtilMisc;
import com.excel.excelclasslibrary.UtilNetwork;
import com.excel.excelclasslibrary.UtilShell;
import com.excel.excelclasslibrary.UtilURL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import static com.excel.remotelycontrolappstv.util.Constants.APPSTVLAUNCHER_PACKAGE_NAME;
import static com.excel.remotelycontrolappstv.util.Constants.APPSTVLAUNCHER_RECEIVER_NAME;

/**
 * Created by Sohail on 02-11-2016.
 */

public class GetBoxConfigService extends Service {

    Context context;
    final static String TAG = "GetBoxConfig";
    RetryCounter retryCounter;

    String[] languages;
    String[] language_codes;
    HashMap<String,String> language_map;

    ConfigurationReader configurationReader;

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.d( TAG, "GetBoxConfigService started" );
        context = this;
        retryCounter = new RetryCounter( "box_config_retry_count" );

        getBoxConfig();

        return START_STICKY;
    }

    private void getBoxConfig(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String s = UtilNetwork.makeRequestForData( UtilURL.getWebserviceURL(), "POST",
                        UtilURL.getURLParamsFromPairs( new String[][]{
                                { "what_do_you_want", "get_box_configuration" },
                                { "mac_address", UtilNetwork.getMacAddress( context ) }
                        } ));

                if( s == null ){
                    Log.d( TAG, "Failed to retrieve Box Config" );
                    setRetryTimer();
                    return;
                }

                Log.d( TAG, "response : "+s );

                String info = null;
                try{
                    JSONArray jsonArray = new JSONArray( s );
                    JSONObject jsonObject = jsonArray.getJSONObject( 0 );
                    String type = jsonObject.getString( "type" );
                    info = jsonObject.getString( "info" );

                    if( type.equals( "error" ) ){
                        Log.e( TAG, "Failed to retrieve Box Config" );
                        setRetryTimer();
                        return;
                    }

                    retryCounter.reset();

                }
                catch ( Exception e ){
                    e.printStackTrace();
                    setRetryTimer();
                    return;
                }

                // configurationWriter = ConfigurationWriter.getInstance( context );
                File appstv_data = new File( ConfigurationWriter.getAppstvDataDirectorypath() );
                if( ! appstv_data.exists() )
                    appstv_data.mkdirs();

                if( ! ConfigurationWriter.writeAllConfigurations( context, info ) ){
                    //CustomItems.showCustomToast( context, "error", "OTS was not successful. Contact Technical Team !", 5000 );
                    Log.e( TAG, "OTS was not successful. Contact Technical Team !" );
                    return;
                }

                configurationReader = ConfigurationReader.getInstance();
                File configuration = configurationReader.getConfigurationFile( false );
                if( configuration.exists() ) {
                    String data = UtilFile.readData(configuration);
                    data = data.trim();
                    if ( data.length() != 0 ) {
                        // Make a backup copy of configuration file
                        File configuration_backup = new File( configurationReader.getConfigurationFile( false ).getAbsolutePath() + ".backup" );
                        try {
                            configuration_backup.createNewFile();
                            UtilFile.saveDataToFile( configuration_backup, UtilFile.readData( configurationReader.getConfigurationFile( false ) ) );
                        }
                        catch ( Exception e ){
                            e.printStackTrace();
                        }
                    }
                    else{
                        Log.e( TAG, "Backup of configuration file not made because it is empty !" );
                    }
                }



                initLanguages();
                //configurationReader = ConfigurationReader.reInstantiate();

                processDownloadedValues();

                // UtilShell.executeShellCommandWithOp( "am force-stop com.excel.appstvlauncher.secondgen" );
                // UtilShell.executeShellCommandWithOp( "monkey -p com.excel.appstvlauncher.secondgen -c android.intent.category.LAUNCHER 1" );


            }
        }).start();


    }

    public void initLanguages(){
        Locale[] langs = Locale.getAvailableLocales();
        language_codes = new String[ langs.length ];
        for( int i = 0; i < langs.length ; i++ ){
            language_codes[ i ] = langs[ i ].getLanguage();
            //Log.d(TAG, language_codes[i]);
        }
        language_codes = UtilArray.removeDuplicates( language_codes );

        // get Language Names from language codes
        languages = new String[ language_codes.length ];
        for( int i = 0; i < language_codes.length ; i++ ){
            languages[ i ] = new Locale( language_codes[ i ] ).getDisplayLanguage();
            //Log.d(TAG, language_codes[i]);
        }

        language_map = new HashMap<String,String>();
        for( int i = 0; i < language_codes.length ; i++ ){
            language_map.put( languages[ i ], language_codes[ i ] );
            //Log.d(TAG, languages[i] + ","+ language_codes[i]);
        }
        //languages = new String[ language_codes.length ];

    }

    public void setTimeZone( String timeZone ){
        UtilShell.executeShellCommandWithOp( "setprop persist.sys.timezone " + timeZone );
    }

    public void setLocaleLanguage( String language ){
        Log.d( TAG, ":" + language + ":" + language_map.get( language ) + ":" );
        UtilShell.executeShellCommandWithOp( "setprop persist.sys.language " + language_map.get( language ) );
    }

    private void processDownloadedValues(){
        configurationReader = ConfigurationReader.reInstantiate();

        // Set TimeZone
        setTimeZone( configurationReader.getTimezone() );

        // Set Language
        //setLocaleLanguage( configurationReader.getLanguage() );

        // Send Broadcast to Enable/Disable Hotspot
        //..
        //..
        //..
        Intent in = new Intent( this, HotspotStarterService.class );
        startService( in );

        // Refresh the Launcher Config so that SSID and Password is visible there
        // sendBroadcast( new Intent( "receive_update_launcher_config" ) );
        UtilMisc.sendExplicitExternalBroadcast( context, "receive_update_launcher_config", APPSTVLAUNCHER_PACKAGE_NAME, APPSTVLAUNCHER_RECEIVER_NAME );
        //sendBroadcast( new Intent( "receive_update_hotspot_info" ) );


        // Send Broadcast to Enable/Disable Airplay (sent inside the HotspotStarterService.class)
        //.. Because, if Airplay is enabled, it should only be started after the Hotspot is enabled
        //..
        //..


    }

    private void setRetryTimer(){
        final long time = retryCounter.getRetryTime();

        Log.d( TAG, "time : " + time );

        new Handler( Looper.getMainLooper() ).postDelayed(new Runnable() {

            @Override
            public void run() {
                Log.d( TAG, "downloading box config after "+(time/1000)+" seconds !" );
                /*GetLauncherConfig glc = new GetLauncherConfig();
                glc.execute( "" );*/
                getBoxConfig();

            }

        }, time );

    }

}
