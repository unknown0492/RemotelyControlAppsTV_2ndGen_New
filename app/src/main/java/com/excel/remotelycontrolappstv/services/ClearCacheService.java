package com.excel.remotelycontrolappstv.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.excel.configuration.PreinstallApps;
import com.excel.excelclasslibrary.UtilShell;
import com.excel.remotelycontrolappstv.secondgen.R;

import java.io.File;

public class ClearCacheService extends Service {

    final static String TAG = "ClearCacheService";
    Context context = this;

    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        Log.d( TAG, "ClearCacheService started" );

        // Clear the DownloadManager data
        // Removed because, once this database is deleted, DownloadManager will throw Nullpointerexception
        //UtilShell.executeShellCommandWithOp( "rm /data/data/com.android.providers.downloads/databases/downloads.db" );
        deleteDownloadManagerData();

        deleteApplicationData( context );

        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(this, "test" );
        notificationBuilder.setSmallIcon( R.drawable.ic_launcher );
        notificationManager.notify(0, notificationBuilder.build());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( "test",TAG, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel( channel );

            Notification notification = new Notification.Builder(getApplicationContext(),"test").build();
            startForeground(1, notification);
        }
        else {
            // startForeground(1, notification);
        }
    }

    public void deleteApplicationData( Context context ){
        //get a list of installed apps, from build.prop
        PreinstallApps[] paps = PreinstallApps.getPreinstallApps();
        //String[] apps = ( loadValidPackages().trim() ).split( "," );
        for( int i = 0 ; i < paps.length; i++ ){
            //if( apps[ i+2 ].equals( "1" )  ){
            /*Log.d(TAG, "package : " + paps[ i ].getPackageName() );
            Log.d(TAG, "MD5 : " + paps[ i ].getMD5() );
            Log.d(TAG, "Button ID : " + paps[ i ].getButtonID() );
            Log.d(TAG, "Show : " + paps[ i ].getShow() );
            Log.d(TAG, "Wipe Cache : " + paps[ i ].getWipeCache() );
            Log.d(TAG, "Force kill : " + paps[ i ].getForceKill() );*/

            if( paps[ i ].getWipeCache().equals( "clear" ) ) {
                Log.d(TAG, "Delete data for : " + paps[i].getPackageName());
                deleteIt(paps[i].getPackageName(), (paps[i].getForceKill().equals("force_kill")) ? true : false);

                // Validate if the process deleted the entire package, then recreate empty package
                validatePackageExistence(paps[i].getPackageName());
                continue;
            }
            Log.d(TAG, "Skipped : " + paps[i].getPackageName());
            /*}
            else{
                Log.d( TAG, "Do Not Delete data for : "+apps[ i ] );
            }*/
        }
    }

    // Execute the shell command to delete all the folders/directories inside the supplied package name
    public void deleteIt( String package_name, boolean kill ){
        String path = "/data/data/" + package_name;
        UtilShell.executeShellCommandWithOp( "chmod -R 777 /data/data/"+package_name );
        if( kill ){
            String pid = UtilShell.executeShellCommandWithOp( "pidof "+package_name ).trim();
            UtilShell.executeShellCommandWithOp( "kill " + pid );
            Log.d( TAG, "Killed pid : "+pid+" : "+package_name );
        }

        UtilShell.executeShellCommandWithOp( "rm -r /data/data/"+package_name+"/*" );

        if( kill ){
            String pid = UtilShell.executeShellCommandWithOp( "pidof "+package_name ).trim();
            UtilShell.executeShellCommandWithOp( "kill " + pid );
            Log.d( TAG, "Killed once again, pid : "+pid+" : "+package_name );
        }
    	/*String dirList = Functions.executeShellCommandWithOp( "ls " + path );
    	String[] children = dirList.split( "\n" );
    	for ( String s : children ) {
        	if ( !s.trim().equals( "lib" ) || !s.trim().equals( "" ) ) {
        		Functions.executeShellCommandWithOp( "rm -r " + path + File.separator + s );
        		Log.i( "TAG", "**************** File /data/data/"+ package_name +"/" + s + " DELETED *******************" );
            }
        }*/
    }

    // Execute the shell command to delete only CACHE directory inside the supplied package name
    public void deleteCache( String package_name ){
        String path = "/data/data/" + package_name;
        String dirList = UtilShell.executeShellCommandWithOp( "ls " + path );
        String[] children = dirList.split( "\n" );
        for ( String s : children ) {
            if ( s.trim().equals( "cache" ) ) {
                UtilShell.executeShellCommandWithOp( "rm -r " + path + File.separator + s );
                Log.i( "TAG", "**************** File /data/data/"+ package_name +"/" + s + " DELETED *******************" );
            }
        }
    }

    // if the delete process deleted entire package, then re create the empty package again
    public void validatePackageExistence( String package_name ){
        if( !( new File( "/data/data/" + package_name ).exists() ) ){ // if package not exist
            // create empty package
            UtilShell.executeShellCommandWithOp( "mkdir " + "/data/data/" + package_name );
        }
    }

    public void deleteDownloadManagerData(){
        UtilShell.executeShellCommandWithOp( "chmod -R 777 /data/data/com.android.providers.downloads/databases/downloads.db" );

        String size_in_kb = UtilShell.executeShellCommandWithOp( "ls -s /data/data/com.android.providers.downloads/databases/downloads.db" );
        size_in_kb = size_in_kb.substring( 0, size_in_kb.indexOf( " " ) );
        size_in_kb = size_in_kb.trim();

        // Delete the downloads.db file if its size is more than 10MB that is 1024*10=10240KB
        int size = Integer.parseInt( size_in_kb );
        Log.d( TAG, "Size of DownloadManager data downloads.db file : "+size );
        if( size >= 10240 ){
            // Delete the file and reboot
            UtilShell.executeShellCommandWithOp( "rm /data/data/com.android.providers.downloads/databases/downloads.db" );
            UtilShell.executeShellCommandWithOp( "reboot" );
        }

    }
}
