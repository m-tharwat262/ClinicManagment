package com.clinic.management.elnour.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.clinic.management.elnour.R;


public class SplashActivity extends AppCompatActivity {

    private Context mContext;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        mContext = SplashActivity.this;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();



        LinearLayout threeLinesLinearLayout = findViewById(R.id.activity_splash_three_lines);
        TextView appNameTextView = findViewById(R.id.activity_splash_app_description_name);

        Animation animationBottom = AnimationUtils.loadAnimation(this, R.anim.animation_bottom);
        threeLinesLinearLayout.setAnimation(animationBottom);
//        appNameTextView.setAnimation(animationBottom);

        int SPLASH_SCREEN = 200;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_SCREEN);

    }


}








