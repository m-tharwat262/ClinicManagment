package com.clinic.management.elnour.activities;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.fragments.countings.CountingFragment;
import com.clinic.management.elnour.fragments.home.HomeFragment;
import com.clinic.management.elnour.fragments.patients.PatientsFragment;
import com.clinic.management.elnour.fragments.employees.EmployeeFragment;
import com.clinic.management.elnour.fragments.settings.SettingsFragment;
import com.clinic.management.elnour.models.UserObject;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class  MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceListener;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private static Toolbar toolbar;

    private static TextView mToolBarMainTitle;
    private static TextView mToolBarNotificationTitle;
    private static ImageView mNotificationIcon;
    private ImageView mNotificationRedDote;

    private LinearLayout mSettingBottomBar;
    private LinearLayout mDoneBottomBar;
    private LinearLayout mHomeBottomBar;
    private LinearLayout mTasksBottomBar;
    private LinearLayout mTablesBottomBar;

    private ImageView mSettingsImage;
    private ImageView mDoneImage;
    private ImageView mHomeImage;
    private ImageView mTasksImage;
    private ImageView mTablesImage;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabaseReference;

    private String mRealName;
    private String mUserName;
    private String mUserEmail;
    private String mUserPhone;


    private String mUserId = "";
    private static final int TAKE_IMAGE_CODE = 4444;

    private boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mContext = MainActivity.this;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        // TODO: remove the below:
        editor.putString(getString(R.string.year_number_key), "2021");
        editor.putString(getString(R.string.month_name_key), Constants.DATABASE_JANUARY);
        editor.apply();


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");



        initializeBottomBarIcons();

        addColorToBottomBarIcons();


        mNotificationIcon = findViewById(R.id.activity_main_notification_icon);
        mNotificationRedDote = findViewById(R.id.activity_main_notification_red_dot);


        toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);



        checkAccountValidation();

        addShadowForBottomLayout();





        setClickingOnSettings();

        setClickingOnPatients();

        setClickingOnHome();

        setClickingOnSalaries();

        setClickingOnCounting();



        setClickingOnNotification();

        HomeFragment homeFragment = new HomeFragment(MainActivity.this);
        openFragment(homeFragment);

    }





    private void setClickingOnSettings() {

        mSettingBottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SettingsFragment settingsFragment = new SettingsFragment(MainActivity.this);
                openFragment(settingsFragment);

                addColorToBottomBarIcons();
                mSettingBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_white_background));
                mSettingsImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorSecondary), PorterDuff.Mode.MULTIPLY);

                mHomeBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_navigation_settings));
                mHomeImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), PorterDuff.Mode.MULTIPLY);

            }
        });


    }

    private void setClickingOnPatients() {

        mDoneBottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PatientsFragment patientsFragment = new PatientsFragment(MainActivity.this);
                openFragment(patientsFragment);

                addColorToBottomBarIcons();
                mDoneBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_white_background));
                mDoneImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorSecondary), PorterDuff.Mode.MULTIPLY);

                mHomeBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_navigation_done));
                mHomeImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), PorterDuff.Mode.MULTIPLY);

            }
        });

    }

    private void setClickingOnHome() {

        mHomeBottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                HomeFragment homeFragment = new HomeFragment(MainActivity.this);
                openFragment(homeFragment);


                addColorToBottomBarIcons();
                mHomeBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_white_background));
                mHomeImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorSecondary), PorterDuff.Mode.MULTIPLY);


            }
        });

    }

    private void setClickingOnSalaries() {

        mTasksBottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EmployeeFragment employeeFragment = new EmployeeFragment(MainActivity.this);
                openFragment(employeeFragment);

                addColorToBottomBarIcons();
                mTasksBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_white_background));
                mTasksImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorSecondary), PorterDuff.Mode.MULTIPLY);

                mHomeBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_navigation_tasks));
                mHomeImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), PorterDuff.Mode.MULTIPLY);

            }
        });


    }

    private void setClickingOnCounting() {

        mTablesBottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CountingFragment countingFragment = new CountingFragment(MainActivity.this);
                openFragment(countingFragment);

                addColorToBottomBarIcons();
                mTablesBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_white_background));
                mTablesImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorSecondary), PorterDuff.Mode.MULTIPLY);

                mHomeBottomBar.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.circle_navigation_tables));
                mHomeImage.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), PorterDuff.Mode.MULTIPLY);

            }
        });

    }



    private void setClickingOnNotification() {

        ImageView imageView = findViewById(R.id.activity_main_notification_icon);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // do something.

            }
        });

    }








    private void checkAccountValidation() {

        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

        mUserId = mFirebaseAuth.getUid();

        mDatabaseReference.child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                UserObject userObject = snapshot.getValue(UserObject.class);

                if (userObject != null) {

                    boolean isDisable = userObject.isDisable();

                    if(isDisable) {


                        Dialog dialog = new Dialog(mContext);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_account_disabled);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setCancelable(false);
                        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


                        TextView contactSupportButton = dialog.findViewById(R.id.dialog_account_disabled_contact_support_button);
                        TextView closeButton = dialog.findViewById(R.id.dialog_account_disabled_close_button);


                        contactSupportButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent intent = new Intent(Intent.ACTION_SENDTO);

                                intent.setData(Uri.parse("mailto:"));

                                String[] toPeople = {"mahmoudtharwat909090@gmail.com"};
                                intent.putExtra(Intent.EXTRA_EMAIL, toPeople);

                                intent.putExtra(Intent.EXTRA_SUBJECT, "TODO App");

                                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.disabled_account_email));

                                intent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmailExternal");


                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivityForResult(Intent.createChooser(intent, "Message to Support"), 800);
                                }

                            }
                        });


                        closeButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mFirebaseAuth.signOut();
                                dialog.dismiss();

                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();


                            }
                        });


                        dialog.show();


                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(mContext, "Can't get the data from the server", Toast.LENGTH_SHORT).show();

            }
        });


    }


    private void initializeBottomBarIcons() {

        mSettingBottomBar = findViewById(R.id.activity_main_bottom_item_settings);
        mDoneBottomBar = findViewById(R.id.activity_main_bottom_item_patients);
        mHomeBottomBar = findViewById(R.id.activity_main_bottom_item_home);
        mTasksBottomBar = findViewById(R.id.activity_main_bottom_item_salaries);
        mTablesBottomBar = findViewById(R.id.activity_main_bottom_item_counting);

        mSettingsImage = findViewById(R.id.activity_main_setting_image);
        mDoneImage = findViewById(R.id.activity_main_done_image);
        mHomeImage = findViewById(R.id.activity_main_home_image);
        mTasksImage = findViewById(R.id.activity_main_tasks_image);
        mTablesImage = findViewById(R.id.activity_main_profile_image);

    }

    private void addColorToBottomBarIcons() {

        mSettingBottomBar.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_navigation_settings));
        mDoneBottomBar.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_navigation_done));
        mHomeBottomBar.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_white_background));
        mTasksBottomBar.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_navigation_tasks));
        mTablesBottomBar.setBackground(ContextCompat.getDrawable(this, R.drawable.circle_navigation_tables));

        mSettingsImage.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.MULTIPLY);
        mDoneImage.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.MULTIPLY);
        mHomeImage.setColorFilter(ContextCompat.getColor(this, R.color.colorSecondary), PorterDuff.Mode.MULTIPLY);
        mTasksImage.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.MULTIPLY);
        mTablesImage.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.MULTIPLY);

    }



    private void addShadowForBottomLayout() {

        FrameLayout frameLayout = findViewById(R.id.main_activity_frame_layout);


        LinearLayout bottomLayout = findViewById(R.id.main_activity_bottom_layout);
        bottomLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                bottomLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = bottomLayout.getHeight();

                frameLayout.setPadding(0, 0, 0, height);

            }

        });

    }

    private void openFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_activity_frame_layout, fragment, null)
                .commit();

    }



}