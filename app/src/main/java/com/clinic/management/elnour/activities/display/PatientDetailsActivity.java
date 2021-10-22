package com.clinic.management.elnour.activities.display;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.fragments.employees.EmployeeFragment;
import com.clinic.management.elnour.models.BalanceObject;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = PatientDetailsActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private TextView mNameTextView;
    private TextView mAgeTextView;
    private TextView mDiseaseTextView;
    private TextView mPhoneNumberTextView;
    private TextView mSessionCostTextView;
    private TextView mAllSessionNumberTextView;
    private TextView mAddedTimeTextView;
    private TextView mAddedByTextView;
    private TextView mBalanceTextView;
    private TextView mDeptTextView;

    private TextView mCurrentMonthSessionsNumberTextView;
    private TextView mCurrentMonthChangeCostTextView;
    private TextView mCurrentMonthChangeDateTextView;
    private TextView mCurrentMonthOldCostTextView;
    private TextView mCurrentMonthAfterChangeTextView;
    private TextView mCurrentMonthBeforeChangeTextView;

    private EditText mBalanceEditText;
    private TextView mAddBalanceButton;

    private DatabaseReference mPatientDatabaseReference;
    private DatabaseReference mIncomeDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;

    private String mPatientName;
    private double mPatientAge;
    private String mDisease;
    private String mPhoneNumber;
    private double mBalance;
    private double mDept;
    private double mSessionCost;
    private double mAllSessionNumber;
    private long mAddedTime;
    private String mAddedBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        mContext = PatientDetailsActivity.this;


        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));


        mNameTextView = findViewById(R.id.activity_patient_basic_info_name);
        mAgeTextView = findViewById(R.id.activity_patient_basic_info_age);
        mDiseaseTextView = findViewById(R.id.activity_patient_basic_info_disease);
        mPhoneNumberTextView = findViewById(R.id.activity_patient_basic_info_phone_number);
        mSessionCostTextView = findViewById(R.id.activity_patient_basic_info_session_cost);
        mAllSessionNumberTextView = findViewById(R.id.activity_patient_basic_info_all_session_number);
        mAddedTimeTextView = findViewById(R.id.activity_patient_basic_info_added_time);
        mAddedByTextView = findViewById(R.id.activity_patient_basic_info_added_by);
        mBalanceTextView = findViewById(R.id.activity_patient_basic_info_balance);
        mDeptTextView = findViewById(R.id.activity_patient_basic_info_dept);


        mCurrentMonthSessionsNumberTextView = findViewById(R.id.activity_patient_sessions_number);
        mCurrentMonthChangeCostTextView = findViewById(R.id.activity_patient_cost_change);
        mCurrentMonthChangeDateTextView = findViewById(R.id.activity_patient_change_date);
        mCurrentMonthOldCostTextView = findViewById(R.id.activity_patient_old_cost);
        mCurrentMonthBeforeChangeTextView = findViewById(R.id.activity_patient_before_change);
        mCurrentMonthAfterChangeTextView = findViewById(R.id.activity_patient_after_change);

        mBalanceEditText = findViewById(R.id.activity_patient_balance);
        mAddBalanceButton = findViewById(R.id.activity_patient_add_button);


        String employeeId = getIntent().getStringExtra("patient_id");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPatientDatabaseReference = mFirebaseDatabase.getReference().child(Constants.PATIENTS_NODE).child(employeeId);
        mIncomeDatabaseReference = mFirebaseDatabase.getReference().child(Constants.INCOME_NODE);

        displayBasicData();

        displayCurrentMonth(mYearNumber, mMonthName);

        setClickingOnAddBalanceButton();

    }

    private void setClickingOnAddBalanceButton() {

        mAddBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String balanceString = mBalanceEditText.getText().toString().trim();


                if (balanceString.isEmpty() || Double.parseDouble(balanceString) == 0.0) {
                    mBalanceEditText.setError("enter the balance first");
                    mBalanceEditText.requestFocus();
                } else {

                    double balance = Double.parseDouble(balanceString);

                    // update the patient wallet inside the patients node.
                    mPatientDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {


                            double wallet = 0.0;

                            try {
                                wallet = snapshot.child(Constants.PATIENT_WALLET).getValue(Double.class);
                            } catch (Exception e) {
                                Log.i(LOG_TAG, "Exception from try-catch block when get balance value");
                            }


                            double dept = 0.0;

                            try {
                                dept = snapshot.child(Constants.PATIENT_DEPT).getValue(Double.class);
                            } catch (Exception e) {
                                Log.i(LOG_TAG, "Exception from try-catch block when get balance value");
                            }


                            double patientDept = dept;
                            double patientWallet = wallet;
                            // get values allWallet & allDept from income node
                            mIncomeDatabaseReference.
                                    addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {


                                            double incomeAllWalletFromServer = 0.0;
                                            try {
                                                incomeAllWalletFromServer = snapshot.child(Constants.INCOME_PATIENTS_WALLET).getValue(Double.class);
                                            } catch (Exception e) {
                                                Log.i(LOG_TAG, "Exception from try-catch block when get allPatientWallet value");
                                            }

                                            double incomeAllDeptFromServer = 0.0;
                                            try {
                                                incomeAllDeptFromServer = snapshot.child(Constants.INCOME_PATIENTS_DEPT).getValue(Double.class);
                                            } catch (Exception e) {
                                                Log.i(LOG_TAG, "Exception from try-catch block when get allPatientDept value");
                                            }



                                            double incomeYearWalletFromServer = 0.0;
                                            try {
                                                incomeYearWalletFromServer = snapshot.child(mYearNumber)
                                                        .child(Constants.INCOME_PATIENTS_WALLET).getValue(Double.class);
                                            } catch (Exception e) {
                                                Log.i(LOG_TAG, "Exception from try-catch block when get allPatientWallet value");
                                            }

                                            double incomeYearDeptFromServer = 0.0;
                                            try {
                                                incomeYearDeptFromServer = snapshot.child(mYearNumber)
                                                        .child(Constants.INCOME_PATIENTS_DEPT).getValue(Double.class);
                                            } catch (Exception e) {
                                                Log.i(LOG_TAG, "Exception from try-catch block when get allPatientDept value");
                                            }



                                            double incomeMonthWalletFromServer = 0.0;
                                            try {
                                                incomeMonthWalletFromServer = snapshot.child(mYearNumber).child(mMonthName)
                                                        .child(Constants.INCOME_PATIENTS_WALLET).getValue(Double.class);
                                            } catch (Exception e) {
                                                Log.i(LOG_TAG, "Exception from try-catch block when get allPatientWallet value");
                                            }

                                            double incomeMonthDeptFromServer = 0.0;
                                            try {
                                                incomeMonthDeptFromServer = snapshot.child(mYearNumber).child(mMonthName)
                                                        .child(Constants.INCOME_PATIENTS_DEPT).getValue(Double.class);
                                            } catch (Exception e) {
                                                Log.i(LOG_TAG, "Exception from try-catch block when get allPatientDept value");
                                            }


                                            // In case the patient dept is 0.0:
                                            // => patients node:
                                            //      update the wallet value by increasing it with
                                            //      the balance value added.
                                            // => income node:
                                            //      update allPatientsWallet by increasing the
                                            //      balance value in (all - year - month).
                                            if (patientDept == 0.0) {

                                                // => patients node.
                                                // update the wallet value for the patient.
                                                mPatientDatabaseReference.child(Constants.PATIENT_WALLET)
                                                        .setValue(patientWallet + balance);


                                                // => income node.
                                                // update the allPatientsWallet value.
                                                mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_WALLET)
                                                        .setValue(incomeAllWalletFromServer + balance);

                                                // => income node => {year number}.
                                                // update the allPatientsWallet value in year.
                                                mIncomeDatabaseReference.child(mYearNumber)
                                                        .child(Constants.INCOME_PATIENTS_WALLET)
                                                        .setValue(incomeYearWalletFromServer + balance);

                                                // => income node => {year number} => {month number}.
                                                // update the allPatientsWallet value in month.
                                                mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                        .child(Constants.INCOME_PATIENTS_WALLET)
                                                        .setValue(incomeMonthWalletFromServer + balance);


                                            }
                                            // In case the dept is bigger than or equal the
                                            // balance that the user added:
                                            // => patients node:
                                            //      update dept by decreasing the balance value.
                                            // => income node:
                                            //      update allPatientDept by decreasing the balance
                                            //      value in (all - year - month).
                                            else if (patientDept >= balance) {

                                                // => patients node.
                                                // update the dept value for the patient.
                                                mPatientDatabaseReference.child(Constants.PATIENT_DEPT)
                                                        .setValue(patientDept - balance);



                                                // => income node => {year number} => {month number}.
                                                // update the allPatientsDept value in income.
                                                mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_DEPT)
                                                        .setValue(incomeAllDeptFromServer - balance);

                                                // => income node => {year number} => {month number}.
                                                // update the allPatientsDept value in year.
                                                mIncomeDatabaseReference.child(mYearNumber)
                                                        .child(Constants.INCOME_PATIENTS_DEPT)
                                                        .setValue(incomeYearDeptFromServer - balance);

                                                // => income node => {year number} => {month number}.
                                                // update the allPatientsDept value in month.
                                                mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                        .child(Constants.INCOME_PATIENTS_DEPT)
                                                        .setValue(incomeMonthDeptFromServer - balance);


                                            }
                                            // In case the dept is smaller than the balance that
                                            // the user added:
                                            // => patients node:
                                            //      make the dept equal 0.0.
                                            //      update the wallet by increasing with the rest
                                            //      from mines the balance added and the dept.
                                            // => income nod:
                                            //      update the allPatientsDept by decreasing it with
                                            //      the patient dept value.
                                            //      update the allPatientsWallet by increasing with
                                            //      the rest from mines the balance added and the
                                            //      patient dept.
                                            else {

                                                // => patients node.
                                                // make the dept value for the patient to be 0.0.
                                                mPatientDatabaseReference.child(Constants.PATIENT_DEPT)
                                                        .setValue(0.0);

                                                // => patients node.
                                                // update the balance value.
                                                mPatientDatabaseReference.child(Constants.PATIENT_WALLET)
                                                        .setValue(balance - patientDept);



                                                // => income node => {year number} => {month number}.
                                                // update the allPatientsDept value in month.
                                                mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_DEPT)
                                                        .setValue(incomeAllDeptFromServer - patientDept);

                                                // => income node => {year number}.
                                                // update the allPatientsDept value in month.
                                                mIncomeDatabaseReference.child(mYearNumber)
                                                        .child(Constants.INCOME_PATIENTS_DEPT)
                                                        .setValue(incomeYearDeptFromServer - patientDept);

                                                // => income node => {year number} => {month number}.
                                                // update the allPatientsDept value in month.
                                                mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                        .child(Constants.INCOME_PATIENTS_DEPT)
                                                        .setValue(incomeMonthDeptFromServer - patientDept);



                                                // => income node.
                                                // update the allPatientsWallet value.
                                                mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_WALLET)
                                                        .setValue(incomeAllWalletFromServer + (balance - patientDept));

                                                // => income node => {year number}.
                                                // update the allPatientsWallet value in year.
                                                mIncomeDatabaseReference.child(mYearNumber)
                                                        .child(Constants.INCOME_PATIENTS_WALLET)
                                                        .setValue(incomeYearWalletFromServer + (balance - patientDept));

                                                // => income node => {year number} => {month number}.
                                                // update the allPatientsWallet value in month.
                                                mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                        .child(Constants.INCOME_PATIENTS_WALLET)
                                                        .setValue(incomeMonthWalletFromServer + (balance - patientDept));
                                            }




                                            // save the balance (time - who add it - the wallet value
                                            // before add the new balance) details in the server.
                                            BalanceObject balanceObject = new BalanceObject();
                                            balanceObject.setOldBalance(patientWallet);
                                            balanceObject.setNewBalance(balance);
                                            balanceObject.setAddedTime(mAddedTime);
                                            balanceObject.setAddedBy(mAddedBy);

                                            DatabaseReference balanceDetailsReference = mPatientDatabaseReference.child(Constants.PATIENT_WALLET_DETAILS);
                                            balanceDetailsReference.push().setValue(balanceObject).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                    // clear the EditText field and show toast to
                                                    // hint the user that the process is finished.
                                                    mBalanceEditText.setText("");
                                                    Toast.makeText(mContext, "balance added successfully", Toast.LENGTH_SHORT).show();
                                                    Log.i(LOG_TAG, "balance in patient node in specific patient id changed successfully");


                                                }
                                            });



                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });



                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });




                }


            }
        });

    }

    private void displayBasicData() {


        mPatientDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    if (dataSnapshot.child(Constants.PATIENT_NAME).getValue(String.class) != null) {
                        mPatientName = dataSnapshot.child(Constants.PATIENT_NAME).getValue(String.class);
                        mNameTextView.setText("Name : " + mPatientName);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_AGE).getValue(Double.class) != null) {
                        mPatientAge = dataSnapshot.child(Constants.PATIENT_AGE).getValue(Double.class);
                        mAgeTextView.setText("Age : " + mPatientAge);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_DISEASE).getValue(String.class) != null) {
                        mDisease = dataSnapshot.child(Constants.PATIENT_DISEASE).getValue(String.class);
                        mDiseaseTextView.setText("Disease : " + mDisease);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_PHONE_NUMBER).getValue(String.class) != null) {
                        mPhoneNumber = dataSnapshot.child(Constants.PATIENT_PHONE_NUMBER).getValue(String.class);
                        mPhoneNumberTextView.setText("Phone number : " + mPhoneNumber);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_WALLET).getValue(Double.class) != null) {
                        mBalance = dataSnapshot.child(Constants.PATIENT_WALLET).getValue(Double.class);
                        mBalanceTextView.setText("Balance : " + mBalance);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_DEPT).getValue(Double.class) != null) {
                        mDept = dataSnapshot.child(Constants.PATIENT_DEPT).getValue(Double.class);
                        mDeptTextView.setText("Dept : " + mDept);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_SESSION_COST).getValue(Double.class) != null) {
                        mSessionCost = dataSnapshot.child(Constants.PATIENT_SESSION_COST).getValue(Double.class);
                        mSessionCostTextView.setText("Session cost : " + mSessionCost);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_ALL_SESSION_NUMBER).getValue(Double.class) != null) {
                        mAllSessionNumber = dataSnapshot.child(Constants.PATIENT_ALL_SESSION_NUMBER).getValue(Double.class);
                        mAllSessionNumberTextView.setText("Sessions number : " + mAllSessionNumber);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_ADDED_TIME).getValue(Long.class) != null) {
                        mAddedTime = dataSnapshot.child(Constants.PATIENT_ADDED_TIME).getValue(Long.class);
                        mAddedTimeTextView.setText("Added time : " + mAddedTime);
                    }
                    if (dataSnapshot.child(Constants.PATIENT_ADDED_BY).getValue(String.class) != null) {
                        mAddedBy = dataSnapshot.child(Constants.PATIENT_ADDED_BY).getValue(String.class);
                        mAddedByTextView.setText("Added By : " + mAddedBy);
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

        mPatientDatabaseReference.child(Constants.EMPLOYEE_SESSIONS_DETAILS)
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
                                isCostChange = dataSnapshot.child(Constants.PATIENT_SESSION_COST_CHANGE).getValue(Boolean.class);
                            }
                            if (dataSnapshot.child(Constants.EMPLOYEE_CHANGE_DATE).getValue(Long.class) != null){
                                changeDate = dataSnapshot.child(Constants.PATIENT_CHANGE_DATE).getValue(Long.class);
                            }
                            if(dataSnapshot.child(Constants.EMPLOYEE_OLD_COST).getValue(Double.class) != null) {
                                oldCost = dataSnapshot.child(Constants.PATIENT_OLD_COST).getValue(Double.class);
                            }
                            if(dataSnapshot.child(Constants.EMPLOYEE_BEFORE_CHANGE).getValue(Double.class) != null) {
                                beforeChange = dataSnapshot.child(Constants.PATIENT_BEFORE_CHANGE).getValue(Double.class);
                            }
                            if (dataSnapshot.child(Constants.EMPLOYEE_AFTER_CHANGE).getValue(Double.class) != null){
                                afterChange = dataSnapshot.child(Constants.PATIENT_AFTER_CHANGE).getValue(Double.class);
                            }

                            mCurrentMonthSessionsNumberTextView.setText("Sessions number : " + sessionsNumber);
                            mCurrentMonthChangeCostTextView.setText("Is session change : " + isCostChange);
                            mCurrentMonthChangeDateTextView.setText("Change Date : " + changeDate);
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