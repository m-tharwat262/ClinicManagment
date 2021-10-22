package com.clinic.management.elnour.fragments.employees;

import android.app.Dialog;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.activities.display.EmployeeDetailsActivity;
import com.clinic.management.elnour.adapters.EmployeeAdapter;
import com.clinic.management.elnour.models.EmployeeObject;
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

public class EmployeeFragment extends Fragment {


    private static final String LOG_TAG = EmployeeFragment.class.getSimpleName();
    private Context mContext;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mEmployeeDatabaseReference;
    private ChildEventListener mChildEventListener;

    private View mMainView;
    private GridView mGridView;
    private LinearLayout mFloatingActionButton;

    private EmployeeAdapter mEmployeeAdapter;

    private boolean doubleBackToExitPressedOnce = false;

    private ArrayList<String> mEmployeeIds = new ArrayList<>();


    public EmployeeFragment(Context context) {
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
        mMainView = inflater.inflate(R.layout.fragment_employee, container, false);

        // initialize firebase objects.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mEmployeeDatabaseReference = mFirebaseDatabase.getReference().child(Constants.EMPLOYEES_NODE);
        mEmployeeDatabaseReference.keepSynced(true);

        // initialize the views from the main layout.
        mGridView = mMainView.findViewById(R.id.fragment_employee_grid_view);
        mFloatingActionButton = mMainView.findViewById(R.id.fragment_employee_floating_action_button);


        // set the adapter to display the items inside the GridView.
        ArrayList<EmployeeObject> employeeObjects = new ArrayList<>();
        mEmployeeAdapter = new EmployeeAdapter(mContext, employeeObjects);
        mGridView.setAdapter(mEmployeeAdapter);


        // set listener to the employee node inside firebase.
        attachDatabaseReadListener();

        // handle clicking on the floating action button.
        setClickingOnFloatingButton();

        // handle clicking on the items inside the GridView.
        setClickingOnItems();


        // handle the back pressed button.
        controlBackPressedButton();


        // Inflate the layout for this fragment
        return mMainView;
    }


    /**
     * Handle Clicking on items inside the GridView.
     */
    private void setClickingOnItems() {

        // send the user to EmployeeDetailsActivity to display all the employee info.
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(mContext, EmployeeDetailsActivity.class);
                intent.putExtra("employee_id", mEmployeeIds.get(position));
                startActivity(intent);

            }
        });

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
                dialog.setContentView(R.layout.dialog_add_employee);
                dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                // initialize views inside the dialog layout.
                EditText employeeNameEditText = dialog.findViewById(R.id.dialog_add_employee_name);
                EditText employeeJobEditText = dialog.findViewById(R.id.dialog_add_employee_job);
                EditText sessionCostEditText = dialog.findViewById(R.id.dialog_add_employee_session_cost);
                EditText phoneNumberEditText = dialog.findViewById(R.id.dialog_add_employee_phone_number);
                TextView addEmployeeButton = dialog.findViewById(R.id.dialog_add_employee_add_button);


                // handle clicking on the add button.
                addEmployeeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // get the data from the views.
                        String employeeName = employeeNameEditText.getText().toString().trim();
                        String employeeJob = employeeJobEditText.getText().toString().trim();
                        String sessionCost = sessionCostEditText.getText().toString().trim();
                        String phoneNumber = phoneNumberEditText.getText().toString().trim();


                        // check if the data that the user inserted is valid ot not.
                        // if it is valid, start inset it in the firebase.
                        if (employeeName.isEmpty()) {
                            employeeNameEditText.setError("Enter name first");
                            employeeNameEditText.requestFocus();
                        } else if (sessionCost.isEmpty()) {
                            sessionCostEditText.setError("Enter session cost first");
                            sessionCostEditText.requestFocus();
                        } else if (Double.parseDouble(sessionCost) < 0) {
                            sessionCostEditText.setError("session cost negative!");
                            sessionCostEditText.requestFocus();
                        } else if (phoneNumber.isEmpty()) {
                            phoneNumberEditText.setError("Enter phone number first");
                            phoneNumberEditText.requestFocus();
                        } else if (employeeJob.isEmpty()) {
                            phoneNumberEditText.setError("Enter the job first");
                            phoneNumberEditText.requestFocus();
                        } else if (phoneNumber.length() != 11) {
                            phoneNumberEditText.setError("Enter valid phone number first");
                            phoneNumberEditText.requestFocus();
                        } else {
                            double sessionCostDouble = Double.parseDouble(sessionCost);
                            insertEmployeeInFirebase(employeeName, employeeJob, sessionCostDouble, phoneNumber);
                            dialog.dismiss();
                        }

                    }
                });

                dialog.show();


            }
        });


    }

    /**
     * Insert a new employee data inside the firebase.
     *
     * @param employeeName the employee name.
     * @param employeeJob the employee job.
     * @param sessionCostDouble the session cost
     * @param phoneNumber the employee phone number.
     */
    private void insertEmployeeInFirebase(String employeeName, String employeeJob, double sessionCostDouble, String phoneNumber) {

        // get the current system time in second.
        long currentTime = (System.currentTimeMillis() / 1000);

        // the user id that will add the employee inside the firebase.
        String userId = mFirebaseUser.getUid();

        // set EmployeeObject data and push it to the firebase.
        EmployeeObject employeeObject = new EmployeeObject();
        employeeObject.setName(employeeName);
        employeeObject.setJob(employeeJob);
        employeeObject.setSessionCost(sessionCostDouble);
        employeeObject.setPhoneNumber(phoneNumber);
        employeeObject.setAddedTime(currentTime);
        employeeObject.setAddedBy(userId);

        mEmployeeDatabaseReference.push().setValue(employeeObject);

    }


    /**
     * Add listener to a specific node (employees) in the firebase to make app up to data inside that node
     * and make tha app display any changes happen in that node.
     */
    private void attachDatabaseReadListener() {

        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    // get the data from the snapshot as an EmployeeObject and add it to the
                    // the adapter to display it on the screen.
                    EmployeeObject employeeObject = snapshot.getValue(EmployeeObject.class);
                    mEmployeeAdapter.add(employeeObject);
                    mEmployeeIds.add(snapshot.getKey());

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

            mEmployeeDatabaseReference.addChildEventListener(mChildEventListener);
        }

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
