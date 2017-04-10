package com.coolweather.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.coolweather.android.R;

public class ChuiZhiActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChuiZhiActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chui_zhi);
        Button button= (Button) findViewById(R.id.bt);
        button.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        String name=null;
        Log.e(TAG, "onCreate: "+name.toString() );
    }
}
