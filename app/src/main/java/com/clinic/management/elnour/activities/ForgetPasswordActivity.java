package com.clinic.management.elnour.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import com.clinic.management.elnour.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgetPasswordActivity extends AppCompatActivity {

    private static final String LOG_TAG = ForgetPasswordActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;

    private String mEmailOrPhoneNumber = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);


        mContext = ForgetPasswordActivity.this;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();


        mFirebaseAuth = FirebaseAuth.getInstance();


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("من فضلك انتظر");
        mProgressDialog.setMessage("جاري إعادة تعيين كلمة المرور ...");
        mProgressDialog.setCanceledOnTouchOutside(false);


        setClickingOnCreateAccount();


        setClickingOnChangePassword();


    }


    private void setClickingOnCreateAccount() {

        TextView textView = findViewById(R.id.activity_forget_password_create_account);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ForgetPasswordActivity.this, CreateAccountActivity.class);
                startActivity(intent);
                finish();


            }
        });

    }


    private void setClickingOnChangePassword() {

        TextView textView = findViewById(R.id.activity_forget_password_change_password_button);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                resetPassword();

            }
        });

    }

    private void resetPassword() {

        EditText emailEditText = findViewById(R.id.activity_forget_password_edit_text);

        mEmailOrPhoneNumber = emailEditText.getText().toString().trim();

        if (mEmailOrPhoneNumber.isEmpty()) {

            emailEditText.setError("ادخل البريد الإلكنروني او رقم الهاتف");
            emailEditText.requestFocus();

        } else if (mEmailOrPhoneNumber.matches("^[0-9]{11}$")) {

            String phoneNumberWithCountryCode = "+20" + mEmailOrPhoneNumber;
            Intent intent = new Intent(ForgetPasswordActivity.this, ResetPasswordByPhoneActivity.class);
            intent.putExtra("phone_number", phoneNumberWithCountryCode);
            startActivity(intent);
            finish();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(mEmailOrPhoneNumber).matches()) {

            emailEditText.setError("بريد إلكتروني غير صالح");
            emailEditText.requestFocus();

        } else {

            mProgressDialog.show();
            mFirebaseAuth.sendPasswordResetEmail(mEmailOrPhoneNumber).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        mProgressDialog.dismiss();
                        showResetDialog();

                    } else {
                        mProgressDialog.dismiss();
                        Toast.makeText(ForgetPasswordActivity.this, "فشل إعادة تعيين كلمة المرور", Toast.LENGTH_SHORT).show();
                    }


                }
            });

        }


    }

    private void showResetDialog() {


        Dialog dialog = new Dialog(ForgetPasswordActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_send_change_password);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView okButton = dialog.findViewById(R.id.dialog_send_change_password_oK_button);


        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                finish();

            }
        });

        dialog.show();

    }





}