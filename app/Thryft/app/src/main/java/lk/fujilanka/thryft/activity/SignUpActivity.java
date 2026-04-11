package lk.fujilanka.thryft.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.databinding.ActivityLoginBinding;
import lk.fujilanka.thryft.databinding.ActivitySignUpBinding;
import lk.fujilanka.thryft.model.User;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.tvSignInLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnCreateAccount.setOnClickListener(view -> {

            String name = binding.etFullName.getText().toString().trim();
            String email = binding.etEmailSignup.getText().toString().trim();
            String password = binding.etPasswordSignup.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (name.isEmpty()) {
                binding.etFullName.setError("Full Name is Required");
                binding.etFullName.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                binding.etEmailSignup.setError("Email is Required");
                binding.etEmailSignup.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmailSignup.setError("Email is not Valid");
                binding.etEmailSignup.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.etPasswordSignup.setError("Password is Required");
                binding.etPasswordSignup.requestFocus();
                return;
            }

            if (password.length() < 6) {
                binding.etPasswordSignup.setError("Password must be at least Characters");
                binding.etPasswordSignup.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                binding.etConfirmPassword.setError("Password Confirmation is Required");
                binding.etConfirmPassword.requestFocus();
                return;
            }

            if (!confirmPassword.equals(password)) {
                binding.etConfirmPassword.setError("Passwords should match");
                binding.etConfirmPassword.requestFocus();
                return;
            }


            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String uid = task.getResult().getUser().getUid();

                                User user = User.builder().uid(uid)
                                        .name(name)
                                        .email(email).build();

                                firebaseFirestore.collection("users")
                                        .document(uid)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getApplicationContext(), "User registered Successfully", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                            }
                        }
                    });

        });

    }
}