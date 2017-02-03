package com.excel.remotelycontrolappstv.secondgen;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    static final String TAG = "RemotelyControlAppsTv";

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        finish();
    }
}
