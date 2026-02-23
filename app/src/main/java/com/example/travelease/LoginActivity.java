package com.example.travelease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.travelease.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();
        setupListeners();
    }

    private void setupListeners() {
        // Login button click listener
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Forgot password click listener
        binding.forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Sign up text click listener
        binding.signupText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void attemptLogin() {
        binding.emailInput.setError(null);
        binding.passwordInput.setError(null);

        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            binding.passwordInput.setError("Password is required");
            focusView = binding.passwordInput;
            cancel = true;
        } else if (password.length() < 6) {
            binding.passwordInput.setError("Password is too short");
            focusView = binding.passwordInput;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            binding.emailInput.setError("Email is required");
            focusView = binding.emailInput;
            cancel = true;
        } else if (!isEmailValid(email)) {
            binding.emailInput.setError("Invalid email address");
            focusView = binding.emailInput;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Login failed: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }
}
