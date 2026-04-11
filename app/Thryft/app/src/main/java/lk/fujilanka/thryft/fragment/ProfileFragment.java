package lk.fujilanka.thryft.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.activity.LoginActivity;
import lk.fujilanka.thryft.activity.MainActivity;
import lk.fujilanka.thryft.activity.MapActivity;
import lk.fujilanka.thryft.databinding.FragmentProfileBinding;
import lk.fujilanka.thryft.model.User;

public class ProfileFragment extends Fragment {

    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    private FragmentProfileBinding binding;
    private ImageView ivProfile;
    private TextView tvName, tvEmail;
    private TextInputEditText etFullName, etEmail;

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                            Uri selectedImage = result.getData().getData();

                            if (selectedImage != null) {

                                // Show image instantly
                                Glide.with(requireContext())
                                        .load(selectedImage)
                                        .circleCrop()
                                        .into(ivProfile);

                                // Upload to Firebase
                                uploadProfileImage(selectedImage);
                            }
                        }
                    }
            );

    private void uploadProfileImage(Uri imageUri) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        StorageReference storageRef = firebaseStorage
                .getReference()
                .child("profile-images")
                .child(uid + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                            String downloadUrl = uri.toString();

                            // Save URL to Firestore
                            firebaseFirestore.collection("users")
                                    .document(uid)
                                    .update("profileImgUrl", downloadUrl);

                        })
                )
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivProfile = view.findViewById(R.id.iv_profile_picture);
        tvName = view.findViewById(R.id.tv_user_name);
        tvEmail = view.findViewById(R.id.tv_user_email);
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);

        loadUserProfile();

        // Click to select new profile picture
        ivProfile.setOnClickListener(v -> openGallery());

        binding.llContactUs.setOnClickListener(v -> {

            String phoneNumber = "+94712345678"; // Your support number

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));

            startActivity(intent);

        });

        // Personal Info expand/collapse
        binding.llPersonalInfoHeader.setOnClickListener(v -> {
            if (binding.llPersonalInfoContent.getVisibility() == View.GONE) {
                binding.llPersonalInfoContent.setVisibility(View.VISIBLE);
                binding.ivPersonalInfoArrow.setRotation(180); // arrow points up
            } else {
                binding.llPersonalInfoContent.setVisibility(View.GONE);
                binding.ivPersonalInfoArrow.setRotation(0); // arrow points down
            }
        });

// Saved Addresses expand/collapse
        binding.llAddressesHeader.setOnClickListener(v -> {
            if (binding.llAddressesContent.getVisibility() == View.GONE) {
                binding.llAddressesContent.setVisibility(View.VISIBLE);
                binding.ivAddressesArrow.setRotation(180);
            } else {
                binding.llAddressesContent.setVisibility(View.GONE);
                binding.ivAddressesArrow.setRotation(0);
            }
        });

        binding.btnSaveChanges.setOnClickListener(v -> {
            String newName = etFullName.getText().toString().trim();
            if (!newName.isEmpty()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    firebaseFirestore.collection("users")
                            .document(user.getUid())
                            .update("name", newName)
                            .addOnSuccessListener(aVoid -> tvName.setText(newName));
                }
            }
        });

        LinearLayout storeLocation = view.findViewById(R.id.ll_store_location);

        storeLocation.setOnClickListener(v -> {

            Intent intent = new Intent(getActivity(), MapActivity.class);
            startActivity(intent);

        });

        Button btnLogout = view.findViewById(R.id.btn_logout); // or binding.btnLogout if using ViewBinding

        btnLogout.setOnClickListener(v -> {
            // 1. Clear SharedPreferences login state
            getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_logged_in", false)
                    .apply();

            // 2. Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // 3. Navigate back to LoginActivity
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear back stack
            startActivity(intent);
            getActivity().finish();
        });
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            if (user != null) {
                                tvName.setText(user.getName());
                                tvEmail.setText(user.getEmail());
                                etFullName.setText(user.getName());
                                etEmail.setText(user.getEmail());

                                Glide.with(requireContext())
                                        .load(user.getProfileImgUrl())
                                        .placeholder(R.drawable.ic_person_placeholder)
                                        .error(R.drawable.ic_person_placeholder)
                                        .circleCrop()
                                        .into(ivProfile);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Log or handle error
                    });
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(Intent.createChooser(intent, "Select Profile Picture"));
    }
}