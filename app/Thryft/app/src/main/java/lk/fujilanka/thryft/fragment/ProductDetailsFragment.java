package lk.fujilanka.thryft.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.activity.LoginActivity;
import lk.fujilanka.thryft.adapter.ImagePagerAdapter;
import lk.fujilanka.thryft.adapter.SectionAdapter;
import lk.fujilanka.thryft.databinding.FragmentProductDetailsBinding;
import lk.fujilanka.thryft.model.CartItem;
import lk.fujilanka.thryft.model.Product;

public class ProductDetailsFragment extends Fragment {

    private FragmentProductDetailsBinding binding;
    private String productId;

    public ProductDetailsFragment() {
        // Required empty constructor
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProductDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide bottom navigation
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);

        // Toolbar back button
        binding.toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );

        // Device back button
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                }
        );

        loadProduct();

        binding.fabAddToCart.setOnClickListener(v -> {

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

            if (firebaseAuth.getCurrentUser() == null) {
                showLoginDialog();
                return;
            }

            String uid = firebaseAuth.getCurrentUser().getUid();

            db.collection("users").document(uid)
                    .collection("cart")
                    .document(productId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists()) {
                            Toast.makeText(getContext(), "Item already in cart", Toast.LENGTH_SHORT).show();
                        } else {

                            CartItem cartItems = new CartItem(productId);

                            db.collection("users").document(uid)
                                    .collection("cart")
                                    .document(productId)
                                    .set(cartItems)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(getContext(), "Item added to cart!", Toast.LENGTH_SHORT).show());
                        }
                    });
        });
    }

    private void loadProduct() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("products")
                .whereEqualTo("productId", productId)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.isEmpty()) {

                        Product product = snapshot.getDocuments()
                                .get(0)
                                .toObject(Product.class);

                        if (product != null) {
                            setProductData(product);
                        }
                    }
                });
    }

    private void setProductData(Product product) {

        // Product Images
        if (product.getImages() != null && !product.getImages().isEmpty()) {

            ImagePagerAdapter adapter =
                    new ImagePagerAdapter(product.getImages());

            binding.vpProductImages.setAdapter(adapter);

            // 🔥 ADD THIS
            setupIndicators(product.getImages().size());

            binding.vpProductImages.registerOnPageChangeCallback(
                    new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            setCurrentIndicator(position);
                        }
                    }
            );
        }

        // Title
        binding.tvProductTitle.setText(product.getTitle());

        // Price
        binding.tvPrice.setText("Rs. " + product.getPrice());

//        // Category
//        binding.tvCategory.setText("Category: " + product.getCategory());

        // Size
        binding.tvSize.setText(product.getSize());

        // Color
        binding.tvColor.setText(product.getColor());

        // Condition
        binding.tvCondition.setText(product.getCondition());

        // Product ID
        binding.tvProductId.setText(product.getProductId());

        // Description
        binding.tvDescription.setText(product.getDescription());

        // Product Status
        // Product Status
        if (product.isStatus()) {

            binding.tvStatus.setText("Available");
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_available);

        } else {

            binding.tvStatus.setText("Sold");
            binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_sold);

        }

        loadRelatedProducts(product.getCategoryId());
    }

    private void loadRelatedProducts(String categoryId) {

        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("status", true) // only active products
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        List<Product> products =
                                queryDocumentSnapshots.toObjects(Product.class);

                        List<Product> filtered = new ArrayList<>();

                        for (Product p : products) {

                            if (!p.getProductId().equals(productId)) {
                                filtered.add(p);
                            }

                        }

                        LinearLayoutManager layoutManager =
                                new LinearLayoutManager(
                                        getContext(),
                                        LinearLayoutManager.HORIZONTAL,
                                        false
                                );

                        binding.rvRelatedProducts.setLayoutManager(layoutManager);

                        SectionAdapter adapter =
                                new SectionAdapter(filtered, product -> {

                                    Bundle bundle = new Bundle();
                                    bundle.putString("productId", product.getProductId());

                                    ProductDetailsFragment fragment =
                                            new ProductDetailsFragment();
                                    fragment.setArguments(bundle);

                                    getParentFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragmentContainer, fragment)
                                            .addToBackStack(null)
                                            .commit();

                                });

                        binding.rvRelatedProducts.setAdapter(adapter);

                    }

                });
    }


    private void showLoginDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Login Required")
                .setMessage("Please login to add items to your cart.")
                .setPositiveButton("Login", (dialog, which) -> {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupIndicators(int count) {

        binding.llImageIndicators.removeAllViews();

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(getContext());

            dot.setImageResource(R.drawable.indicator_dot_inactive);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);

            binding.llImageIndicators.addView(dot);
        }

        setCurrentIndicator(0); // first dot active
    }

    private void setCurrentIndicator(int position) {

        for (int i = 0; i < binding.llImageIndicators.getChildCount(); i++) {

            ImageView dot = (ImageView) binding.llImageIndicators.getChildAt(i);

            if (i == position) {
                dot.setImageResource(R.drawable.indicator_dot_active);
            } else {
                dot.setImageResource(R.drawable.indicator_dot_inactive);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Show bottom navigation again
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Hide bottom navigation when opening product details
        requireActivity().findViewById(R.id.bottom_navigation).setVisibility(View.GONE);
    }
}