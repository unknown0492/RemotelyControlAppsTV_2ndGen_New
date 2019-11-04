package com.excel.remotelycontrolappstv.secondgen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.excel.excelclasslibrary.UtilShell;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
			
			/*if( _for.equals( "manipulate_hotspot_info" ) ){
				jsonObject = jsonArray.getJSONObject( 0 );
				String ssid 	= jsonObject.getString( "ssid" );
				String password = jsonObject.getString( "password" );
				String enabled	= jsonObject.getString( "enabled" );
				String airplay_enabled = jsonObject.getString( "airplay_enabled" );
				
				// This is for Airplay enable/disable info saving
				Functions.saveFile( "OTS", "airplay_enabled", "txt", airplay_enabled.trim().getBytes() );
				
				if( enabled.equals( "1" ) ){
					// Store the Credentials of the Hotspot on the box, then trigger the hotspot ON
					String csv_data = String.format( "%s,%s", ssid, password );
					Functions.saveFile( "OTS", "hotspot", "txt", csv_data.getBytes() );
					
					context.sendBroadcast( new Intent( "turn_on_hotspot" ) );
					Log.i( TAG, "Broadcast Sent" );
				}
				else if( enabled.equals( "0" ) ){
					// Store the Credentials of the Hotspot on the box, then trigger the hotspot OFF
					String csv_data = String.format( "%s,%s", ssid, password );
					Functions.saveFile( "OTS", "hotspot", "txt", csv_data.getBytes() );
					
					context.sendBroadcast( new Intent( "turn_off_hotspot" ) );
					Log.i( TAG, "Broadcast Sent" );
				}
			}
			else */
			if( _for.equals( "start_restore_service" ) ){
				context.sendBroadcast( new Intent( "start_restore_service" ) );
			}
			else if( _for.equals( "push_launcher_menu_items" ) ){
				context.sendBroadcast( new Intent( "get_launcher_config" ) );
			}
			else if( _for.equals( "push_digital_signage" ) ){
				context.sendBroadcast( new Intent( "get_wallpapers" ) );
			}
			else if( _for.equals( "push_preinstall_apps" ) ){
				context.sendBroadcast( new Intent( "get_preinstall_apps_info" ) );
			}
			else if( _for.equals( "push_ticker_text" ) ){
				context.sendBroadcast( new Intent( "get_launcher_config" ) );
			}
			else if( _for.equals( "push_hotel_logo" ) ){
				context.sendBroadcast( new Intent( "get_hotel_logo" ) );
			}
			else if( _for.equals( "push_dvb_tv_channels" ) ){
				context.sendBroadcast( new Intent( "get_tv_channels_file" ) );
			}
			else if( _for.equals( "push_appstv_common_settings" ) ){
				context.sendBroadcast( new Intent( "get_box_configuration" ) );
			}
			else if( _for.equals( "push_hotspot" ) ){
				context.sendBroadcast( new Intent( "get_box_configuration" ) );
			}
			else if( _for.equals( "push_ota" ) ){
				context.sendBroadcast( new Intent( "get_ota_info" ) );
			}
			else if( _for.equals( "clear_cache" ) ){
				context.sendBroadcast( new Intent( "clear_application_cache" ) );
			}
			else if( _for.equals( "reboot" ) ){
				UtilShell.executeShellCommandWithOp( "reboot" );
			}
			else if( _for.equals( "reboot_recovery" ) ){
				UtilShell.executeShellCommandWithOp( "reboot recovery" );
			}
			else if( _for.equals( "update_box_active_status" ) ){
				context.sendBroadcast( new Intent( "update_box_active_status" ) );
			}
			else if( _for.equals( "execute_script" ) ){
				Intent inn = new Intent( "execute_script" );
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
				context.sendBroadcast( inn );
			}
			else if( _for.equals( "show_welcome_screen" ) ){
				UtilShell.executeShellCommandWithOp( "monkey -p com.excel.welcomeguestapp.secondgen -c android.intent.category.LAUNCHER 1" );
			}
			else if( _for.equals( "release_cc" ) ){
				context.sendBroadcast( new Intent( "release_cc" ) );
			}

			Log.i( TAG, "processMessage() completed" );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}
}
