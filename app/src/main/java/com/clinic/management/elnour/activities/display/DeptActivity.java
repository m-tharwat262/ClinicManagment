package com.clinic.management.elnour.activities.display;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.adapters.DeptAdapter;
import com.clinic.management.elnour.models.PatientObject;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DeptActivity extends AppCompatActivity {


    private static final String LOG_TAG = DeptActivity.class.getSimpleName();
    private Context mContext;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPatientReference;
    private DatabaseReference mIncomeReference;
    private ChildEventListener mPatientChildEventListener;
    private ChildEventListener mIncomeChildEventListener;

    private TextView mAllDeptValueTextView;
    private ListView mListView;

    private DeptAdapter mDeptAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dept);


        mContext = DeptActivity.this;

        mAllDeptValueTextView = findViewById(R.id.activity_dept_all_dept_value);
        mListView = findViewById(R.id.activity_dept_list_view);


        ArrayList<PatientObject> patientObjects = new ArrayList<>();
        mDeptAdapter = new DeptAdapter(mContext, patientObjects);
        mListView.setAdapter(mDeptAdapter);

        // initialize firebase objects.
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mPatientReference = mFirebaseDatabase.getReference().child(Constants.PATIENTS_NODE);
        mPatientReference.keepSynced(true);
        mIncomeReference = mFirebaseDatabase.getReference().child(Constants.INCOME_NODE);
        mIncomeReference.keepSynced(true);



        // set listener to the patient node inside firebase.
        attachPatientDatabaseReadListener();

        // set listener to the income node inside firebase.
        attachIncomeDatabaseReadListener();



    }


    private void attachPatientDatabaseReadListener() {

        if (mPatientChildEventListener == null) {
            mPatientChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    updateListView(snapshot);

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

            mPatientReference.addChildEventListener(mPatientChildEventListener);
        }

    }


    private void updateListView(DataSnapshot snapshot) {

        try {
            PatientObject patientObject = snapshot.getValue(PatientObject.class);
            if (patientObject != null) {
                mDeptAdapter.add(patientObject);
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception from try-catch block inside updateLayout method when get PatientObject");
        }

    }



    private void attachIncomeDatabaseReadListener() {

        if (mIncomeChildEventListener == null) {
            mIncomeChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    updateAllDeptValue(snapshot);

                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

                    updateAllDeptValue(snapshot);

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

            mIncomeReference.addChildEventListener(mIncomeChildEventListener);
        }

    }


    private void updateAllDeptValue(DataSnapshot snapshot) {

        try {

            if (Constants.INCOME_PATIENTS_DEPT.equals(snapshot.getKey())) {
                double dept = snapshot.getValue(Double.class);
                mAllDeptValueTextView.setText(dept + "$");
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception from try-catch block inside updateLayout method when get patientsDept");
        }

    }

}