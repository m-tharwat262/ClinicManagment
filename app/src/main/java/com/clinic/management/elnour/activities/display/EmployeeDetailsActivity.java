package com.clinic.management.elnour.activities.display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.fragments.employees.EmployeeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

public class EmployeeDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = EmployeeDetailsActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private TextView mNameTextView;
    private TextView mJobTextView;
    private TextView mPhoneNumberTextView;
    private TextView mSessionCostTextView;
    private TextView mAllSessionNumberTextView;
    private TextView mAddedTimeTextView;
    private TextView mBalanceTextView;

    private TextView mCurrentMonthSessionsNumberTextView;
    private TextView mCurrentMonthChangeCostTextView;
    private TextView mCurrentMonthChangeDateTextView;
    private TextView mCurrentMonthOldCostTextView;
    private TextView mCurrentMonthAfterChangeTextView;
    private TextView mCurrentMonthBeforeChangeTextView;

    private DatabaseReference mEmployeeDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;

    private String mEmployeeName;
    private String mEmployeeJob;
    private String mPhoneNumber;
    private double mBalance;
    private double mSessionCost;
    private double mAllSessionNumber;
    private long mAddedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_details);


        mContext = EmployeeDetailsActivity.this;

        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));


        mNameTextView = findViewById(R.id.activity_employee_basic_info_name);
        mJobTextView = findViewById(R.id.activity_employee_basic_info_job);
        mPhoneNumberTextView = findViewById(R.id.activity_employee_basic_info_phone_number);
        mSessionCostTextView = findViewById(R.id.activity_employee_basic_info_session_cost);
        mAllSessionNumberTextView = findViewById(R.id.activity_employee_basic_info_all_session_number);
        mAddedTimeTextView = findViewById(R.id.activity_employee_basic_info_added_time);
        mBalanceTextView = findViewById(R.id.activity_employee_basic_info_balance);


        mCurrentMonthSessionsNumberTextView = findViewById(R.id.activity_employee_sessions_number);
        mCurrentMonthChangeCostTextView = findViewById(R.id.activity_employee_cost_change);
        mCurrentMonthChangeDateTextView = findViewById(R.id.activity_employee_change_date);
        mCurrentMonthOldCostTextView = findViewById(R.id.activity_employee_old_cost);
        mCurrentMonthBeforeChangeTextView = findViewById(R.id.activity_employee_before_change);
        mCurrentMonthAfterChangeTextView = findViewById(R.id.activity_employee_after_change);


        String employeeId = getIntent().getStringExtra("employee_id");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEmployeeDatabaseReference = mFirebaseDatabase.getReference().child(Constants.EMPLOYEES_NODE).child(employeeId);

        displayBasicData();

        displayCurrentMonth(mYearNumber, mMonthName);

    }

    private void displayBasicData() {


        mEmployeeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    if (dataSnapshot.child(Constants.EMPLOYEE_NAME).getValue(String.class) != null) {
                        mEmployeeName = dataSnapshot.child(Constants.EMPLOYEE_NAME).getValue(String.class);
                        mNameTextView.setText("Name : " + mEmployeeName);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_JOB).getValue(String.class) != null) {
                        mEmployeeJob = dataSnapshot.child(Constants.EMPLOYEE_JOB).getValue(String.class);
                        mJobTextView.setText("Job : " + mEmployeeJob);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_PHONE_NUMBER).getValue(String.class) != null) {
                        mPhoneNumber = dataSnapshot.child(Constants.EMPLOYEE_PHONE_NUMBER).getValue(String.class);
                        mPhoneNumberTextView.setText("Phone Number : " + mPhoneNumber);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_SESSION_COST).getValue(Double.class) != null) {
                        mSessionCost = dataSnapshot.child(Constants.EMPLOYEE_SESSION_COST).getValue(Double.class);
                        mSessionCostTextView.setText("Session Cost : " + mSessionCost);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_ALL_SESSION_NUMBER).getValue(Integer.class) != null) {
                        mAllSessionNumber = dataSnapshot.child(Constants.EMPLOYEE_ALL_SESSION_NUMBER).getValue(Integer.class);
                        mAllSessionNumberTextView.setText("All sessions number : " + mAllSessionNumber);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_ADDED_TIME).getValue(Long.class) != null) {
                        mAddedTime = dataSnapshot.child(Constants.EMPLOYEE_ADDED_TIME).getValue(Long.class);
                        mAddedTimeTextView.setText("Added time : " + mAddedTime);
                    }

                    if (dataSnapshot.child(Constants.EMPLOYEE_BALANCE).getValue(Double.class) != null) {
                        mBalance = dataSnapshot.child(Constants.EMPLOYEE_BALANCE).getValue(Double.class);
                        mBalanceTextView.setText("Balance : " + mBalance);
                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "onCancelled", databaseError.toException());
            }
        });

    }


    private void displayCurrentMonth(String yearNumber, String monthNumber) {

        mEmployeeDatabaseReference.child(Constants.EMPLOYEE_SESSIONS_DETAILS)
                .child(Constants.DATABASE_YEARS).child(yearNumber).child(monthNumber)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    double sessionsNumber = 0.0;
                    boolean isCostChange = false;
                    long changeDate = -1;
                    double oldCost = 0.0;
                    double beforeChange = 0.0;
                    double afterChange = 0.0;


                    if(dataSnapshot.child(Constants.EMPLOYEE_SESSIONS_NUMBER).getValue(Double.class) != null) {
                        sessionsNumber = dataSnapshot.child(Constants.EMPLOYEE_SESSIONS_NUMBER).getValue(Double.class);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_SESSION_COST_CHANGE).getValue(Boolean.class) != null){
                        isCostChange = dataSnapshot.child(Constants.EMPLOYEE_SESSION_COST_CHANGE).getValue(Boolean.class);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_CHANGE_DATE).getValue(Long.class) != null){
                        changeDate = dataSnapshot.child(Constants.EMPLOYEE_CHANGE_DATE).getValue(Long.class);
                    }
                    if(dataSnapshot.child(Constants.EMPLOYEE_OLD_COST).getValue(Double.class) != null) {
                        oldCost = dataSnapshot.child(Constants.EMPLOYEE_OLD_COST).getValue(Double.class);
                    }
                    if(dataSnapshot.child(Constants.EMPLOYEE_BEFORE_CHANGE).getValue(Double.class) != null) {
                        beforeChange = dataSnapshot.child(Constants.EMPLOYEE_BEFORE_CHANGE).getValue(Double.class);
                    }
                    if (dataSnapshot.child(Constants.EMPLOYEE_AFTER_CHANGE).getValue(Double.class) != null){
                        afterChange = dataSnapshot.child(Constants.EMPLOYEE_AFTER_CHANGE).getValue(Double.class);
                    }

                    mCurrentMonthSessionsNumberTextView.setText("Sessions number : " + sessionsNumber);
                    mCurrentMonthChangeCostTextView.setText("Is cost change : " + isCostChange);
                    mCurrentMonthChangeDateTextView.setText("Change date : " + changeDate);
                    mCurrentMonthOldCostTextView.setText("Old cost : " + oldCost);
                    mCurrentMonthBeforeChangeTextView.setText("Before change : " + beforeChange);
                    mCurrentMonthAfterChangeTextView.setText("After change : " + afterChange);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "onCancelled", databaseError.toException());
            }
        });

    }

}