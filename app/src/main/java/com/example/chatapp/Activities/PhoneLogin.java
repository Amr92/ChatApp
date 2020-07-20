package com.example.chatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhoneLogin extends AppCompatActivity {

    private EditText phoneNumberInput;
    private EditText verificationCodeInput;
    private Button sendVerCodeButton;
    private Button verifyButton;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(PhoneLogin.this);

        phoneNumberInput = findViewById(R.id.phone_number_input);
        verificationCodeInput = findViewById(R.id.verification_code_input);
        sendVerCodeButton = findViewById(R.id.send_ver_code_button);
        verifyButton = findViewById(R.id.verify_button);

        sendVerCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberInput.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    phoneNumberInput.requestFocus();
                    Toast.makeText(PhoneLogin.this, "please enter your phone number..", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLogin.this,
                            callbacks);

                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerCodeButton.setVisibility(View.INVISIBLE);
                phoneNumberInput.setVisibility(View.INVISIBLE);

                String verCode = verificationCodeInput.getText().toString();
                if(TextUtils.isEmpty(verCode)){
                    Toast.makeText(PhoneLogin.this, "please write code first", Toast.LENGTH_SHORT).show();
                }else {
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                loadingBar.dismiss();
                Toast.makeText(PhoneLogin.this, "Invalid Phone number, please enter correct phone number", Toast.LENGTH_SHORT).show();
                sendVerCodeButton.setVisibility(View.VISIBLE);
                phoneNumberInput.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                verificationCodeInput.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                sendVerCodeButton.setVisibility(View.INVISIBLE);
                phoneNumberInput.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                verificationCodeInput.setVisibility(View.VISIBLE);

            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLogin.this, "logged in successfully..", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {

                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(PhoneLogin.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}
