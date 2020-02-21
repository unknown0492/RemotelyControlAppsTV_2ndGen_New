package com.excel.remotelycontrolappstv.secondgen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = "MyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        //String data = intent.getExtras().getString("data");
        Log.i( TAG, "Action : "+intent.getAction() );
        Toast.makeText(context, "Action : "+intent.getAction() , Toast.LENGTH_LONG).show();
    }
}
