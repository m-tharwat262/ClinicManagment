package com.clinic.management.elnour.activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.clinic.management.elnour.R;
import com.clinic.management.elnour.models.UserObject;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;


public class LoginActivity extends AppCompatActivity {


    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    private Context mContext;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private TextView mCreateAccountButton;
    private TextView mForgetPasswordButton;
    private EditText mUserNameEditText;
    private EditText mPasswordEditText;
    private ProgressDialog mProgressDialog;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private String mEmailOrUserNameOrPhoneNumber = "";
    private String mPassword = "";

    private String mUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if the user is already login or not before continue to execute the rest of code.
        mFirebaseAuth = FirebaseAuth.getInstance();
        checkUserAlreadyLoggedIn();


        mContext = LoginActivity.this;
        pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = pref.edit();

        mUserNameEditText = findViewById(R.id.activity_login_user_name);
        mPasswordEditText = findViewById(R.id.activity_login_user_password);
        mCreateAccountButton = findViewById(R.id.activity_login_create_account);
        mForgetPasswordButton = findViewById(R.id.activity_login_forget_password);


        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setTitle("انتظر من فضلك");
        mProgressDialog.setMessage("جار تسجيل الدخول ...");
        mProgressDialog.setCanceledOnTouchOutside(false);


        setClickingOnLogin();

        setClickingOnForgetPassword();

