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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.nit.womenssecurity.R;
import com.nit.womenssecurity.utils.WSFirebase;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForegetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForegetPasswordActivity";

    private RelativeLayout emailBorder;
    private LinearLayout pinLayout;
    private EditText emailET;
    private TextView resentEmail, sentEmail, emailTV;
    private SweetAlertDialog alertDialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        setContentView(R.layout.activity_foreget_password);

        alertDialog = new SweetAlertDialog(this);

        emailBorder = findViewById(R.id.emailBorder);
        pinLayout = findViewById(R.id.pinLayout);
        emailET = findViewById(R.id.emailET);
        resentEmail = findViewById(R.id.resentEmail);
        sentEmail = findViewById(R.id.sentEmail);
        emailTV = findViewById(R.id.emailTV);

        pinLayout.setVisibility(View.GONE);
    }

    public void onLogin(View view) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void onSend(View view) {
        String txt = sentEmail.getText().toString();
        String email = "";
        if (txt.equals("Send Email")) {
            try {
                email = emailET.getText().toString().trim();

            }catch (Exception e){
                Log.d(TAG, "onSend: " + e.getMessage());
            }

            if (TextUtils.isEmpty(email)) {
                emailET.setError("Enter Email");
                return;
            }
            alertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
            alertDialog.setTitle("Processing...");
            alertDialog.show();
            String finalEmail = email;
            WSFirebase.getAuth().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                emailET.setVisibility(View.GONE);
                                emailBorder.setVisibility(View.GONE);

                                pinLayout.setVisibility(View.VISIBLE);
                                emailTV.setText(finalEmail);
                                sentEmail.setText("Check Mail");

                                alertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                alertDialog.setTitle("Successful");
                                alertDialog.setContentText("Email send to : " + finalEmail);
                                alertDialog.setConfirmText("Ok");
                                alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                                        alertDialog.dismiss();
                                    }
                                });

                                Toast.makeText(ForegetPasswordActivity.this, "Email send", Toast.LENGTH_SHORT).show();
                            } else {
                                alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                alertDialog.setTitle("Failed");
                                alertDialog.setContentText(task.getException().getMessage());
                                alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        alertDialog.dismiss();
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    alertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    alertDialog.setTitle("Failed");
                    alertDialog.setContentText(e.getMessage());
                    alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            alertDialog.dismiss();
                        }
                    });
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }
            });
        } else if (txt.equals("Verify")) {

        }
    }

    public void resendEmail(View view) {
        emailET.setVisibility(View.VISIBLE);
        emailBorder.setVisibility(View.VISIBLE);
        pinLayout.setVisibility(View.GONE);

        sentEmail.setText("Send Email");
    }
}
