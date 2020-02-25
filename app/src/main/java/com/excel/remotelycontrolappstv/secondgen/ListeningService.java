package com.excel.remotelycontrolappstv.secondgen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.excel.excelclasslibrary.UtilMisc;
import com.excel.excelclasslibrary.UtilShell;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import static com.excel.remotelycontrolappstv.util.Constants.CHROMECAST_STREAMING_PACKAGE_NAME;
import static com.excel.remotelycontrolappstv.util.Constants.CHROMECAST_STREAMING_RECEIVER_NAME;
import static com.excel.remotelycontrolappstv.util.Constants.DATADOWNLOADER_PACKAGE_NAME;
import static com.excel.remotelycontrolappstv.util.Constants.DATADOWNLOADER_RECEIVER_NAME;

public class ListeningService extends Service {
	
	Context context = null;
	final static String TAG = "ListeningService";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand( Intent intent, int flags, int startId ) {
		Log.i( TAG, "Listening Service Started !" );
		
		context = this;
		
		NetworkTask nt = new NetworkTask();
		nt.execute();
		
		return START_STICKY;
	}
	
	class NetworkTask extends AsyncTask<Void, Void, Void>{
		
		
		@Override
		protected Void doInBackground( Void... params ) {
			
			DatagramPacket p = null;
			DatagramSocket s = null;
			
			try{
				while( true ){
					String text;
				    int server_port = 12345;
				    byte[] message = new byte[1500];
				    p = new DatagramPacket( message, message.length );
				    s = new DatagramSocket( server_port );
				    s.receive( p );
					s.send( p );
				    text = new String( message, 0, p.getLength() );
				    Log.d( TAG,"message:" + text );

					// Forward this text to the processing function
				    processMessage( text );
				    
				    s.close();
				} 
			}
			catch( Exception e ){
				e.printStackTrace();
			}
			finally{
				s.close();
			}
			return null;
		}
	}

	public void processMessage( String message ){
		JSONArray jsonArray = null;
		JSONObject jsonObject = null;
		try{
			jsonArray = new JSONArray( message );
			jsonObject = jsonArray.getJSONObject( 0 );
			
			String _for = jsonObject.getString( "for" );
			Log.i( TAG, "_for : "+_for );
			

			if( _for.equals( "start_restore_service" ) ){
				// context.sendBroadcast( new Intent( "start_restore_service" ) );		// Defined inside DataDownloader
				UtilMisc.sendExplicitExternalBroadcast( context, "start_restore_service", DATADOWNLOADER_PACKAGE_NAME, DATADOWNLOADER_RECEIVER_NAME  );
			}
			else if( _for.equals( "push_launcher_menu_items" ) ){
				// context.sendBroadcast( new Intent( "get_launcher_config" ) );
				UtilMisc.sendExplicitInternalBroadcast( context, "get_launcher_config", Receiver.class );
			}
			else if( _for.equals( "push_digital_signage" ) ){								// Defined inside DataDownloader
				// context.sendBroadcast( new Intent( "get_wallpapers" ) );
				UtilMisc.sendExplicitExternalBroadcast( context, "get_wallpapers", DATADOWNLOADER_PACKAGE_NAME, DATADOWNLOADER_RECEIVER_NAME  );
			}
			else if( _for.equals( "push_preinstall_apps" ) ){
				// context.sendBroadcast( new Intent( "get_preinstall_apps_info" ) );
				UtilMisc.sendExplicitInternalBroadcast( context, "get_preinstall_apps_info", Receiver.class );
			}
			else if( _for.equals( "push_ticker_text" ) ){
				// context.sendBroadcast( new Intent( "get_launcher_config" ) );
				UtilMisc.sendExplicitInternalBroadcast( context, "get_launcher_config", Receiver.class );
			}
			else if( _for.equals( "push_hotel_logo" ) ){									// Defined inside DataDownloader
				// context.sendBroadcast( new Intent( "get_hotel_logo" ) );
				UtilMisc.sendExplicitExternalBroadcast( context, "get_hotel_logo", DATADOWNLOADER_PACKAGE_NAME, DATADOWNLOADER_RECEIVER_NAME  );
			}
			else if( _for.equals( "push_dvb_tv_channels" ) ){								// Defined inside DataDownloader
				// context.sendBroadcast( new Intent( "get_tv_channels_file" ) );
				UtilMisc.sendExplicitExternalBroadcast( context, "get_tv_channels_file", DATADOWNLOADER_PACKAGE_NAME, DATADOWNLOADER_RECEIVER_NAME  );
			}
			else if( _for.equals( "push_appstv_common_settings" ) ){
				//context.sendBroadcast( new Intent( "get_box_configuration" ) );
				UtilMisc.sendExplicitInternalBroadcast( context, "get_box_configuration", Receiver.class );
			}
			else if( _for.equals( "push_hotspot" ) ){
				// context.sendBroadcast( new Intent( "get_box_configuration" ) );
				UtilMisc.sendExplicitInternalBroadcast( context, "get_box_configuration", Receiver.class );
			}
			else if( _for.equals( "push_ota" ) ){											// Defined inside DataDownloader
				// context.sendBroadcast( new Intent( "get_ota_info" ) );
				UtilMisc.sendExplicitExternalBroadcast( context, "get_ota_info", DATADOWNLOADER_PACKAGE_NAME, DATADOWNLOADER_RECEIVER_NAME  );
			}
			else if( _for.equals( "clear_cache" ) ){
				// context.sendBroadcast( new Intent( "clear_application_cache" ) );
				UtilMisc.sendExplicitInternalBroadcast( context, "clear_application_cache", Receiver.class );
			}
			else if( _for.equals( "reboot" ) ){
				UtilShell.executeShellCommandWithOp( "reboot" );
			}
			else if( _for.equals( "reboot_recovery" ) ){
				UtilShell.executeShellCommandWithOp( "reboot recovery" );
			}
			else if( _for.equals( "update_box_active_status" ) ){
				// context.sendBroadcast( new Intent( "update_box_active_status" ) );
				UtilMisc.sendExplicitInternalBroadcast( context, "update_box_active_status", Receiver.class );
			}
			else if( _for.equals( "execute_script" ) ){
				Intent inn = new Intent();
				String scripts[] = null;
				try{
					JSONArray jsa = jsonObject.getJSONArray( "info" );
					scripts = new String[ jsa.length() ];
					for( int i = 0 ; i < jsa.length() ; i++ ){
						scripts[ i ] = jsa.getString( i );
					}
				}
				catch ( Exception e ){
					e.printStackTrace();
				}
				inn.putExtra( "scripts", scripts );
				UtilMisc.sendExplicitInternalBroadcast( context, inn, "execute_script", Receiver.class );
			}
			else if( _for.equals( "show_welcome_screen" ) ){
				UtilShell.executeShellCommandWithOp( "monkey -p com.excel.welcomeguestapp.secondgen -c android.intent.category.LAUNCHER 1" );
			}
			else if( _for.equals( "release_cc" ) ){
				// context.sendBroadcast( new Intent( "release_cc" ) );					// Defined inside ChromecastStreaming
				UtilMisc.sendExplicitExternalBroadcast( context, "release_cc", CHROMECAST_STREAMING_PACKAGE_NAME, CHROMECAST_STREAMING_RECEIVER_NAME  );
			}

			Log.i( TAG, "processMessage() completed" );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}
}
