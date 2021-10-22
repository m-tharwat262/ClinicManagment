package com.clinic.management.elnour.fragments.countings;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.activities.display.BasicPaymentsActivity;
import com.clinic.management.elnour.activities.display.DeptActivity;
import com.clinic.management.elnour.activities.display.OthersPaymentsActivity;
import com.clinic.management.elnour.activities.display.SalariesActivity;
import com.clinic.management.elnour.activities.display.WalletActivity;
import com.clinic.management.elnour.fragments.employees.EmployeeFragment;
import com.clinic.management.elnour.models.SessionObject;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CountingFragment extends Fragment {


    private static final String LOG_TAG = CountingFragment.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mIncomeDatabaseReference;
    private ChildEventListener mIncomeChildEventListener;

    private View mMainView;
    private TextView mIncomeTextView;
    private TextView mPaymentsTextView;
    private TextView mWalletTextView;
    private TextView mDeptTextView;
    private LinearLayout mSalariesItemLinearLayout;
    private LinearLayout mWalletItemLinearLayout;
    private LinearLayout mDeptItemLinearLayout;
    private LinearLayout mBasicsPaymentsItemLinearLayout;
    private LinearLayout mOtherPaymentsItemLinearLayout;



    double mIncome = 0.0;
    double mPayments = 0.0;
    double mWallet = 0.0;
    double mDept = 0.0;

    private boolean doubleBackToExitPressedOnce = false;

    public CountingFragment(Context context) {
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
        // inflate the fragment layout.
        mMainView = inflater.inflate(R.layout.fragment_counting, container, false);


        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));



        // initialize the views from the main layout.
        mIncomeTextView = mMainView.findViewById(R.id.fragment_counting_income_value);
        mPaymentsTextView = mMainView.findViewById(R.id.fragment_counting_payment_value);
        mWalletTextView = mMainView.findViewById(R.id.fragment_counting_wallet_value);
        mDeptTextView = mMainView.findViewById(R.id.fragment_counting_dept_value);
        mSalariesItemLinearLayout = mMainView.findViewById(R.id.fragment_counting_salaries);
        mWalletItemLinearLayout = mMainView.findViewById(R.id.fragment_counting_wallet);
        mDeptItemLinearLayout = mMainView.findViewById(R.id.fragment_counting_dept);
        mBasicsPaymentsItemLinearLayout = mMainView.findViewById(R.id.fragment_counting_basic_payments);
        mOtherPaymentsItemLinearLayout = mMainView.findViewById(R.id.fragment_counting_other_payments);


        // initialize firebase objects.
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mIncomeDatabaseReference = mFirebaseDatabase.getReference().child(Constants.INCOME_NODE);
        mIncomeDatabaseReference.keepSynced(true);


        // set listener to the income node inside firebase.
        attachIncomeDatabaseReadListener();

        // handle clicking on items.
        setClickingOnSalariesItem();
        setClickingOnWalletItem();
        setClickingOnDeptItem();
        setClickingOnBasicPaymentsItem();
        setClickingOnOtherPaymentsItem();


        // handle the back pressed button.
        controlBackPressedButton();


        // Inflate the layout for this fragment
        return mMainView;
    }

    private void setClickingOnSalariesItem() {

        mSalariesItemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, SalariesActivity.class);
                startActivity(intent);

            }
        });

    }


    private void setClickingOnWalletItem() {

        mWalletItemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, WalletActivity.class);
                startActivity(intent);

            }
        });

    }

    private void setClickingOnDeptItem() {

        mDeptItemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, DeptActivity.class);
                startActivity(intent);

            }
        });

    }


    private void setClickingOnBasicPaymentsItem() {

        mBasicsPaymentsItemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, BasicPaymentsActivity.class);
                startActivity(intent);

            }
        });

    }


    private void setClickingOnOtherPaymentsItem() {

        mOtherPaymentsItemLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, OthersPaymentsActivity.class);
                startActivity(intent);

            }
        });

    }



    private void attachIncomeDatabaseReadListener() {

        if (mIncomeChildEventListener == null) {
            mIncomeChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    updateLayout(snapshot);

                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

                    updateLayout(snapshot);

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
            mIncomeDatabaseReference.addChildEventListener(mIncomeChildEventListener);
        }

    }


    private void updateLayout(DataSnapshot snapshot) {


        if (snapshot.getKey().equals(Constants.INCOME_PATIENTS_WALLET)) {
            try {
                mWallet = snapshot.getValue(Double.class);
            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception inside try-catch block inside " +
                        "attachSessionDatabaseReadListener method when get patientsWallet " +
                        "value from firebase");
            }
        }


        if (snapshot.getKey().equals(Constants.INCOME_PATIENTS_DEPT)) {
            try {
                mDept = snapshot.getValue(Double.class);
            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception inside try-catch block inside " +
                        "attachSessionDatabaseReadListener method when get patientsWallet " +
                        "value from firebase");
            }
        }



        if (snapshot.getKey().equals(mYearNumber)) {
            try {
                mIncome = snapshot.child(mMonthName)
                        .child(Constants.INCOME_INCOME).getValue(Double.class);
            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception inside try-catch block inside " +
                        "attachSessionDatabaseReadListener method when get income " +
                        "value for specific month from firebase");
            }

            mPayments = 0.0;

            try {
                mPayments += snapshot.child(mMonthName)
                        .child(Constants.INCOME_SALARIES)
                        .child(Constants.INCOME_SALARIES_ALL_SALARIES_NOT_PAID).getValue(Double.class);
            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception inside try-catch block inside " +
                        "attachSessionDatabaseReadListener method when get allPayments " +
                        "value in salaries node for specific month from firebase");
            }

            try {
                mPayments += snapshot.child(mMonthName)
                        .child(Constants.INCOME_BASICS)
                        .child(Constants.INCOME_BASICS_ALL_PAYMENT).getValue(Double.class);
            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception inside try-catch block inside " +
                        "attachSessionDatabaseReadListener method when get allPayments " +
                        "value in basics node for specific month from firebase");
            }
        }


        mIncomeTextView.setText(String.valueOf(mIncome));
        mPaymentsTextView.setText(String.valueOf(mPayments));
        mWalletTextView.setText(String.valueOf(mWallet));
        mDeptTextView.setText(String.valueOf(mDept));


    }




    /**
     * Handle clicking on the back button.
     */
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
