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
import com.clinic.management.elnour.adapters.WalletAdapter;
import com.clinic.management.elnour.models.PatientObject;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class WalletActivity extends AppCompatActivity {

    private static final String LOG_TAG = DeptActivity.class.getSimpleName();
    private Context mContext;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mPatientReference;
    private DatabaseReference mIncomeReference;
    private ChildEventListener mPatientChildEventListener;
    private ChildEventListener mIncomeChildEventListener;

    private TextView mAllWalletValueTextView;
    private ListView mListView;

    private WalletAdapter mWalletAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);


        mContext = WalletActivity.this;

        mAllWalletValueTextView = findViewById(R.id.activity_wallet_all_wallet_value);
        mListView = findViewById(R.id.activity_wallet_list_view);


        ArrayList<PatientObject> patientObjects = new ArrayList<>();
        mWalletAdapter = new WalletAdapter(mContext, patientObjects);
        mListView.setAdapter(mWalletAdapter);

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
                mWalletAdapter.add(patientObject);
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

            if (Constants.INCOME_PATIENTS_WALLET.equals(snapshot.getKey())) {
                double wallet = snapshot.getValue(Double.class);
                mAllWalletValueTextView.setText(wallet + "$");
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception from try-catch block inside updateLayout method when get patientsDept");
        }

    }

}