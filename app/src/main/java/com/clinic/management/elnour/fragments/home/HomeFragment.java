package com.clinic.management.elnour.fragments.home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.activities.CreateAccountActivity;
import com.clinic.management.elnour.activities.MainActivity;
import com.clinic.management.elnour.activities.display.EmployeeDetailsActivity;
import com.clinic.management.elnour.activities.display.SessionDetailsActivity;
import com.clinic.management.elnour.adapters.EmployeeAdapter;
import com.clinic.management.elnour.adapters.SessionAdapter;
import com.clinic.management.elnour.fragments.employees.EmployeeFragment;
import com.clinic.management.elnour.models.EmployeeObject;
import com.clinic.management.elnour.models.PatientObject;
import com.clinic.management.elnour.models.SalaryObject;
import com.clinic.management.elnour.models.SessionObject;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private static final String LOG_TAG = EmployeeFragment.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mIncomeDatabaseReference;
    private DatabaseReference mEmployeeDatabaseReference;
    private DatabaseReference mPatientDatabaseReference;
    private DatabaseReference mSessionDatabaseReference;
    private DatabaseReference mSessionLastMonthDatabaseReference;
    private ChildEventListener mEmployeeChildEventListener;
    private ChildEventListener mPatientChildEventListener;
    private ChildEventListener mSessionChildEventListener;

    private View mMainView;
    private GridView mGridView;
    private LinearLayout mFloatingActionButton;

    private SessionAdapter mSessionAdapter;

    private boolean doubleBackToExitPressedOnce = false;

    private double mSessionsNumber;
    private String mNotes;

    private ArrayList<String> mAllSessionIds = new ArrayList<>();

    private int mEmployeePosition;
    private ArrayList<String> mAllEmployeeIds = new ArrayList<>();
    private ArrayList<String> mAllEmployeeNames = new ArrayList<>();
    private ArrayList<String> mAllEmployeeJobs = new ArrayList<>();
    private ArrayList<Double> mAllEmployeeSessionsCost = new ArrayList<>();

    private int mPatientPosition;
    private ArrayList<String> mAllPatientIds = new ArrayList<>();
    private ArrayList<String> mAllPatientNames = new ArrayList<>();
    private ArrayList<Double> mAllPatientSessionsCost = new ArrayList<>();
    private ArrayList<String> mAllPatientDiseases = new ArrayList<>();

    private double mDeptFromSession = 0.0;

    public HomeFragment(Context context) {
        // Required empty public constructor
        mContext = context;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // TODO: add progress bar until get data from the firebase.

        mMainView = inflater.inflate(R.layout.fragment_employee, container, false);

        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mIncomeDatabaseReference = mFirebaseDatabase.getReference().child(Constants.INCOME_NODE);
        mIncomeDatabaseReference.keepSynced(true);
        mEmployeeDatabaseReference = mFirebaseDatabase.getReference().child(Constants.EMPLOYEES_NODE);
        mEmployeeDatabaseReference.keepSynced(true);
        mPatientDatabaseReference = mFirebaseDatabase.getReference().child(Constants.PATIENTS_NODE);
        mPatientDatabaseReference.keepSynced(true);
        mSessionDatabaseReference = mFirebaseDatabase.getReference().child(Constants.SESSIONS_NODE);
        mSessionDatabaseReference.keepSynced(true);
        mSessionLastMonthDatabaseReference = mSessionDatabaseReference.child(mYearNumber).child(mMonthName);
        mSessionLastMonthDatabaseReference.keepSynced(true);



        mGridView = mMainView.findViewById(R.id.fragment_employee_grid_view);
        mFloatingActionButton = mMainView.findViewById(R.id.fragment_employee_floating_action_button);


        ArrayList<SessionObject> sessionObjects = new ArrayList<>();
        mSessionAdapter = new SessionAdapter(mContext, sessionObjects);
        mGridView.setAdapter(mSessionAdapter);

        attachEmployeeDatabaseReadListener();
        attachPatientDatabaseReadListener();
        attachSessionDatabaseReadListener();

        setClickingOnFloatingButton();

        setClickingOnItems();



        controlBackPressedButton();


        // Inflate the layout for this fragment
        return mMainView;
    }


    private void setClickingOnItems() {

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(mContext, SessionDetailsActivity.class);
                intent.putExtra("session_id", mAllSessionIds.get(position));
                startActivity(intent);

            }
        });

    }


    private void setClickingOnFloatingButton() {

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_add_session);
                dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                Spinner employeeSpinner = dialog.findViewById(R.id.dialog_add_session_employee_spinner);
                Spinner patientSpinner = dialog.findViewById(R.id.dialog_add_session_patient_spinner);
                EditText sessionNumberEditText = dialog.findViewById(R.id.dialog_add_session_sessions_number);
                EditText notesEditText = dialog.findViewById(R.id.dialog_add_session_notes);
                TextView addButton = dialog.findViewById(R.id.dialog_add_session_add_button);


                ArrayAdapter employeeSpinnerAdapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, mAllEmployeeNames);
                employeeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                employeeSpinner.setAdapter(employeeSpinnerAdapter);
                employeeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        mEmployeePosition = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        mEmployeePosition = -1;
                    }
                });


                ArrayAdapter patientSpinnerAdapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item, mAllPatientNames);
                patientSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                patientSpinner.setAdapter(patientSpinnerAdapter);
                patientSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        mPatientPosition = position;

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        mPatientPosition = -1;
                    }
                });


                addButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String sessionNumber = sessionNumberEditText.getText().toString().trim();
                        mNotes = notesEditText.getText().toString().trim();

                        if (mEmployeePosition == -1) {
                            Toast.makeText(mContext, "Select doctor first", Toast.LENGTH_SHORT).show();
                        } else if (mPatientPosition == -1) {
                            Toast.makeText(mContext, "Select patient first", Toast.LENGTH_SHORT).show();
                        } else if (sessionNumber.isEmpty()) {
                            sessionNumberEditText.setError("Enter sessions number first");
                            sessionNumberEditText.requestFocus();
                        } else if (Double.parseDouble(sessionNumber) < 0) {
                            sessionNumberEditText.setError("session number negative!");
                            sessionNumberEditText.requestFocus();
                        } else {

                            mSessionsNumber = Double.parseDouble(sessionNumber);
                            insertSessionInFirebase();

                            dialog.dismiss();

                        }



                    }
                });



                dialog.show();

            }
        });


    }

    private void insertSessionInFirebase() {

        long currentTime = (System.currentTimeMillis() / 1000);

        String userId = mFirebaseUser.getUid();

        SessionObject sessionObject = new SessionObject();
        sessionObject.setDoctorId(mAllEmployeeIds.get(mEmployeePosition));
        sessionObject.setDoctorName(mAllEmployeeNames.get(mEmployeePosition));
        sessionObject.setDoctorSessionCost(mAllEmployeeSessionsCost.get(mEmployeePosition));
        sessionObject.setPatientId(mAllPatientIds.get(mPatientPosition));
        sessionObject.setPatientName(mAllPatientNames.get(mPatientPosition));
        sessionObject.setPatientSessionCost(mAllPatientSessionsCost.get(mPatientPosition));
        sessionObject.setDisease(mAllPatientDiseases.get(mPatientPosition));
        sessionObject.setSessionsNumber(mSessionsNumber);
        sessionObject.setNotes(mNotes);
        sessionObject.setAddedTime(currentTime);
        sessionObject.setAddedBy(userId);

        DatabaseReference monthReference = mSessionDatabaseReference.child(mYearNumber).child(mMonthName);
        monthReference.push().setValue(sessionObject).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                updateDataInsideEmployeeNode();
                updateDataInsidePatientNode();
                updateDataInsideIncomeNode();


                // update the sessions number inside the sessions node.
                mSessionDatabaseReference.child(Constants.SESSION_ALL_SESSIONS_NUMBER)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                double sessionsNumber = 0.0;
                                try {
                                    sessionsNumber = snapshot.getValue(Double.class);
                                } catch (Exception e) {
                                    Log.i(LOG_TAG, "Exception form try-catch blocks inside insertSessionInFirebase method");
                                }


                                mSessionDatabaseReference.child(Constants.SESSION_ALL_SESSIONS_NUMBER)
                                        .setValue(sessionsNumber + mSessionsNumber).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                Log.i(LOG_TAG, "allSessionsNumber in sessions node changed successfully");

                                            }
                                        });


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



            }
        });


    }



    private void updateDataInsideEmployeeNode() {

        // update all sessions number.
        mEmployeeDatabaseReference.child(mAllEmployeeIds.get(mEmployeePosition))
                .child(Constants.EMPLOYEE_ALL_SESSION_NUMBER).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                double sessionsNumber = 0.0;

                try {
                    sessionsNumber = snapshot.getValue(Double.class);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "Exception from try-catch block when get allSessionsNumber value from a specific month");
                }


                // change the verifiedNumber value to be true.
                mEmployeeDatabaseReference.child(mAllEmployeeIds.get(mEmployeePosition))
                        .child(Constants.EMPLOYEE_ALL_SESSION_NUMBER)
                        .setValue(sessionsNumber + mSessionsNumber)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i(LOG_TAG, "allSessionsNumber in employee node changed successfully");
                            }
                        });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        DatabaseReference monthReference = mEmployeeDatabaseReference.child(mAllEmployeeIds.get(mEmployeePosition))
                .child(Constants.EMPLOYEE_SESSIONS_DETAILS).child(mYearNumber)
                .child(mMonthName);

        // update sessions number inside specific month and if the cost change in that month
        // update the sessions number in the node that count the sessions number after change.
        monthReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {


                        double sessionsNumber = 0;

                        try {
                            sessionsNumber = snapshot.child(Constants.EMPLOYEE_SESSIONS_NUMBER).getValue(Double.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get sessionsNumber value from a specific month");
                        }


                        monthReference.child(Constants.EMPLOYEE_SESSIONS_NUMBER)
                                .setValue(sessionsNumber + mSessionsNumber)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.i(LOG_TAG, "sessionsNumber in employee node in specific month changed successfully");
                                    }
                                });


                        boolean isSessionCostChange = false;

                        try {
                            isSessionCostChange = snapshot.child(Constants.EMPLOYEE_SESSION_COST_CHANGE).getValue(Boolean.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get isSessionCostChange value from a specific month");
                        }

                        if (isSessionCostChange) {

                            double afterChange = 0;

                            try {
                                afterChange = snapshot.child(Constants.EMPLOYEE_CHANGE_DETAILS)
                                        .child(Constants.EMPLOYEE_AFTER_CHANGE).getValue(Double.class);
                            } catch (Exception e) {
                                Log.i(LOG_TAG, "Exception from try-catch block when get afterChange value from a specific month");
                            }


                            monthReference.child(Constants.EMPLOYEE_CHANGE_DETAILS).child(Constants.EMPLOYEE_AFTER_CHANGE)
                                    .setValue(afterChange + mSessionsNumber)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            Log.i(LOG_TAG, "afterChange in employee node in specific month changed successfully");

                                        }
                                    });

                        }



                        // update employee balance
                        mEmployeeDatabaseReference.child(mAllEmployeeIds.get(mEmployeePosition))
                                .child(Constants.EMPLOYEE_BALANCE)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        double balanceFromServer = 0;

                                        try {
                                            balanceFromServer = snapshot.getValue(Double.class);
                                        } catch (Exception e) {
                                            Log.i(LOG_TAG, "Exception from try-catch block when get balance value");
                                        }

                                        double currentSessionsCostForEmployee = mSessionsNumber * mAllEmployeeSessionsCost.get(mEmployeePosition);


                                        mEmployeeDatabaseReference.child(mAllEmployeeIds.get(mEmployeePosition))
                                                .child(Constants.EMPLOYEE_BALANCE)
                                                .setValue(balanceFromServer + currentSessionsCostForEmployee)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        Log.i(LOG_TAG, "balance in patients node changed successfully");

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

    private void updateDataInsidePatientNode() {

        // update the all sessions number for the patient.
        mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                .child(Constants.PATIENT_ALL_SESSION_NUMBER)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                double sessionsNumber = 0;

                try {
                    sessionsNumber = snapshot.getValue(Double.class);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "Exception from try-catch block when get sessionsNumber value");
                }

                mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                        .child(Constants.PATIENT_ALL_SESSION_NUMBER)
                        .setValue(mSessionsNumber + sessionsNumber)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i(LOG_TAG, "sessionsNumber in patients node changed successfully");
                            }
                        });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        // update sessions number inside specific month and if the cost change in that month
        // update the sessions number in the node that count the sessions number after change.
        DatabaseReference monthReference = mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                .child(Constants.PATIENT_SESSIONS_DETAILS).child(mYearNumber)
                .child(mMonthName);


        monthReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                double sessionsNumber = 0;

                try {
                    sessionsNumber = snapshot.child(Constants.PATIENT_SESSIONS_NUMBER).getValue(Double.class);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "Exception from try-catch block when get sessionsNumber value from a specific month");
                }



                monthReference.child(Constants.PATIENT_SESSIONS_NUMBER)
                        .setValue(mSessionsNumber + sessionsNumber)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i(LOG_TAG, "sessionsNumber in patients node in specific month changed successfully");
                            }
                        });


                boolean isSessionCostChange = false;

                try {
                    isSessionCostChange = snapshot.child(Constants.PATIENT_SESSION_COST_CHANGE).getValue(Boolean.class);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "Exception from try-catch block when get isSessionCostChange value from a specific month");
                }

                if (isSessionCostChange) {

                    double afterChange = 0;

                    try {
                        afterChange = snapshot.child(Constants.PATIENT_CHANGE_DETAILS)
                                .child(Constants.PATIENT_AFTER_CHANGE).getValue(Double.class);
                    } catch (Exception e) {
                        Log.i(LOG_TAG, "Exception from try-catch block when get afterChange value from a specific month");
                    }


                    monthReference.child(Constants.PATIENT_CHANGE_DETAILS).child(Constants.PATIENT_AFTER_CHANGE)
                            .setValue(afterChange + mSessionsNumber)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    Log.i(LOG_TAG, "afterChange in patient node in specific month changed successfully");

                                }
                            });


                }





                // update patient wallet and dept => patients node.
                // update data => income node.
                mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                double wallet = 0.0;
                                try {
                                    wallet = snapshot.child(Constants.PATIENT_WALLET).getValue(Integer.class);
                                } catch (Exception e) {
                                    Log.i(LOG_TAG, "Exception from try-catch block when get wallet value");
                                }


                                double dept = 0.0;
                                try {
                                    dept = snapshot.child(Constants.PATIENT_DEPT).getValue(Integer.class);
                                } catch (Exception e) {
                                    Log.i(LOG_TAG, "Exception from try-catch block when get dept value");
                                }



                                double sessionsCost = mSessionsNumber * mAllPatientSessionsCost.get(mPatientPosition);




                                double patientWallet = wallet;
                                double patientDept = dept;
                                mIncomeDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                double incomeAllPatientsWallet = 0.0;
                                                try {
                                                    incomeAllPatientsWallet = snapshot.child(Constants.INCOME_PATIENTS_WALLET).getValue(Double.class);
                                                } catch (Exception e) {
                                                    Log.i(LOG_TAG, "Exception from try-catch block when get allPatientWallet value");
                                                }

                                                double incomeAllPatientsDept = 0.0;
                                                try {
                                                    incomeAllPatientsDept = snapshot.child(Constants.INCOME_PATIENTS_DEPT).getValue(Double.class);
                                                } catch (Exception e) {
                                                    Log.i(LOG_TAG, "Exception from try-catch block when get allPatientDept value");
                                                }

                                                double incomeYearPatientsWallet = 0.0;
                                                try {
                                                    incomeYearPatientsWallet = snapshot.child(mYearNumber)
                                                            .child(Constants.INCOME_PATIENTS_WALLET).getValue(Double.class);
                                                } catch (Exception e) {
                                                    Log.i(LOG_TAG, "Exception from try-catch block when get allPatientWallet value in year");
                                                }

                                                double incomeYearPatientsDept = 0.0;
                                                try {
                                                    incomeYearPatientsDept = snapshot.child(mYearNumber)
                                                            .child(Constants.INCOME_PATIENTS_DEPT).getValue(Double.class);
                                                } catch (Exception e) {
                                                    Log.i(LOG_TAG, "Exception from try-catch block when get allPatientDept value in year");
                                                }

                                                double incomeMonthPatientsWallet = 0.0;
                                                try {
                                                    incomeMonthPatientsWallet = snapshot.child(mYearNumber).child(mMonthName)
                                                            .child(Constants.INCOME_PATIENTS_WALLET).getValue(Double.class);
                                                } catch (Exception e) {
                                                    Log.i(LOG_TAG, "Exception from try-catch block when get allPatientWallet value in month");
                                                }

                                                double incomeMonthPatientsDept = 0.0;
                                                try {
                                                    incomeMonthPatientsDept = snapshot.child(mYearNumber).child(mMonthName)
                                                            .child(Constants.INCOME_PATIENTS_DEPT).getValue(Double.class);
                                                } catch (Exception e) {
                                                    Log.i(LOG_TAG, "Exception from try-catch block when get allPatientDept value in month");
                                                }





                                                // patient wallet mines session cost for the patient.
                                                double result = patientWallet - sessionsCost;

                                                // In case the patient wallet = 0.0$ :
                                                // update the patient dept value => patients node.
                                                // update patientsDept value in => income node (all - years - month).
                                                if (patientWallet == 0.0) {

                                                    // update the dept value (increasing the sessions cost)
                                                    // => patient node.
                                                    mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                                                            .child(Constants.PATIENT_DEPT).setValue(patientDept + sessionsCost);



                                                    // update the patientsDept value (increasing the sessions cost)
                                                    // => income node.
                                                    mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_DEPT)
                                                            .setValue(incomeAllPatientsDept + sessionsCost);

                                                    // update the patientsDept value (increasing the sessions cost)
                                                    // => income node => {year number}.
                                                    mIncomeDatabaseReference.child(mYearNumber)
                                                            .child(Constants.INCOME_PATIENTS_DEPT)
                                                            .setValue(incomeYearPatientsDept + sessionsCost);

                                                    // update the patientsDept value (increasing the sessions cost)
                                                    // => income node => {year number} => {month number}.
                                                    mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                            .child(Constants.INCOME_PATIENTS_DEPT)
                                                            .setValue(incomeMonthPatientsDept + sessionsCost);


                                                }
                                                // In case the patient wallet has enough money :
                                                // update the patient wallet value => patients node.
                                                // update patientsWallet value in => income node (all - years - month).
                                                else if (result >= 0.0) {

                                                    // update the wallet value (decreasing the sessions cost)
                                                    // => patient node.
                                                    mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                                                            .child(Constants.PATIENT_WALLET).setValue(result);



                                                    // update the patientsWallet value (decreasing the sessions cost)
                                                    // => income node.
                                                    mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_WALLET)
                                                            .setValue(incomeAllPatientsWallet - sessionsCost);

                                                    // update the patientsWallet value (decreasing the sessions cost)
                                                    // => income node => {year number}.
                                                    mIncomeDatabaseReference.child(mYearNumber)
                                                            .child(Constants.INCOME_PATIENTS_WALLET)
                                                            .setValue(incomeYearPatientsWallet - sessionsCost);

                                                    // update the patientsWallet value (decreasing the sessions cost)
                                                    // => income node => {year number} => {month number}.
                                                    mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                            .child(Constants.INCOME_PATIENTS_WALLET)
                                                            .setValue(incomeMonthPatientsWallet - sessionsCost);


                                                }
                                                // In case the patient wallet has money but not enough :
                                                // update the patient wallet value to be 0.0 => patients node.
                                                // update the patient dept value => patients node.
                                                // update patientsWallet value in => income node (all - years - month).
                                                // update patientsDept value in => income node (all - years - month).
                                                else {

                                                    // make the patient wallet value 0.0
                                                    mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                                                            .child(Constants.PATIENT_WALLET).setValue(0.0);

                                                    // make the patient wallet value 0.0
                                                    mPatientDatabaseReference.child(mAllPatientIds.get(mPatientPosition))
                                                            .child(Constants.PATIENT_DEPT).setValue(sessionsCost - patientWallet);



                                                    // update the patientsWallet value (decreasing the sessions cost)
                                                    // => income node.
                                                    mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_WALLET)
                                                            .setValue(incomeAllPatientsWallet - patientWallet);

                                                    // update the patientsWallet value (decreasing the sessions cost)
                                                    // => income node => {year number}.
                                                    mIncomeDatabaseReference.child(mYearNumber)
                                                            .child(Constants.INCOME_PATIENTS_WALLET)
                                                            .setValue(incomeYearPatientsWallet - patientWallet);

                                                    // update the patientsWallet value (decreasing the sessions cost)
                                                    // => income node => {year number} => {month number}.
                                                    mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                            .child(Constants.INCOME_PATIENTS_WALLET)
                                                            .setValue(incomeMonthPatientsWallet - patientWallet);




                                                    double deptFromSessionCost = sessionsCost - patientWallet;
                                                    Log.d(LOG_TAG, "the dept from session cost is equal : " + deptFromSessionCost);
                                                    // update the patientsDept value (increasing the sessions cost)
                                                    // => income node.
                                                    mIncomeDatabaseReference.child(Constants.INCOME_PATIENTS_DEPT)
                                                            .setValue(incomeAllPatientsDept + deptFromSessionCost);

                                                    // update the patientsDept value (increasing the sessions cost)
                                                    // => income node => {year number}.
                                                    mIncomeDatabaseReference.child(mYearNumber)
                                                            .child(Constants.INCOME_PATIENTS_DEPT)
                                                            .setValue(incomeYearPatientsDept + deptFromSessionCost);

                                                    // update the patientsDept value (increasing the sessions cost)
                                                    // => income node => {year number} => {month number}.
                                                    mIncomeDatabaseReference.child(mYearNumber).child(mMonthName)
                                                            .child(Constants.INCOME_PATIENTS_DEPT)
                                                            .setValue(incomeMonthPatientsDept + deptFromSessionCost);


                                                }


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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void updateDataInsideIncomeNode() {

        // update the income value for a specific year inside the income node.
        // update the income in a specific month.
        DatabaseReference yearReference = mIncomeDatabaseReference.child(mYearNumber);

        yearReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                double incomeFromSession = mSessionsNumber * mAllPatientSessionsCost.get(mPatientPosition) - mDeptFromSession;

                double incomeValue = 0.0;

                try {
                    incomeValue = snapshot.child(Constants.INCOME_INCOME).getValue(Double.class);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "Exception from try-catch block when get income value from a specific month");
                }


                yearReference.child(Constants.INCOME_INCOME)
                        .setValue(incomeValue + incomeFromSession)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i(LOG_TAG, "income in income node in specific month changed successfully");
                            }
                        });



                // to change the income value for a specific month inside the income node.
                DatabaseReference monthReference = yearReference.child(mMonthName);

                monthReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        double incomeFromSession = mSessionsNumber * mAllPatientSessionsCost.get(mPatientPosition) - mDeptFromSession;

                        double incomeValue = 0.0;

                        try {
                            incomeValue = snapshot.child(Constants.INCOME_INCOME).getValue(Double.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get income value from a specific month");
                        }


                        monthReference.child(Constants.INCOME_INCOME)
                                .setValue(incomeValue + incomeFromSession)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.i(LOG_TAG, "income in income node in specific month changed successfully");
                                    }
                                });






                        // get the salary inside the income node => salaries node
                        double salary = 0.0;
                        try {
                            salary = snapshot.child(Constants.INCOME_SALARIES)
                                    .child(mAllEmployeeIds.get(mEmployeePosition))
                                    .child(Constants.INCOME_SALARIES_SALARY).getValue(Double.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get salary value from a specific month");
                        }


                        // get the sessions number inside the income node => salaries node
                        double sessionsNumber = 0;
                        try {
                            sessionsNumber = snapshot.child(Constants.INCOME_SALARIES)
                                    .child(mAllEmployeeIds.get(mEmployeePosition))
                                    .child(Constants.INCOME_SALARIES_SESSIONS_NUMBER).getValue(Double.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get sessionsNumber value from a specific month");
                        }



                        double allSalaries = 0.0;
                        try {
                            allSalaries = snapshot.child(Constants.INCOME_SALARIES)
                                    .child(Constants.INCOME_SALARIES_ALL_SALARIES)
                                    .getValue(Double.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get allSalaries value from a specific month");
                        }

                        double allSalariesNotPaid = 0.0;
                        try {
                            allSalariesNotPaid = snapshot.child(Constants.INCOME_SALARIES)
                                    .child(Constants.INCOME_SALARIES_ALL_SALARIES_NOT_PAID)
                                    .getValue(Double.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get allSalariesNotPaid value from a specific month");
                        }




                        // the percent for the employee from the session that he work on.
                        double employeePercent = mSessionsNumber * mAllEmployeeSessionsCost.get(mEmployeePosition);


                        // update the allSalaries value.
                        monthReference.child(Constants.INCOME_SALARIES)
                                .child(Constants.INCOME_SALARIES_ALL_SALARIES)
                                .setValue(allSalaries + employeePercent);

                        // update the allSalariesNotPaid value.
                        monthReference.child(Constants.INCOME_SALARIES)
                                .child(Constants.INCOME_SALARIES_ALL_SALARIES_NOT_PAID)
                                .setValue(allSalariesNotPaid + employeePercent);


                        // create a SalaryObject and insert it with the employee id (from employees
                        // node) in a specific month.
                        SalaryObject salaryObject = new SalaryObject();
                        salaryObject.setName(mAllEmployeeNames.get(mEmployeePosition));
                        salaryObject.setJob(mAllEmployeeJobs.get(mEmployeePosition));
                        salaryObject.setSalary(salary + employeePercent);
                        salaryObject.setSessionsNumber(sessionsNumber + mSessionsNumber);

                        monthReference.child(Constants.INCOME_SALARIES)
                                .child(mAllEmployeeIds.get(mEmployeePosition)).setValue(salaryObject);

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




    private void attachSessionDatabaseReadListener() {

        if (mSessionChildEventListener == null) {
            mSessionChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
                    SessionObject sessionObject = snapshot.getValue(SessionObject.class);
                    mSessionAdapter.add(sessionObject);
                    mAllSessionIds.add(snapshot.getKey());


                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                }
                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                }
                @Override
                public void onCancelled(DatabaseError error) {
                }
            };
            mSessionLastMonthDatabaseReference.addChildEventListener(mSessionChildEventListener);
        }

    }

    private void attachEmployeeDatabaseReadListener() {

        if (mEmployeeChildEventListener == null) {
            mEmployeeChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    EmployeeObject employeeObject = snapshot.getValue(EmployeeObject.class);
                    if (employeeObject != null) {
                        mAllEmployeeNames.add(employeeObject.getName());
                        mAllEmployeeJobs.add(employeeObject.getJob());
                        mAllEmployeeSessionsCost.add(employeeObject.getSessionCost());
                    }

                    mAllEmployeeIds.add(snapshot.getKey());


                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                }
                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                }
                @Override
                public void onCancelled(DatabaseError error) {
                }
            };

            mEmployeeDatabaseReference.addChildEventListener(mEmployeeChildEventListener);
        }

    }

    private void attachPatientDatabaseReadListener() {

        if (mPatientChildEventListener == null) {
            mPatientChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    PatientObject patientObject = snapshot.getValue(PatientObject.class);
                    if (patientObject != null) {
                        mAllPatientNames.add(patientObject.getName());
                        mAllPatientSessionsCost.add(patientObject.getSessionCost());
                        mAllPatientDiseases.add(patientObject.getDisease());

                    }

                    mAllPatientIds.add(snapshot.getKey());


                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
                }
                @Override
                public void onChildRemoved(DataSnapshot snapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
                }
                @Override
                public void onCancelled(DatabaseError error) {
                }
            };

            mPatientDatabaseReference.addChildEventListener(mPatientChildEventListener);
        }

    }


    private void controlBackPressedButton() {

        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                if (doubleBackToExitPressedOnce) {
                    getActivity().finish();
                    return;
                }
                doubleBackToExitPressedOnce = true;
                Toast.makeText(mContext, getResources().getString(R.string.click_back_again_to_exit), Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);


            }
        });

    }





}
