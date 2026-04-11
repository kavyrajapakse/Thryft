package lk.fujilanka.thryft.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) { // when user leaves the field
                String email = binding.etEmail.getText().toString().trim();

                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.etEmail.setError("Invalid email format");
                }
            }
        });

        binding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String password = binding.etPassword.getText().toString().trim();

                if (!password.isEmpty() && password.length() < 6) {
                    binding.etPassword.setError("Password must be at least 6 characters");
                }
            }
        });

        binding.tvSignUpLink.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnSignIn.setOnClickListener(view -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.etEmail.setError("Email is Required");
                binding.etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.setError("Email is not Valid");
                binding.etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.etPassword.setError("Password is Required");
                binding.etPassword.requestFocus();
                return;
            }

            if(password.length() < 6){
                binding.etPassword.setError("Password must be at least 6 characters");
                binding.etPassword.requestFocus();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                updateUI(firebaseAuth.getCurrentUser());
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        binding.tvForgotPassword.setOnClickListener(v -> {

            String email = binding.etEmail.getText().toString().trim();

            if(email.isEmpty()){
                binding.etEmail.setError("Enter your email first");
                binding.etEmail.requestFocus();
                return;
            }

            sendPasswordResetEmail(email);

        });

    }

    private void updateUI(FirebaseUser user) {
        // Save login state
        getSharedPreferences("user_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", true)
                .apply();

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendPasswordResetEmail(String email){

        FirebaseAuth.getInstance()
                .sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if(task.isSuccessful()){

                        Toast.makeText(
                                LoginActivity.this,
                                "Password reset email sent",
                                Toast.LENGTH_LONG
                        ).show();

                    }else{

                        Toast.makeText(
                                LoginActivity.this,
                                "Email not registered",
                                Toast.LENGTH_LONG
                        ).show();

                    }

                });
    }

}