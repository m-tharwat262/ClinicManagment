package com.clinic.management.elnour.activities.display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.adapters.OtherPaymentAdapter;
import com.clinic.management.elnour.models.EmployeeObject;
import com.clinic.management.elnour.models.PaymentObject;
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

public class OthersPaymentsActivity extends AppCompatActivity {

    private static final String LOG_TAG = OthersPaymentsActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mOthersPaymentReference;
    private DatabaseReference mOthersPaymentItemsReference;
    private ChildEventListener mOtherPaymentItemsChildEventListener;
    private ChildEventListener mOtherPaymentAllPaymentChildEventListener;

    private TextView mAllOthersPaymentsTextView;
    private LinearLayout mFloatingActionButton;
    private ListView mListView;

    private OtherPaymentAdapter mOtherPaymentAdapter;

    ArrayList<String> mItemsIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_payments);

        mContext = OthersPaymentsActivity.this;

        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();
        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));


        mAllOthersPaymentsTextView = findViewById(R.id.activity_others_payments_all_others_payment_value);
        mFloatingActionButton = findViewById(R.id.activity_others_payments_floating_action_button);
        mListView = findViewById(R.id.activity_others_payments_list_view);


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mOthersPaymentReference = mFirebaseDatabase.getReference().child(Constants.INCOME_NODE)
                .child(mYearNumber).child(mMonthName).child(Constants.INCOME_OTHERS);
        mOthersPaymentReference.keepSynced(true);
        mOthersPaymentItemsReference = mOthersPaymentReference.child(Constants.INCOME_OTHERS_ITEMS);
        mOthersPaymentItemsReference.keepSynced(true);


        ArrayList<PaymentObject> paymentObjects = new ArrayList<>();
        mOtherPaymentAdapter = new OtherPaymentAdapter(mContext, paymentObjects);
        mListView.setAdapter(mOtherPaymentAdapter);


        // set listener to the allPayment value inside income => otherPayments node inside firebase.
        attachAllPaymentsDatabaseReadListener();

        // set listener to the items(PaymentObject) value inside income => otherPayments node inside firebase.
        attachItemsDatabaseReadListener();

        // handle clicking on the floating action button.
        setClickingOnFloatingButton();

        // handleClicking on items in the ListView.
        setClickingOnItems();

    }





    /**
     * Handle Clicking on the floating action button.
     */
    private void setClickingOnFloatingButton() {

        // display a dialog to the user to enter the employee info to add it to the firebase.
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // create and display a dialog for add a new employee.
                Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_other_payment);
                dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                // initialize views inside the dialog layout.
                EditText itemNameEditText = dialog.findViewById(R.id.dialog_other_payment_item_name);
                EditText itemsNumberEditText = dialog.findViewById(R.id.dialog_other_payment_items_number);
                EditText itemCostEditText = dialog.findViewById(R.id.dialog_other_payment_item_cost);
                TextView allItemsCostTextView = dialog.findViewById(R.id.dialog_other_payment_all_items_cost);
                TextView saveButton = dialog.findViewById(R.id.dialog_other_payment_save_button);


                itemsNumberEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        String itemsNumberAsString = itemsNumberEditText.getText().toString().trim();
                        String itemCostAsString = itemCostEditText.getText().toString().trim();

                        double itemsNumber = 0;
                        if (!itemsNumberAsString.isEmpty()) {
                            itemsNumber = Double.parseDouble(itemsNumberAsString);
                        }
                        double itemCost = 0.0;
                        if (!itemCostAsString.isEmpty()) {
                            itemCost = Double.parseDouble(itemCostAsString);
                        }

                        double allItemsCost = itemsNumber * itemCost;
                        allItemsCostTextView.setText(String.valueOf(allItemsCost));

                    }
                });

                itemCostEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                        String itemsNumberAsString = itemsNumberEditText.getText().toString().trim();
                        String itemCostAsString = itemCostEditText.getText().toString().trim();

                        double itemsNumber = 0;
                        if (!itemsNumberAsString.isEmpty()) {
                            itemsNumber = Double.parseDouble(itemsNumberAsString);
                        }
                        double itemCost = 0.0;
                        if (!itemCostAsString.isEmpty()) {
                            itemCost = Double.parseDouble(itemCostAsString);
                        }

                        double allItemsCost = itemsNumber * itemCost;
                        allItemsCostTextView.setText(String.valueOf(allItemsCost));

                    }
                });



                // handle clicking on the add button.
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // get the data from the views.
                        String itemName = itemNameEditText.getText().toString().trim();
                        String itemsNumberAsString = itemsNumberEditText.getText().toString().trim();
                        String itemCostAsString = itemCostEditText.getText().toString().trim();


                        // check if the data that the user inserted is valid ot not.
                        // if it is valid, start inset it in the firebase.
                        if (itemName.isEmpty()) {
                            itemNameEditText.setError("Enter item name first");
                            itemNameEditText.requestFocus();
                        } else if (itemsNumberAsString.isEmpty()) {
                            itemsNumberEditText.setError("Enter items number first");
                            itemsNumberEditText.requestFocus();
                        } else if (itemCostAsString.isEmpty()) {
                            itemCostEditText.setError("Enter the one item cost first");
                            itemCostEditText.requestFocus();
                        } else {
                            double itemsNumber = Double.parseDouble(itemsNumberAsString);
                            double itemCost = Double.parseDouble(itemCostAsString);
                            insertItemInFirebase(itemName, itemsNumber, itemCost);
                            dialog.dismiss();
                        }

                    }
                });

                dialog.show();


            }
        });


    }

    /**
     * Handle Clicking on items inside the GridView.
     */
    private void setClickingOnItems() {

        // send the user to EmployeeDetailsActivity to display all the employee info.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                // create and display a dialog for add a new employee.
                Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_other_payment);
                dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                // initialize views inside the dialog layout.
                EditText itemNameEditText = dialog.findViewById(R.id.dialog_other_payment_item_name);
                EditText itemsNumberEditText = dialog.findViewById(R.id.dialog_other_payment_items_number);
                EditText itemCostEditText = dialog.findViewById(R.id.dialog_other_payment_item_cost);
                TextView allItemsCostTextView = dialog.findViewById(R.id.dialog_other_payment_all_items_cost);
                TextView saveButton = dialog.findViewById(R.id.dialog_other_payment_save_button);


                itemsNumberEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                        String itemsNumberAsString = itemsNumberEditText.getText().toString().trim();
                        String itemCostAsString = itemCostEditText.getText().toString().trim();
                        double itemsNumber = Double.parseDouble(itemsNumberAsString);
                        double itemCost = Double.parseDouble(itemCostAsString);

                        double allItemsCost = itemsNumber * itemCost;
                        allItemsCostTextView.setText(String.valueOf(allItemsCost));

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });


                // handle clicking on the add button.
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // get the data from the views.
                        String itemName = itemNameEditText.getText().toString().trim();
                        String itemsNumberAsString = itemsNumberEditText.getText().toString().trim();
                        String itemCostAsString = itemCostEditText.getText().toString().trim();


                        // check if the data that the user inserted is valid ot not.
                        // if it is valid, start inset it in the firebase.
                        if (itemName.isEmpty()) {
                            itemNameEditText.setError("Enter item name first");
                            itemNameEditText.requestFocus();
                        } else if (itemsNumberAsString.isEmpty()) {
                            itemsNumberEditText.setError("Enter items number first");
                            itemsNumberEditText.requestFocus();
                        } else if (itemCostAsString.isEmpty()) {
                            itemCostEditText.setError("Enter the one item cost first");
                            itemCostEditText.requestFocus();
                        } else {
                            double itemsNumber = Double.parseDouble(itemsNumberAsString);
                            double itemCost = Double.parseDouble(itemCostAsString);
                            insertItemInFirebase(itemName, itemsNumber, itemCost);
                            dialog.dismiss();
                        }

                    }
                });

                dialog.show();




            }
        });

    }




    private void insertItemInFirebase(String itemName, double itemsNumber, double itemCost) {

        // get the current system time in second.
        long currentTime = (System.currentTimeMillis() / 1000);

        // the user id that will add the employee inside the firebase.
        String userId = mFirebaseUser.getUid();

        // set PaymentObject data and push it to the firebase.
        PaymentObject paymentObject = new PaymentObject();
        paymentObject.setName(itemName);
        paymentObject.setItemsNumber(itemsNumber);
        paymentObject.setCost(itemCost);
        paymentObject.setAddedDate(currentTime);
        paymentObject.setAddedBy(userId);


        // push the PaymentObject with unique id in income => {year} => {month} => others => items
        // and update allPayment value in income => {year} => {month} => others
        mOthersPaymentItemsReference.push().setValue(paymentObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {


                mOthersPaymentReference.child(Constants.INCOME_OTHERS_ALL_PAYMENT)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                double allPayment = 0.0;
                                try {
                                    allPayment = snapshot.getValue(Double.class);
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "Exception from try-catch block inside insertItemInFirebase method when get allPayment value");
                                }

                                mOthersPaymentReference.child(Constants.INCOME_OTHERS_ALL_PAYMENT)
                                        .setValue(allPayment + (itemCost * itemsNumber))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Toast.makeText(mContext, "Added Successfully", Toast.LENGTH_SHORT).show();

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




    private void attachAllPaymentsDatabaseReadListener() {

        if (mOtherPaymentAllPaymentChildEventListener == null) {
            mOtherPaymentAllPaymentChildEventListener = new ChildEventListener() {
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

            mOthersPaymentReference.addChildEventListener(mOtherPaymentAllPaymentChildEventListener);
        }

    }

    private void updateAllPaymentValue(DataSnapshot snapshot) {

        if (Constants.INCOME_OTHERS_ALL_PAYMENT.equals(snapshot.getKey())) {

            double allPayments = 0.0;
            try {

                allPayments = snapshot.getValue(Double.class);

            } catch (Exception e) {
                Log.i(LOG_TAG, "Exception from try-catch block inside updateLayout method when get PatientObject");
            }

            mAllOthersPaymentsTextView.setText(String.valueOf(allPayments));

        }

    }


    private void attachItemsDatabaseReadListener() {

        if (mOtherPaymentItemsChildEventListener == null) {
            mOtherPaymentItemsChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    updateItems(snapshot);

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

            mOthersPaymentItemsReference.addChildEventListener(mOtherPaymentItemsChildEventListener);
        }

    }

    private void updateItems(DataSnapshot snapshot) {

        PaymentObject paymentObject = snapshot.getValue(PaymentObject.class);

        if (paymentObject != null) {
            mOtherPaymentAdapter.add(paymentObject);
            mItemsIds.add(snapshot.getKey());
        }


    }

}