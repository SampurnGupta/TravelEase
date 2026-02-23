package com.example.travelease;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        emailInput = findViewById(R.id.email_input);
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.send_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordReset();
            }
        });

        findViewById(R.id.signup_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "Don't have an account? Register Now"
                Intent intent = new Intent(ForgotPasswordActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void attemptPasswordReset() {
        emailInput.setError(null);
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
        } else if (!isEmailValid(email)) {
            emailInput.setError("Invalid email address");
            emailInput.requestFocus();
        } else {
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Password reset link sent to " + email, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to send reset email: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }
}
