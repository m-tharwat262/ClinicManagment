package com.clinic.management.elnour.fragments.patients;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.clinic.management.elnour.Constants;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.activities.display.EmployeeDetailsActivity;
import com.clinic.management.elnour.activities.display.PatientDetailsActivity;
import com.clinic.management.elnour.adapters.EmployeeAdapter;
import com.clinic.management.elnour.adapters.PatientAdapter;
import com.clinic.management.elnour.fragments.employees.EmployeeFragment;
import com.clinic.management.elnour.models.EmployeeObject;
import com.clinic.management.elnour.models.PatientObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class PatientsFragment extends Fragment {

    private static final String LOG_TAG = PatientsFragment.class.getSimpleName();
    private Context mContext;

    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mPatientDatabaseReference;
    private ChildEventListener mChildEventListener;

    private View mMainView;
    private TextView mTitleTextView;
    private GridView mGridView;
    private LinearLayout mFloatingActionButton;

    private PatientAdapter mPatientAdapter;

    private boolean doubleBackToExitPressedOnce = false;

    private ArrayList<String> mPatientIds = new ArrayList<>();


    public PatientsFragment(Context context) {
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
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // initialize firebase objects.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mPatientDatabaseReference = mFirebaseDatabase.getReference().child(Constants.PATIENTS_NODE);
        mPatientDatabaseReference.keepSynced(true);


        // initialize the views from the main layout.
        mTitleTextView = mMainView.findViewById(R.id.fragment_employee_title);
        mGridView = mMainView.findViewById(R.id.fragment_employee_grid_view);
        mFloatingActionButton = mMainView.findViewById(R.id.fragment_employee_floating_action_button);


        mTitleTextView.setText(R.string.bottom_items_patients);

        // set the adapter to display the items inside the GridView.
        ArrayList<PatientObject> patientObjects = new ArrayList<>();
        mPatientAdapter = new PatientAdapter(mContext, patientObjects);
        mGridView.setAdapter(mPatientAdapter);


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

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // send the user to PatientDetailsActivity to display all the employee info.
                Intent intent = new Intent(mContext, PatientDetailsActivity.class);
                intent.putExtra("patient_id", mPatientIds.get(position));
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

                // create and display a dialog for add a new patient.
                Dialog dialog = new Dialog(mContext);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_add_patient);
                dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                // initialize views inside the dialog layout.
                EditText patientNameEditText = dialog.findViewById(R.id.dialog_add_patient_name);
                EditText patientAgeEditText = dialog.findViewById(R.id.dialog_add_patient_age);
                EditText patientDiseaseEditText = dialog.findViewById(R.id.dialog_add_patient_disease);
                EditText sessionCostEditText = dialog.findViewById(R.id.dialog_add_patient_session_cost);
                EditText phoneNumberEditText = dialog.findViewById(R.id.dialog_add_patient_phone_number);
                TextView addPatientButton = dialog.findViewById(R.id.dialog_add_patient_add_button);


                // handle clicking on the add button.
                addPatientButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // get the data from the views.
                        String patientName = patientNameEditText.getText().toString().trim();
                        String patientAge = patientAgeEditText.getText().toString().trim();
                        String patientDisease = patientDiseaseEditText.getText().toString().trim();
                        String sessionCost = sessionCostEditText.getText().toString().trim();
                        String phoneNumber = phoneNumberEditText.getText().toString().trim();


                        // check if the data that the user inserted is valid ot not.
                        // if it is valid, start inset it in the firebase.
                        if (patientName.isEmpty()) {
                            patientNameEditText.setError("Enter name first");
                            patientNameEditText.requestFocus();
                        } else if (patientAge.isEmpty()) {
                            patientAgeEditText.setError("Enter age first");
                            patientAgeEditText.requestFocus();
                        } else if (patientDisease.isEmpty()) {
                            patientDiseaseEditText.setError("Enter disease first");
                            patientDiseaseEditText.requestFocus();
                        } else if (sessionCost.isEmpty()) {
                            sessionCostEditText.setError("Enter session cost first");
                            sessionCostEditText.requestFocus();
                        } else if (Double.parseDouble(sessionCost) < 0) {
                            sessionCostEditText.setError("session cost negative!");
                            sessionCostEditText.requestFocus();
                        } else if (phoneNumber.isEmpty()) {
                            phoneNumberEditText.setError("Enter phone number first");
                            phoneNumberEditText.requestFocus();
                        } else if (phoneNumber.length() != 11) {
                            phoneNumberEditText.setError("Enter valid phone number first");
                            phoneNumberEditText.requestFocus();
                        } else {
                            double patientAgeInt = Double.parseDouble(patientAge);
                            double sessionCostDouble = Double.parseDouble(sessionCost);
                            insertPatientInFirebase(patientName, patientAgeInt, patientDisease,
                                    sessionCostDouble, phoneNumber);
                            dialog.dismiss();
                        }

                    }
                });

                dialog.show();


            }
        });


    }

    /**
     * Insert a new patient data as object with unique id inside the firebase.
     *
     * @param patientName the patient name.
     * @param patientAge the patient age.
     * @param patientDisease the patient disease.
     * @param sessionCostDouble the session cost for the patient.
     * @param phoneNumber the patient phone number.
     */
    private void insertPatientInFirebase(String patientName, double patientAge,String patientDisease,
                                         double sessionCostDouble, String phoneNumber) {

        // get the current system time in second.
        long currentTime = (System.currentTimeMillis() / 1000);

        // the user id that will add the employee inside the firebase.
        String userId = mFirebaseUser.getUid();

        // set PatientObject data and push it to the firebase.
        PatientObject patientObject = new PatientObject();
        patientObject.setName(patientName);
        patientObject.setAge(patientAge);
        patientObject.setDisease(patientDisease);
        patientObject.setSessionCost(sessionCostDouble);
        patientObject.setPhoneNumber(phoneNumber);
        patientObject.setAddedTime(currentTime);
        patientObject.setAddedBy(userId);

        mPatientDatabaseReference.push().setValue(patientObject);


    }

    /**
     * Add listener to a specific node (patients) in the firebase to make app up to data inside that node
     * and make tha app display any changes happen in that node.
     */
    private void attachDatabaseReadListener() {

        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String previousChildName) {

                    // get the data from the snapshot as an PatientObject and add it to the
                    // the adapter to display it on the screen.
                    PatientObject patientObject = snapshot.getValue(PatientObject.class);
                    mPatientAdapter.add(patientObject);
                    mPatientIds.add(snapshot.getKey());

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

            mPatientDatabaseReference.addChildEventListener(mChildEventListener);
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