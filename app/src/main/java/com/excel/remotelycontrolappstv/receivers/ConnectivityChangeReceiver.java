package com.excel.remotelycontrolappstv.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

    final static String TAG = "ConnectivityChange";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d( TAG, "action : " + action );
    }
}
