package com.clinic.management.elnour.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.clinic.management.elnour.R;
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

import java.util.concurrent.TimeUnit;

public class ResetPasswordByPhoneActivity extends AppCompatActivity {


    private static final String LOG_TAG = ForgetPasswordActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private EditText mVerificationCodeEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private TextView mResetPasswordButton;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;

    private String mPhoneNumber;

    private String mVerificationCode;
    private String mNewPassword;
    private String mConfirmNewPassword;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_by_phone);


        mContext = ResetPasswordByPhoneActivity.this;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mVerificationCodeEditText = findViewById(R.id.activity_reset_password_by_phone_verification_code);
        mPasswordEditText = findViewById(R.id.activity_reset_password_by_phone_new_password);
        mConfirmPasswordEditText = findViewById(R.id.activity_reset_password_by_phone_confirm_new_password);
        mResetPasswordButton = findViewById(R.id.activity_reset_password_by_phone_reset_button);



        // create out custom progress dialog.
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.signing_up));
        mProgressDialog.setMessage(getString(R.string.create_new_account));
        mProgressDialog.setCanceledOnTouchOutside(false);


        mFirebaseAuth = FirebaseAuth.getInstance();

        mPhoneNumber = getIntent().getStringExtra("phone_number");


        initializeCallback();


        sendVerificationCodeToUser();


        setClickingOnResetPasswordButton();




    }



    private void sendVerificationCodeToUser() {

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mFirebaseAuth)
                        .setPhoneNumber(mPhoneNumber)              // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS)  // Timeout and unit
                        .setActivity(ResetPasswordByPhoneActivity.this)    // Activity (for callback binding)
                        .setCallbacks(callbacks)                    // OnVerificationStateChangedCallbacks
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

    }


    private boolean hasValidData() {

        mVerificationCode = mVerificationCodeEditText.getText().toString().trim();
        mNewPassword = mPasswordEditText.getText().toString();
        mConfirmNewPassword = mConfirmPasswordEditText.getText().toString();

        if (mVerificationCode.isEmpty()) {

            mVerificationCodeEditText.setError(getString(R.string.enter_your_verification_code));
            mVerificationCodeEditText.requestFocus();
            return false;

        } else if(mVerificationCode.length() != 6) {

            mVerificationCodeEditText.setError(getString(R.string.verification_code_not_correct));
            mVerificationCodeEditText.requestFocus();
            return false;

        } else if (TextUtils.isEmpty(mNewPassword)) {

            mPasswordEditText.setError(getString(R.string.enter_your_password));
            mPasswordEditText.requestFocus();
            return false;

        } else if (mNewPassword.length() < 8) {

            mPasswordEditText.setError(getString(R.string.password_can_not_be_less_than_8));
            mPasswordEditText.requestFocus();
            return false;

        } else if (!mConfirmNewPassword.equals(mNewPassword)) {

            mConfirmPasswordEditText.setError(getString(R.string.password_not_match));
            mConfirmPasswordEditText.requestFocus();
            return false;

        }

        return true;

    }

    private void setClickingOnResetPasswordButton() {

        mResetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(hasValidData()) {
                    startChangingPasswordProcesses();
                }

            }
        });

    }

    private void startChangingPasswordProcesses() {

        // create out custom progress dialog.
        mProgressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mVerificationCode);

        mFirebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        updatePassword();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mProgressDialog.dismiss();
                        Toast.makeText(mContext, "Something worn, try again later!", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void initializeCallback() {

        // initialize what should happen when the verification code requested to send to the user(phone number).
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // will triggered when the verification code sent and the phone number was in the same
            // phone that the application installed in & and the google play services installed
            // in the phone then the it can completed the verification automatically.
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                String smsCode = phoneAuthCredential.getSmsCode();
                mVerificationCodeEditText.setText(smsCode);

            }

            // will triggered when the
            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(ResetPasswordByPhoneActivity.this, "Invalid Phone Number, Please enter correct phone number", Toast.LENGTH_LONG).show();
                mProgressDialog.dismiss();

            }

            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(ResetPasswordByPhoneActivity.this, "Code has been sent, please check it and verify...", Toast.LENGTH_SHORT).show();

            }
        };

    }


    private void updatePassword() {

        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {


            firebaseUser.updatePassword(mNewPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {

                                mProgressDialog.dismiss();
                                Toast.makeText(mContext, "تم تغيير كلمة المرور بنجاح", Toast.LENGTH_SHORT).show();
                            }

                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mProgressDialog.dismiss();
                            Toast.makeText(mContext, "حدث خطأ حاول مره اخري", Toast.LENGTH_SHORT).show();

                        }
                    });


        }

    }



}