package com.nit.womenssecurity.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.pojos.User;
import com.nit.womenssecurity.utils.WSFirebase;
import com.nit.womenssecurity.utils.WSPreference;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private SweetAlertDialog alertDialog;
    private EditText emailET, passwordET;
    private FirebaseAuth auth;
    private WSPreference wsPreference;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_login);

        wsPreference = new WSPreference(this);
        alertDialog =  new SweetAlertDialog(this);

        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);

        auth = FirebaseAuth.getInstance();

    }


    public void openSignupPage(View view) {
        startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        finish();
    }

    public void openForgetPassword(View view) {
        startActivity(new Intent(this, ForegetPasswordActivity.class));
        finish();
    }

    public void signIn(View view) {

        String email, password;
        try {
            email = emailET.getText().toString().trim();
            password = passwordET.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                emailET.setError("Please enter email");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordET.setError("Please enter password");
                return;
            }

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                alertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                alertDialog.setTitle("Processing...");
                alertDialog.setContentText(null);
                alertDialog.show();

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    getUser(auth.getUid());
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        alertDialog.setTitle("Failed");
                        alertDialog.setContentText("Login failed");
                        alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                alertDialog.dismiss();
                            }
                        });

                        Log.d(TAG, "onFailure:  " + e.getMessage());
                    }
                });
            }
        }catch (Exception e){
            Log.d(TAG, "onClick: " + e.getMessage());
        }

    }

    private void getUser(String userId) {
        WSFirebase.user().child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    alertDialog.dismiss();

                    User user = dataSnapshot.getValue(User.class);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    wsPreference.setUser(user);
                    wsPreference.saveTacking(true);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                alertDialog.setTitle("Failed");
                alertDialog.setContentText("Sign up please");
                alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        alertDialog.dismiss();
                    }
                });

                Log.d(TAG, "onCancelled: "+ databaseError.getDetails());
            }
        });
    }
}
