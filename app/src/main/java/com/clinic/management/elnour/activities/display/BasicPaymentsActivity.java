package com.clinic.management.elnour.activities.display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.PatientObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BasicPaymentsActivity extends AppCompatActivity {

    private static final String LOG_TAG = BasicPaymentsActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mBasicPaymentReference;
    private DatabaseReference mIncomeReference;
    private DatabaseReference mYearReference;
    private DatabaseReference mMonthReference;
    private ChildEventListener mBasicPaymentChildEventListener;

    private TextView mAllBasicsPaymentsTextView;
    private LinearLayout mElectricityLinearLayout;
    private LinearLayout mWaterLinearLayout;
    private LinearLayout mGasLinearLayout;
    private LinearLayout mRentLinearLayout;

    private static final String ELECTRICITY_TITLE = "Electricity";
    private static final String WATER_TITLE = "Water";
    private static final String GAS_TITLE = "Gas";
    private static final String RENT_TITLE = "Rent";

    private double mOldValue;
    private boolean mOldIsChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_payments);

        mContext = BasicPaymentsActivity.this;

        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));



        mAllBasicsPaymentsTextView = findViewById(R.id.activity_basic_payments_all_basic_payment_value);
        mElectricityLinearLayout = findViewById(R.id.activity_basic_payments_electricity);
        mWaterLinearLayout = findViewById(R.id.activity_basic_payments_water);
        mGasLinearLayout = findViewById(R.id.activity_basic_payments_gas);
        mRentLinearLayout = findViewById(R.id.activity_basic_payments_rent);


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mIncomeReference = mFirebaseDatabase.getReference();
        mIncomeReference.keepSynced(true);
        mYearReference = mIncomeReference.child(Constants.INCOME_NODE).child(mYearNumber);
        mYearReference.keepSynced(true);
        mMonthReference = mYearReference.child(mMonthName);
        mMonthReference.keepSynced(true);
        mBasicPaymentReference = mMonthReference.child(Constants.INCOME_BASICS);
        mBasicPaymentReference.keepSynced(true);



        // set listener to the basic payments in income node inside firebase.
        attachBasicPaymentsDatabaseReadListener();


        setClickingOnItems();




    }

    private void setClickingOnItems() {

        setOnClickingOnElectricityItem();
        setOnClickingOnWaterItem();
        setOnClickingOnGasItem();
        setOnClickingOnRentItem();

    }

    private void setOnClickingOnElectricityItem() {

        mElectricityLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showBasicDialog(ELECTRICITY_TITLE, Constants.INCOME_BASICS_ELECTRICITY);

            }
        });

    }

    private void setOnClickingOnWaterItem() {

        mWaterLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showBasicDialog(WATER_TITLE, Constants.INCOME_BASICS_WATER);

            }
        });

    }

    private void setOnClickingOnGasItem() {

        mGasLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showBasicDialog(GAS_TITLE, Constants.INCOME_BASICS_GAS);

            }
        });

    }

    private void setOnClickingOnRentItem() {

        mRentLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showBasicDialog(RENT_TITLE, Constants.INCOME_BASICS_RENT);

            }
        });

    }




    private void showBasicDialog(String basicItemType, String nodeKey) {


        Dialog dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_basic_payment);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        TextView nameTextView = dialog.findViewById(R.id.dialog_basic_payment_name);
        EditText valueEditText = dialog.findViewById(R.id.dialog_basic_payment_value);
        CheckBox paidCheckBox = dialog.findViewById(R.id.dialog_basic_payment_check_box);
        TextView saveButton = dialog.findViewById(R.id.dialog_basic_payment_save_button);


        nameTextView.setText(basicItemType);

        // TODO: add progress bar to wait until the old value loaded.

        mBasicPaymentReference.child(nodeKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        mOldValue = 0.0;
                        try {
                            mOldValue = snapshot.child(Constants.INCOME_BASICS_COST)
                                    .getValue(Double.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get old {nodeKey} value");
                        }

                        if (mOldValue != 0.0) {
                            valueEditText.setText(String.valueOf(mOldValue));
                        }


                        mOldIsChecked = false;
                        try {
                            mOldIsChecked = snapshot.child(Constants.INCOME_BASICS_PAID)
                                    .getValue(Boolean.class);
                        } catch (Exception e) {
                            Log.i(LOG_TAG, "Exception from try-catch block when get old paid value");
                        }

                        paidCheckBox.setChecked(mOldIsChecked);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String stringValue = valueEditText.getText().toString().trim();
                double newValue = 0.0;
                if (!stringValue.isEmpty()) {
                    newValue = Double.valueOf(stringValue);
                }

                boolean isChecked = paidCheckBox.isChecked();

                updateValueInFirebase(nodeKey, newValue, isChecked);

                dialog.dismiss();

            }
        });


        dialog.show();



    }


    /**
     * Update value for basics payments nodes like (electricity - water - gas - rent).
     *
     * @param key refer to which node that the select (electricity - water - gas - rent).
     * @param newValue the value for the specific basic payment item.
     * @param isChecked if the user choose to pay the item or not.
     */
    private void updateValueInFirebase(String key, double newValue, boolean isChecked) {

        // update the cost value.
        mBasicPaymentReference.child(key)
                .child(Constants.INCOME_BASICS_COST).setValue(newValue);


        // update the paid value (true or false).
        mBasicPaymentReference.child(key)
                .child(Constants.INCOME_BASICS_PAID).setValue(isChecked);

        // if the user choose to pay the item , update the paidBy and paidDate values.
        if (isChecked) {

            // update the paidDate value.
            Long currentTime = System.currentTimeMillis() / 1000;
            mBasicPaymentReference.child(key)
                    .child(Constants.INCOME_BASICS_PAID_DATE).setValue(currentTime);

            // update the paidBy value.
            String userId = mFirebaseUser.getUid();
            mBasicPaymentReference.child(key)
                    .child(Constants.INCOME_BASICS_PAID_BY).setValue(userId);

        }




        // just if the isPaid value changed the income in the year and the month will be changing.
        if (isChecked != mOldIsChecked) {

            // make the mOldChecked now like the new one.
            mOldIsChecked = isChecked;

            // change the income value in specific year.
            mYearReference.child(Constants.INCOME_INCOME).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            double yearIncome = 0.0;
                            try {
                                yearIncome = snapshot.getValue(Double.class);
                            } catch (Exception e) {
                                Log.i(LOG_TAG, "Exception from try-catch block inside updateFirebase method when get income value");
                            }

                            double newYearIncomeValue;
                            if (isChecked) {
                                newYearIncomeValue = yearIncome - newValue;
                            } else {
                                newYearIncomeValue = yearIncome + newValue;
                            }

                            mYearReference.child(Constants.INCOME_INCOME).setValue(newYearIncomeValue);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


            // change the income value in specific month.
            mMonthReference.child(Constants.INCOME_INCOME).
                    addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            double monthIncome = 0.0;
                            try {
                                monthIncome = snapshot.getValue(Double.class);
                            } catch (Exception e) {
                                Log.i(LOG_TAG, "Exception from try-catch block inside updateFirebase method when get income value");
                            }

                            // create a new
                            double newMonthIncomeValue;
                            if (isChecked) {
                                newMonthIncomeValue = monthIncome - newValue;
                            } else {
                                newMonthIncomeValue = monthIncome + newValue;
                            }

                            mMonthReference.child(Constants.INCOME_INCOME).setValue(newMonthIncomeValue);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }


    }



    private void attachBasicPaymentsDatabaseReadListener() {

        if (mBasicPaymentChildEventListener == null) {
            mBasicPaymentChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    updateAllPaymentValue(snapshot);

                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

                    updateAllPaymentValue(snapshot);

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

            mBasicPaymentReference.addChildEventListener(mBasicPaymentChildEventListener);
        }

    }


    private void updateAllPaymentValue(DataSnapshot snapshot) {

        if (Constants.INCOME_BASICS_ALL_PAYMENT.equals(snapshot.getKey())) {

            double allPayments = 0.0;
            try {

                allPayments = snapshot.getValue(Double.class);

            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception from try-catch block inside updateAllPaymentValue method when get allPayments value");
            }

            mAllBasicsPaymentsTextView.setText(String.valueOf(allPayments));

        }


    }

}