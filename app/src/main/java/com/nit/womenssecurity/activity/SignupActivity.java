package com.nit.womenssecurity.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.utils.WSFirebase;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    private SweetAlertDialog alertDialog;
    private FirebaseAuth auth;

    private EditText nameET, emailET, phoneET, passwordET;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_signup);

        alertDialog = new SweetAlertDialog(this);
        auth = FirebaseAuth.getInstance();

        nameET = findViewById(R.id.nameET);
        emailET = findViewById(R.id.emailET);
        phoneET = findViewById(R.id.phoneET);
        passwordET = findViewById(R.id.passwordET);
        passwordET.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

    }

    public void onLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void signUp(View view) {
        String name, email, phone, password;
        try {
            name = nameET.getText().toString().trim();
            email = emailET.getText().toString().trim();
            phone = phoneET.getText().toString().trim();
            password = passwordET.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                nameET.setError("Enter full name");
                return;
            }
            if (TextUtils.isEmpty(email)) {
                emailET.setError("Enter email");
                return;
            }
            if (TextUtils.isEmpty(phone)) {
                phoneET.setError("Enter phone");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordET.setError("Enter password");
                return;
            }
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password)) {

                alertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                alertDialog.setTitle("Processing....");
                alertDialog.show();
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    User user = new User(auth.getUid(), name, email, phone);
                                    saveUser(user);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        alertDialog.setTitle("Failed");
                        alertDialog.setContentText("Sign up failed. Try agin");
                        alertDialog.setConfirmText("OK");
                        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                alertDialog.dismiss();
                            }
                        });

                        Log.d(TAG, "onFailure: " + e.getMessage());
                    }
                });

            }

        }catch (Exception e){
            Log.d(TAG, "signUp: "+ e.getMessage());
        }
    }

    private void saveUser(User user) {
        WSFirebase.user().child(user.getId()).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                            alertDialog.dismiss();
                            Toast.makeText(SignupActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                alertDialog.setTitle("Failed");
                alertDialog.setContentText(e.getMessage());
                alertDialog.setConfirmText("OK");
                alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        alertDialog.dismiss();
                    }
                });


                Log.d(TAG, "onFailure: " + e.getMessage());
            }
        });
    }
}
