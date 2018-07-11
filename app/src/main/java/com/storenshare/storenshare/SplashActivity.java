package com.storenshare.storenshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /*
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
*/

        final ProgressBar splashProgreeBar=(ProgressBar)findViewById(R.id.splashProgressBar);
        splashProgreeBar.getProgressDrawable().setColorFilter(
                Color.argb(255,56,159,120), android.graphics.PorterDuff.Mode.SRC_IN);
        splashProgreeBar.setMax(100);

        new Thread(new Runnable() {

            public void run() {
                int i = 0;
                while (i < 100) {
                    SystemClock.sleep(25);
                    i++;
                    final int curCount = i;
                    if (curCount % 5 == 0) {
                        //update UI with progress every 5%
                        splashProgreeBar.post(new Runnable() {
                            public void run() {
                                splashProgreeBar.setProgress(curCount);
                            }
                        });
                    }
                }
                splashProgreeBar.post(new Runnable() {
                    public void run() {
                        startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                        finish();
                    }
                });
            }
        }).start();
    }
}
