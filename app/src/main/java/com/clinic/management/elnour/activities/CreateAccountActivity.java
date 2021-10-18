package com.clinic.management.elnour.activities;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.UserObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;


public class CreateAccountActivity extends AppCompatActivity {


    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private EditText mRealNameEditText;
    private EditText mUserNameEditText;
    private EditText mEmailAddressEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private EditText mPhoneNumberEditText;
    private TextView mLoginInButton;
    private ProgressDialog mProgressDialog;
    private Dialog mVerificationPhoneNumber;

    private String mRealName = "";
    private String mUserName = "";
    private String mEmailAddress = "";
    private String mPassword = "";
    private String mConfirmPassword = "";
    private String mPhoneNumber = "";
    private String mUnixTime = "";
    private boolean hasVerifiedNumber = false;


    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // initialize the main context and shared preference.
        mContext = CreateAccountActivity.this;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        // initialize the views in the layout.
        mRealNameEditText = findViewById(R.id.activity_create_account_real_name);
        mUserNameEditText = findViewById(R.id.activity_create_account_user_name);
        mEmailAddressEditText = findViewById(R.id.activity_create_account_email);
        mPasswordEditText = findViewById(R.id.activity_create_account_password);
        mConfirmPasswordEditText = findViewById(R.id.activity_create_account_confirm_password);
        mPhoneNumberEditText = findViewById(R.id.activity_create_account_phone_number);
        mLoginInButton = findViewById(R.id.activity_create_account_log_in_button);


        mPhoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Log.i(LOG_TAG, "the Editable contains (" + s.toString() + ")");