        setClickingOnCreateAccount();

    }


    /**
     * Check if the user is already login or not.
     */
    private void checkUserAlreadyLoggedIn() {

        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

        if(firebaseUser != null) {

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }

    }

    private boolean hasValidData() {

        // get data
        mEmailOrUserNameOrPhoneNumber = mUserNameEditText.getText().toString().trim();
        mPassword = mPasswordEditText.getText().toString();


        if (TextUtils.isEmpty(mEmailOrUserNameOrPhoneNumber)) {

            mUserNameEditText.setError("ادخل اسم المسخدم او ابريد الإلكتروني اولا");
            return false;

        } else if (TextUtils.isEmpty(mPassword)) {

            mPasswordEditText.setError("ادخل كلمة المرور");
            return false;

        }

        return true;

    }


    private void LoginWithEmailOrUserName() {

        firebaseLogin(mEmailOrUserNameOrPhoneNumber, mPassword);

        if(Patterns.EMAIL_ADDRESS.matcher(mEmailOrUserNameOrPhoneNumber).matches()) {

            firebaseLogin(mEmailOrUserNameOrPhoneNumber, mPassword);

        } else if(mEmailOrUserNameOrPhoneNumber.matches("^[0-9]{11}$")) {

            tryLoginWithPhoneNumber();

        } else {

            tryLoginWithUserName();

        }
    }

    private void tryLoginWithPhoneNumber() {

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String email = "";

                for(DataSnapshot ds : snapshot.getChildren()) {

                    String userName = ds.child("userPhone").getValue(String.class);

                    String phoneNumber = "+20" + mEmailOrUserNameOrPhoneNumber;

                    if (phoneNumber.equals(userName)) {

                        email = ds.child("userEmail").getValue(String.class);

                    }

                }


                if (email.isEmpty()) {
                    mUserNameEditText.setError("رقم الهاتف غير صحيح");
                    mUserNameEditText.requestFocus();

                } else {

                    firebaseLogin(email, mPassword);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }

    private void tryLoginWithUserName() {

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String email = "";

                for(DataSnapshot ds : snapshot.getChildren()) {

                    String userName = ds.child("userName").getValue(String.class);

                    if (mEmailOrUserNameOrPhoneNumber.equals(userName)) {

                        email = ds.child("userEmail").getValue(String.class);

                    }

                }


                if (email.isEmpty()) {
                    mUserNameEditText.setError("اسم المستخدم غير صحيح");
                    mUserNameEditText.requestFocus();

                } else {

                    firebaseLogin(email, mPassword);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }


    private void firebaseLogin(String email, String password) {

        mProgressDialog.show();

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {


                        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                        mDatabaseReference = FirebaseDatabase.getInstance().getReference("users");


                        mUserId = firebaseUser.getUid();


                        mDatabaseReference.child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                UserObject userObject = snapshot.getValue(UserObject.class);

                                if (userObject != null) {

                                    saveProfilePicInDevice();

                                    String realName = userObject.getUserRealName();
                                    String uerName = userObject.getUserName();
                                    String emailAddress = userObject.getUserEmail();
                                    String phoneNumber = userObject.getUserPhone();
                                    String joinUnixTime = userObject.getUnixTime();
                                    boolean accountDisable = userObject.isDisable();
                                    boolean accountAdmin = userObject.isAdmin();

                                    editor.putString(getString(R.string.user_id_Key), mUserId);
                                    editor.putString(getString(R.string.real_name_Key), realName);
                                    editor.putString(getString(R.string.user_name_Key), uerName);
                                    editor.putString(getString(R.string.email_address_Key), emailAddress);
                                    editor.putString(getString(R.string.phone_number_Key), phoneNumber);
                                    editor.putString(getString(R.string.user_join_unix_time_key), joinUnixTime);
                                    editor.putBoolean(getString(R.string.account_disable_Key), accountDisable);
                                    editor.putBoolean(getString(R.string.account_admin_Key), accountAdmin);
                                    editor.apply();

                                    mProgressDialog.dismiss();


                                    // changed to be executed after the profile pic downloading finish.

//                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                    startActivity(intent);
//                                    finish();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                                Toast.makeText(mContext, "Can't get the data from the server", Toast.LENGTH_SHORT).show();

                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        mProgressDialog.dismiss();

                        Toast.makeText(LoginActivity.this, "فشل تسجيل الدخول", Toast.LENGTH_SHORT).show();

                    }
                });

    }



    private void saveProfilePicInDevice() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("profile_pic_image")
                .child(mUserId + ".jpeg");

        if (storageReference == null) {
            Toast.makeText(mContext, "null in storage", Toast.LENGTH_SHORT).show();
        }

        storageReference.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        FirebaseUser user = mFirebaseAuth.getCurrentUser();

                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(uri)
                                .build();


                        user.updateProfile(request)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        saveProfilePicInAsThumbnail(uri);

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {

                                        Toast.makeText(mContext, "حدث خطأ اثناء رفع الصورة", Toast.LENGTH_SHORT).show();
                                        Log.i(LOG_TAG, "that error from exception while try to upload the profile pic to firebase  : " + e.toString());

                                        mProgressDialog.dismiss();

                                    }
                                });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                mProgressDialog.dismiss();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(intent);

            }
        });

    }



    private void saveProfilePicInAsThumbnail(Uri uri) {


        File thumbnailPath = mContext.getExternalFilesDir("/thumbnail/profile_pics");
        if (!thumbnailPath.exists()) {
            thumbnailPath.mkdirs();
        }


        String userProfilePicFileName = mUserId + ".jpg";

        File newProfilePic = new File(thumbnailPath, userProfilePicFileName);

        if(newProfilePic.exists()) {
            newProfilePic.delete();
        }




        Glide.with(mContext).asBitmap().load(uri).thumbnail(1f).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                Bitmap profilePicBitmap = resource;

                try {

                    FileOutputStream out = new FileOutputStream(newProfilePic);
                    profilePicBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                    out.flush();
                    out.close();


                    mProgressDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                } catch (Exception e){
                    e.printStackTrace();
                }


            }
        });

    }











    private void setClickingOnLogin() {

        TextView textView = findViewById(R.id.activity_login_login_button);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(hasValidData()) {
                    LoginWithEmailOrUserName();
                }

            }
        });

    }


    private void setClickingOnForgetPassword() {

        TextView textView = findViewById(R.id.activity_login_forget_password);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);


            }
        });

    }



    private void setClickingOnCreateAccount() {

        TextView textView = findViewById(R.id.activity_login_create_account);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);


            }
        });

    }



}