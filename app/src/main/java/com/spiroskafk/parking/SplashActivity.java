package com.spiroskafk.parking;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity
{
    protected int _splashTime = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int secondsDelayed = 1;
        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                startActivity(new Intent(SplashActivity.this,
                        NavActivity.class));
                finish();
            }
        }, secondsDelayed * 1000);
    }
}
