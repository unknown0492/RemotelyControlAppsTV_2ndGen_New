package com.excel.remotelycontrolappstv.secondgen;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

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
				    text = new String( message, 0, p.getLength() );
				    Log.d( TAG,"message:" + text );

					// Forward this text to the processing function
				    // processMessage( text );
				    
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
	/*
	public void processMessage( String message ){
		JSONArray jsonArray = null;
		JSONObject jsonObject = null;
		try{
			jsonArray = new JSONArray( message );
			jsonObject = jsonArray.getJSONObject( 0 );
			
			String _for = (String) jsonObject.get( "for" );
			jsonArray = new JSONArray( (String) jsonObject.get( "info" ) );
			Log.i( TAG, "_for : "+_for );
			
			if( _for.equals( "manipulate_hotspot_info" ) ){
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
			else if( _for.equals( "start_restore_service" ) ){
				context.sendBroadcast( new Intent( "start_restore_service" ) );
			}
			else if( _for.equals( "download_launcher_config" ) ){
				context.sendBroadcast( new Intent( "download_launcher_config" ) );
			}
			else if( _for.equals( "start_downloading_wallpapers" ) ){
				context.sendBroadcast( new Intent( "start_downloading_wallpapers" ) );
			}
			else if( _for.equals( "welcome_page_shown" ) ){
				context.sendBroadcast( new Intent( "welcome_page_shown" ) );
			}
			else if( _for.equals( "welcome_page_not_shown" ) ){
				context.sendBroadcast( new Intent( "welcome_page_not_shown" ) );
			}
			else if( _for.equals( "show_welcome_page" ) ){
				context.sendBroadcast( new Intent( "show_welcome_page" ) );
			}
			else if( _for.equals( "start_ota" ) ){
				context.sendBroadcast( new Intent( "start_ota" ) );
			}
			else if( _for.equals( "turn_on_clearappdataservice" ) ){
				context.sendBroadcast( new Intent( "turn_on_clearappdataservice" ) );
			}
			else if( _for.equals( "turn_on_repairdtvservice" ) ){
				context.sendBroadcast( new Intent( "turn_on_repairdtvservice" ) );
			}
			else if( _for.equals( "reboot_box" ) ){
				context.sendBroadcast( new Intent( "reboot_box" ) );
			}
			else if( _for.equals( "reboot_to_recovery" ) ){
				context.sendBroadcast( new Intent( "reboot_to_recovery" ) );
			}
			else if( _for.equals( "execute_script" ) ){
				context.sendBroadcast( new Intent( "execute_script" ) );
				// Execute the shell scripts, sent in "info"
				*//*[	Array (0)
					{ "type" : "success" , 				Object("type")
					  "for" : "execute_script" , 		Object("for")
					  "info" : "[						Object("info")			Array(0)
									[ \"ls -l\", \" ps\", \" chmod 777 abc.zip\" ]		Array(0), Array(1), Array(2)
								]"
					}
				]*//*
				jsonArray = jsonArray.getJSONArray( 0 );
				String[] commands = new String[ jsonArray.length() ];
				for( int i = 0 ; i < jsonArray.length() ; i++ ){
					commands[ i ] = jsonArray.getString( i );
					*//*commands += jsonArray( i );
					if( i != jsonArray.length() - 1 )
						commands += ",";*//*
				}
				Functions.executeShellCommandWithOp( commands );

			}
			else if( _for.equals( "capture_screenshot" ) ){
				context.sendBroadcast( new Intent( "capture_screenshot" ) );
			}
			else if( _for.equals( "update_ip" ) ){
				context.sendBroadcast( new Intent( "update_ip" ) );
			}
			else if( _for.equals( "update_cms_ip" ) ){
				context.sendBroadcast( new Intent( "update_cms_ip" ) );
			}
			else if( _for.equals( "update_room_no" ) ){
				context.sendBroadcast( new Intent( "update_room_no" ) );
			}
			else if( _for.equals( "start_background_ota" ) ){
				context.sendBroadcast( new Intent( "start_background_ota" ) );
			}
			else if( _for.equals( "start_download_ota" ) ){
				context.sendBroadcast( new Intent( "start_download_ota" ) );
			}
			else if( _for.equals( "start_quiet_ota" ) ){
				context.sendBroadcast( new Intent( "start_quiet_ota" ) );
			}
			
			
			Log.i( TAG, "processMessage() completed" );
		}
		catch( Exception e ){
			e.printStackTrace();
		}
	}*/
}