                if(!s.toString().startsWith("+20 ") ) {

                    if (s.toString().startsWith("+20")) {
                        s.insert(3, " ");
                    } else {
                        s.insert(0, "+20 ");
                    }

                }
            }
        });

        // initialize the firebase objects.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");


        // create out custom progress dialog.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.signing_up));
        mProgressDialog.setMessage(getString(R.string.create_new_account));
        mProgressDialog.setCanceledOnTouchOutside(false);


        // set what should happen when the sign up button clicked.
        setClickingOnCreateAccount();

        // set what should happen when the login button clicked.
        setClickingOnLogin();


        initializeCallback();


        initializeVerificationDialog();


    }

    private void initializeCallback() {

        // initialize what should happen when the verification code requested to send to the user(phone number).
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // will triggered when the verification code sent and the phone number was in the same
            // phone that the application installed in & and the google play services installed
            // in the phone then the it can completed the verification automatically.
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                firebaseSingUp(phoneAuthCredential);

            }

            // will triggered when the
            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(CreateAccountActivity.this, "Invalid Phone Number, Please enter correct phone number", Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();

            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(CreateAccountActivity.this, "Code has been sent, please check it and verify...", Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();


            }
        };

    }


    private boolean hasValidateData() {


        // get the data from the editText fields
        mRealName = mRealNameEditText.getText().toString().trim();
        mEmailAddress = mEmailAddressEditText.getText().toString().trim();
        mUserName = mUserNameEditText.getText().toString().trim();
        mPassword = mPasswordEditText.getText().toString();
        mConfirmPassword = mConfirmPasswordEditText.getText().toString();
        mPhoneNumber = mPhoneNumberEditText.getText().toString().trim().replace(" ", "");

        Log.i(LOG_TAG, "the phone number that comes from the edit text is : " + mPhoneNumber);

        // check if the data in each field is valid or not.
        if (TextUtils.isEmpty(mRealName)) {

            mRealNameEditText.setError(getString(R.string.enter_your_name));
            mRealNameEditText.requestFocus();
            return false;

        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmailAddress).matches()) {

            mEmailAddressEditText.setError(getString(R.string.enter_valid_email_address));
            mEmailAddressEditText.requestFocus();
            return false;

        } else if (TextUtils.isEmpty(mUserName)) {

            mUserNameEditText.setError(getString(R.string.enter_user_name));
            mUserNameEditText.requestFocus();
            return false;

        } else if(!mUserName.matches(".*[a-z].*")) {

            mUserNameEditText.setError(getString(R.string.user_name_must_contains_character));
            mUserNameEditText.requestFocus();
            return false;

        } else if (TextUtils.isEmpty(mPassword)) {

            mPasswordEditText.setError(getString(R.string.enter_your_password));
            mPasswordEditText.requestFocus();
            return false;

        } else if (mPassword.length() < 8) {

            mPasswordEditText.setError(getString(R.string.password_can_not_be_less_than_8));
            mPasswordEditText.requestFocus();
            return false;

        } else if (!mConfirmPassword.equals(mPassword)) {

            mConfirmPasswordEditText.setError(getString(R.string.password_not_match));
            mConfirmPasswordEditText.requestFocus();
            return false;

        } else if (!mPhoneNumber.matches("^\\+20[0-9]{11}$")) {

            mPhoneNumberEditText.setError(getString(R.string.enter_valid_phone_number));
            mPhoneNumberEditText.requestFocus();
            return false;

        }

        // if all the check validation is correct then the method will return finally true.
        return true;

    }


    private void startSignUpProcesses() {

        mProgressDialog.show();

        // check if the email address or the user name or the phone number that the user entered is
        // already exist (set error message to make the user change it) or sign up with that data.
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean isValidEmailAddress = true;
                boolean isValidUserName = true;
                boolean isValidPhoneNumber = true;

                for(DataSnapshot ds : snapshot.getChildren()) {

                    String emailAddress = ds.child("userEmail").getValue(String.class);
                    String userName = ds.child("userName").getValue(String.class);
                    String phoneNumber = ds.child("userPhone").getValue(String.class);

                    if (mEmailAddress.equals(emailAddress)) {

                        isValidEmailAddress = false;

                    }
                    if (mUserName.equals(userName)) {

                        isValidUserName = false;

                    }
                    if (mPhoneNumber.equals(phoneNumber)) {

                        isValidPhoneNumber = false;

                    }

                }

                if (!isValidEmailAddress) {

                    mProgressDialog.dismiss();
                    mEmailAddressEditText.setError(getString(R.string.email_address_already_exist));
                    mEmailAddressEditText.requestFocus();

                } else if (!isValidUserName) {

                    mProgressDialog.dismiss();
                    mUserNameEditText.setError(getString(R.string.user_name_already_exist));
                    mUserNameEditText.requestFocus();

                } else if (!isValidPhoneNumber) {

                    mProgressDialog.dismiss();
                    mPhoneNumberEditText.setError(getString(R.string.phone_number_already_exist));
                    mPhoneNumberEditText.requestFocus();

                } else {

                    mVerificationPhoneNumber.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mFirebaseAuth)
                                    .setPhoneNumber(mPhoneNumber)              // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
                                    .setActivity(CreateAccountActivity.this)    // Activity (for callback binding)
                                    .setCallbacks(callbacks)                    // OnVerificationStateChangedCallbacks
                                    .build();

                    PhoneAuthProvider.verifyPhoneNumber(options);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }



    private void firebaseSingUp(PhoneAuthCredential phoneAuthCredential) {

        mFirebaseAuth.createUserWithEmailAndPassword(mEmailAddress, mPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                // get the current unix time from the system.
                mUnixTime = String.valueOf(System.currentTimeMillis());

                UserObject userObject = new UserObject(mRealName, mUserName, mEmailAddress,
                        mPhoneNumber, mUnixTime, false,false, false);

                FirebaseDatabase.getInstance().getReference("users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(userObject).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            editor.putString(getString(R.string.user_id_Key), userId);
                            editor.putString(getString(R.string.real_name_Key), mRealName);
                            editor.putString(getString(R.string.user_name_Key), mUserName);
                            editor.putString(getString(R.string.email_address_Key), mEmailAddress);
                            editor.putString(getString(R.string.phone_number_Key), mPhoneNumber);
                            editor.putString(getString(R.string.user_join_unix_time_key), mUnixTime);
                            editor.putBoolean(getString(R.string.verified_number_Key), false);
                            editor.putBoolean(getString(R.string.account_disable_Key), false);
                            editor.putBoolean(getString(R.string.account_admin_Key), false);
                            editor.apply();


                            addPhoneNumberToSignInProvider(firebaseUser, phoneAuthCredential);


                            mProgressDialog.dismiss();

                        } else {

                            Toast.makeText(CreateAccountActivity.this, "فشل إنشاء الحساب", Toast.LENGTH_SHORT).show();

                        }


                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(mContext, "فشل انشاء الحساب", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void addPhoneNumberToSignInProvider(FirebaseUser firebaseUser, PhoneAuthCredential phoneAuthCredential) {

        firebaseUser.updatePhoneNumber(phoneAuthCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                editor.putBoolean(getString(R.string.verified_number_Key), true);

                Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();


            }
        }) .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                Toast.makeText(mContext, "phone number not verified", Toast.LENGTH_SHORT).show();

            }
        });

    }


    private void initializeVerificationDialog() {


        mVerificationPhoneNumber = new Dialog(mContext);
        mVerificationPhoneNumber.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mVerificationPhoneNumber.setContentView(R.layout.dialog_verification_code);
        mVerificationPhoneNumber.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        mVerificationPhoneNumber.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText verificationField = mVerificationPhoneNumber.findViewById(R.id.dialog_verification_code_field);
        TextView okButton = mVerificationPhoneNumber.findViewById(R.id.dialog_verification_code_confirm_button);


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String verificationCode = verificationField.getText().toString().trim();

                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(CreateAccountActivity.this, "Please write verification code first...", Toast.LENGTH_SHORT).show();
                } else {
                    mProgressDialog.setTitle("Verification Code");
                    mProgressDialog.setMessage("Please wait, while we are verifying verification code...");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    Log.i(LOG_TAG, "the Credential Object that used for signing in is : " + credential);

                    firebaseSingUp(credential);


                }

            }
        });


    }


    private void setClickingOnLogin() {

        TextView textView = findViewById(R.id.activity_create_account_log_in_button);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();

            }
        });

    }


    private void setClickingOnCreateAccount() {

        TextView textView = findViewById(R.id.activity_create_account_create_account);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasValidateData()) {
                    startSignUpProcesses();
                }

            }
        });

    }





}