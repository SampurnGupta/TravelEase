package com.example.travelease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private EditText passwordConfirmInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        emailInput = findViewById(R.id.reg_email_input);
        passwordInput = findViewById(R.id.reg_password_input);
        passwordConfirmInput = findViewById(R.id.reg_password_confirm);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegister();
            }
        });

        findViewById(R.id.signup_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Have an account? Sign In Now"
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void attemptRegister() {
        emailInput.setError(null);
        passwordInput.setError(null);
        passwordConfirmInput.setError(null);

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String passwordConfirm = passwordConfirmInput.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            focusView = emailInput;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailInput.setError("Invalid email address");
            focusView = emailInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            focusView = passwordInput;
            cancel = true;
        } else if (password.length() < 6) {
            passwordInput.setError("Password is too short");
            focusView = passwordInput;
            cancel = true;
        }

        if (!password.equals(passwordConfirm)) {
            passwordConfirmInput.setError("Passwords do not match");
            focusView = passwordConfirmInput;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Registration failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }
}