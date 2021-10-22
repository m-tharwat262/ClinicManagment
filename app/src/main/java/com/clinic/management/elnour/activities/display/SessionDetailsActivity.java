package com.clinic.management.elnour.activities.display;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.fragments.employees.EmployeeFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SessionDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = SessionDetailsActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private TextView mDoctorNameTextView;
    private TextView mDoctorIdTextView;
    private TextView mDoctorSessionCostTextView;
    private TextView mPatientNameTextView;
    private TextView mPatientIdTextView;
    private TextView mPatientSessionCostTextView;
    private TextView mDiseaseTextView;
    private TextView mSessionNumberTextView;
    private TextView mNotesTextView;
    private TextView mAddedTimeTextView;
    private TextView mAddedByTextView;

    private DatabaseReference mEmployeeDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;

    private String mDoctorName;
    private String mDoctorId;
    private double mDoctorSessionCost;
    private String mPatientName;
    private String mPatientId;
    private double mPatientSessionCost;
    private String mDisease;
    private double mSessionNumber;
    private String mNotes;
    private long mAddedTime;
    private String mAddedBy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        mContext = SessionDetailsActivity.this;

        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));


        mDoctorNameTextView = findViewById(R.id.activity_session_doctor_name);
        mDoctorIdTextView = findViewById(R.id.activity_session_doctor_id);
        mDoctorSessionCostTextView = findViewById(R.id.activity_session_doctor_session_cost);
        mPatientNameTextView = findViewById(R.id.activity_session_patient_name);
        mPatientIdTextView = findViewById(R.id.activity_session_patient_id);
        mPatientSessionCostTextView = findViewById(R.id.activity_session_patient_session_cost);
        mDiseaseTextView = findViewById(R.id.activity_session_disease);
        mSessionNumberTextView = findViewById(R.id.activity_session_session_number);
        mNotesTextView = findViewById(R.id.activity_session_notes);
        mAddedTimeTextView = findViewById(R.id.activity_session_added_time);
        mAddedByTextView = findViewById(R.id.activity_session_added_by);


        String sessionId = getIntent().getStringExtra("session_id");

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEmployeeDatabaseReference = mFirebaseDatabase.getReference().child(Constants.SESSIONS_NODE)
                .child(mYearNumber).child(mMonthName).child(sessionId);

        displayBasicData();


    }

    private void displayBasicData() {


        mEmployeeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {

                if (dataSnapshot != null) {

                    if (dataSnapshot.child(Constants.SESSION_DOCTOR_NAME).getValue(String.class) != null) {
                        mDoctorName = dataSnapshot.child(Constants.SESSION_DOCTOR_NAME).getValue(String.class);
                        mDoctorNameTextView.setText("Doctor name : " + mDoctorName);
                    }
                    if (dataSnapshot.child(Constants.SESSION_DOCTOR_ID).getValue(String.class) != null) {
                        mDoctorId = dataSnapshot.child(Constants.SESSION_DOCTOR_ID).getValue(String.class);
                        mDoctorIdTextView.setText("Doctor id : " + mDoctorId);
                    }
                    if (dataSnapshot.child(Constants.SESSION_DOCTOR_SESSION_COST).getValue(Double.class) != null) {
                        mDoctorSessionCost = dataSnapshot.child(Constants.SESSION_DOCTOR_SESSION_COST).getValue(Double.class);
                        mDoctorSessionCostTextView.setText("Doctor session cost : " + mDoctorSessionCost);
                    }
                    if (dataSnapshot.child(Constants.SESSION_PATIENT_NAME).getValue(String.class) != null) {
                        mPatientName = dataSnapshot.child(Constants.SESSION_PATIENT_NAME).getValue(String.class);
                        mPatientNameTextView.setText("Patient name : " + mPatientName);
                    }
                    if (dataSnapshot.child(Constants.SESSION_PATIENT_ID).getValue(String.class) != null) {
                        mPatientId = dataSnapshot.child(Constants.SESSION_PATIENT_ID).getValue(String.class);
                        mPatientIdTextView.setText("Patient id : " + mPatientId);
                    }
                    if (dataSnapshot.child(Constants.SESSION_PATIENT_SESSION_COST).getValue(Double.class) != null) {
                        mPatientSessionCost = dataSnapshot.child(Constants.SESSION_PATIENT_SESSION_COST).getValue(Double.class);
                        mPatientSessionCostTextView.setText("Patient session cost : " + mPatientSessionCost);
                    }
                    if (dataSnapshot.child(Constants.SESSION_DISEASE).getValue(String.class) != null) {
                        mDisease = dataSnapshot.child(Constants.SESSION_DISEASE).getValue(String.class);
                        mDiseaseTextView.setText("Disease : " + mDisease);
                    }
                    if (dataSnapshot.child(Constants.SESSION_NOTES).getValue(String.class) != null) {
                        mNotes = dataSnapshot.child(Constants.SESSION_NOTES).getValue(String.class);
                        mNotesTextView.setText("Notes : " + mNotes);
                    }
                    if (dataSnapshot.child(Constants.SESSION_SESSIONS_NUMBER).getValue(Double.class) != null) {
                        mSessionNumber = dataSnapshot.child(Constants.SESSION_SESSIONS_NUMBER).getValue(Double.class);
                        mSessionNumberTextView.setText("Sessions number : " + mSessionNumber);

                    }
                    if (dataSnapshot.child(Constants.SESSION_ADDED_TIME).getValue(Long.class) != null) {
                        mAddedTime = dataSnapshot.child(Constants.SESSION_ADDED_TIME).getValue(Long.class);
                        mAddedTimeTextView.setText("Added time : " + mAddedTime);
                    }
                    if (dataSnapshot.child(Constants.SESSION_ADDED_BY).getValue(String.class) != null) {
                        mAddedBy = dataSnapshot.child(Constants.SESSION_ADDED_BY).getValue(String.class);
                        mAddedByTextView.setText("Added time : " + mAddedBy);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(LOG_TAG, "onCancelled", databaseError.toException());
            }
        });

    }



}