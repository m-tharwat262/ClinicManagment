package com.clinic.management.elnour.activities.display;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.adapters.SalaryAdapter;
import com.clinic.management.elnour.fragments.countings.CountingFragment;
import com.clinic.management.elnour.models.SalaryObject;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class SalariesActivity extends AppCompatActivity {

    private static final String LOG_TAG = SalariesActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String mYearNumber;
    private String mMonthName;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mYearReference;
    private DatabaseReference mMonthReference;
    private DatabaseReference mSalariesReference;
    private ChildEventListener mChildEventListener;

    private TextView mSaveButton;
    private ListView mListView;
    private SalaryAdapter mSalaryAdapter;

    private ArrayList<String> mEmployeeIds = new ArrayList<>();
    private ArrayList<Boolean> mGotSalaries = new ArrayList<>();
    private ArrayList<Double> mSalaries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salaries);

        mContext = SalariesActivity.this;

        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mYearNumber = pref.getString(getString(R.string.year_number_key), getString(R.string.preference_string_empty_value));
        mMonthName = pref.getString(getString(R.string.month_name_key), getString(R.string.preference_string_empty_value));

        mSaveButton = findViewById(R.id.activity_salaries_save_button);
        mListView = findViewById(R.id.activity_salaries_list_view);


        ArrayList<SalaryObject> salaryObjects = new ArrayList<>();
        mSalaryAdapter = new SalaryAdapter(mContext, salaryObjects);
        mListView.setAdapter(mSalaryAdapter);

        // initialize firebase objects.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child(Constants.INCOME_NODE);
        mDatabaseReference.keepSynced(true);
        mYearReference = mDatabaseReference.child(mYearNumber);
        mMonthReference = mYearReference.child(mMonthName);
        mSalariesReference = mMonthReference.child(Constants.INCOME_SALARIES);


        // set listener to the income node inside firebase.
        attachDatabaseReadListener();


        setClickingOnSaveButton();

    }

    private void setClickingOnSaveButton() {

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateFirebase();

            }
        });

    }

    private void updateFirebase() {

        ArrayList<Boolean> checkedItemsFromAdapter = mSalaryAdapter.getItemChecked();

        mSalariesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                double allSalariesNotPaid = 0.0;
                try {
                    allSalariesNotPaid = snapshot.child(Constants.INCOME_SALARIES_ALL_SALARIES_NOT_PAID).getValue(Double.class);
                } catch (Exception e) {
                    Log.i(LOG_TAG, "Exception from try-catch block inside updateFirebase method when get income allSalariesNotPaid");
                }


                double salariesWillPaid = 0.0;
                double salariesWillNotPaid = 0.0;
                for(int i = 0 ; i < checkedItemsFromAdapter.size() ; i++) {


                    // to determine if the income will increase or decrease.
//                    boolean isIncomeDecreasing;


                    if (checkedItemsFromAdapter.get(i) != mGotSalaries.get(i)) {


                        // add the time that the employee get his salary and the user id that give
                        // it to him
                        if (!mGotSalaries.get(i)) {

                            // the employee get his salary so :

                            // detect the date that the employee got his salary at.
                            mSalariesReference.child(mEmployeeIds.get(i)).child(Constants.INCOME_SALARIES_GOT_SALARY_DATE)
                                    .setValue((System.currentTimeMillis() / 1000));

                            // detect the user that change the employee gotSalary state (from false to true).
                            mSalariesReference.child(mEmployeeIds.get(i)).child(Constants.INCOME_SALARIES_GOT_SALARY_BY)
                                    .setValue(mFirebaseUser.getUid());


                            // update allSalariesNotPaid value if the user select employee or more to pay their
                            // salaries.
                            salariesWillPaid += mSalaries.get(i);
//                            mSalariesReference.child(Constants.INCOME_SALARIES_ALL_SALARIES_NOT_PAID)
//                                    .setValue(allSalariesNotPaid - decreaseSalaries);


//                            isIncomeDecreasing = true;

                        } else {

                            // the employee not get his salary so :

                            // make the date that the employee received his salary at is -1.
                            mSalariesReference.child(mEmployeeIds.get(i)).child(Constants.INCOME_SALARIES_GOT_SALARY_DATE)
                                    .setValue(-1);

                            // update allSalariesNotPaid value if the user select employee or more to pay their
                            // salaries.
                            salariesWillNotPaid += mSalaries.get(i);
//                            mSalariesReference.child(Constants.INCOME_SALARIES_ALL_SALARIES_NOT_PAID)
//                                    .setValue(allSalariesNotPaid + increaseSalaries);

//                            isIncomeDecreasing = false;
                        }




                        // to use the variable from loop inside the abstract methods.
                        int position = i;

                        // update the user gotSalary state (true - false).
                        // and the global variable mGotSalaries.
                        mSalariesReference.child(mEmployeeIds.get(i)).child(Constants.INCOME_SALARIES_GOT_SALARY)
                                .setValue(checkedItemsFromAdapter.get(i)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                mGotSalaries.set(position, checkedItemsFromAdapter.get(position));
//                                Toast.makeText(mContext, "Saved Successfully", Toast.LENGTH_SHORT).show();

                            }
                        });


//                        // change the income value in specific year.
//                        mYearReference.child(Constants.INCOME_INCOME).
//                                addListenerForSingleValueEvent(new ValueEventListener() {
//                                    @Override
//                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                        double yearIncome = 0.0;
//                                        try {
//                                            yearIncome = snapshot.getValue(Double.class);
//                                        } catch (Exception e) {
//                                            Log.i(LOG_TAG, "Exception from try-catch block inside updateFirebase method when get income value");
//                                        }
//
//                                        // create a new
//                                        double newYearIncomeValue;
//                                        if (isIncomeDecreasing) {
//                                            newYearIncomeValue = yearIncome - mSalaries.get(position);
//                                        } else {
//                                            newYearIncomeValue = yearIncome + mSalaries.get(position);
//                                        }
//
//                                        mYearReference.child(Constants.INCOME_INCOME).setValue(newYearIncomeValue);
//
//                                    }
//
//                                    @Override
//                                    public void onCancelled(@NonNull DatabaseError error) {
//
//                                    }
//                                });
//
//
//
//
//
//                        // change the income value in specific month.
//                        mMonthReference.child(Constants.INCOME_INCOME).
//                                addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                                double monthIncome = 0.0;
//                                try {
//                                    monthIncome = snapshot.getValue(Double.class);
//                                } catch (Exception e) {
//                                    Log.i(LOG_TAG, "Exception from try-catch block inside updateFirebase method when get income value");
//                                }
//
//
//                                double newMonthIncomeValue;
//                                if (isIncomeDecreasing) {
//                                    newMonthIncomeValue = monthIncome - mSalaries.get(position);
//                                } else {
//                                    newMonthIncomeValue = monthIncome + mSalaries.get(position);
//                                }
//
//                                mMonthReference.child(Constants.INCOME_INCOME).setValue(newMonthIncomeValue);
//
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//
//                            }
//                        });

                    }

                }



                // final values for the salaries that will be paid and not paid after finishing the
                // loop above.
                double salariesWillPaidAfterLoop = salariesWillPaid;
                double salariesWillNotPaidAfterLoop = salariesWillNotPaid;




                mSalariesReference.child(Constants.INCOME_SALARIES_ALL_SALARIES_NOT_PAID)
                                    .setValue(allSalariesNotPaid + salariesWillNotPaidAfterLoop - salariesWillPaidAfterLoop);


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

                                // determine the new income value for the year.
                                double newYearIncomeValue = yearIncome - salariesWillPaidAfterLoop + salariesWillNotPaidAfterLoop;

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


                                // determine the new income value for the month.
                                double newMonthIncomeValue = monthIncome - salariesWillPaidAfterLoop + salariesWillNotPaidAfterLoop;


                                mMonthReference.child(Constants.INCOME_INCOME).setValue(newMonthIncomeValue);

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


    private void attachDatabaseReadListener() {

        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    updateLayout(snapshot);


                }
                @Override
                public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

                    updateItem0(snapshot);

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
            mSalariesReference.addChildEventListener(mChildEventListener);
        }

    }


    private void updateLayout(DataSnapshot snapshot) {

        try {
            SalaryObject salaryObject = snapshot.getValue(SalaryObject.class);
            if (salaryObject != null) {
                mSalaryAdapter.addItem(salaryObject);
                mEmployeeIds.add(snapshot.getKey());
                mGotSalaries.add(salaryObject.isGotSalary());
                mSalaries.add(salaryObject.getSalary());
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception from try-catch block inside updateLayout method when get SalaryObject");
        }

    }


    private void updateItem0(DataSnapshot snapshot) {


        try {

            SalaryObject salaryObject = snapshot.getValue(SalaryObject.class);
            if (salaryObject != null) {

                int position = mEmployeeIds.indexOf(snapshot.getKey());
                mSalaryAdapter.remove(mSalaryAdapter.getItem(position));
                mSalaryAdapter.insertItem(salaryObject, position);


                mGotSalaries.set(position, salaryObject.isGotSalary());
                mSalaries.set(position, salaryObject.getSalary());

            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception from try-catch block inside updateItem method when get SalaryObject");
        }



    }

    private void updateItem(DataSnapshot snapshot) {


        try {
            SalaryObject salaryObject = snapshot.getValue(SalaryObject.class);
            if (salaryObject != null) {

                int position = mEmployeeIds.indexOf(snapshot.getKey());
                mSalaryAdapter.insertItem(salaryObject, position);

                mEmployeeIds.add(snapshot.getKey());
                mGotSalaries.add(position, salaryObject.isGotSalary());
                mSalaries.add(salaryObject.getSalary());
            }

        } catch (Exception e) {
            Log.i(LOG_TAG, "Exception from try-catch block inside updateLayout method when get SalaryObject");
        }



    }



}