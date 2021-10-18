package com.clinic.management.elnour.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clinic.management.elnour.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mContext = MainActivity.this;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();


        mFirebaseAuth = FirebaseAuth.getInstance();

        TextView signOutButton = findViewById(R.id.activity_main_sign_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editor.putString(getString(R.string.user_id_Key), getString(R.string.preference_string_empty_value));
                editor.putString(getString(R.string.real_name_Key), getString(R.string.preference_string_empty_value));
                editor.putString(getString(R.string.user_name_Key), getString(R.string.preference_string_empty_value));
                editor.putString(getString(R.string.email_address_Key), getString(R.string.preference_string_empty_value));
                editor.putString(getString(R.string.phone_number_Key), getString(R.string.preference_string_empty_value));
                editor.putString(getString(R.string.user_join_unix_time_key), getString(R.string.preference_string_empty_value));
                editor.putBoolean(getString(R.string.verified_number_Key), false);
                editor.putBoolean(getString(R.string.account_disable_Key), false);
                editor.putBoolean(getString(R.string.account_admin_Key), false);
                editor.apply();

                mFirebaseAuth.signOut();
                Toast.makeText(MainActivity.this, getString(R.string.sign_out_successfully), Toast.LENGTH_SHORT).show();
                checkUserAlreadyLoggedIn();

            }
        });

    }


    private void checkUserAlreadyLoggedIn() {

        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if(firebaseUser == null) {

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }

    }

}