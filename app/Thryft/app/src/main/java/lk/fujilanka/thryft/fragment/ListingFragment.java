package lk.fujilanka.thryft.fragment;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Arrays;
import java.util.List;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.adapter.ListingAdapter;
import lk.fujilanka.thryft.databinding.FragmentListingBinding;
import lk.fujilanka.thryft.model.Product;

public class ListingFragment extends Fragment {

    private FragmentListingBinding binding;
    private ListingAdapter adapter;
    private String categoryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString("categoryId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentListingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        Product p1 = new Product(
//                "pid1",
//                "Floral Summer Dress",
//                "Light cotton summer dress, perfect for casual outings.",
//                2500,
//                "cat1",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1520975922284-9e0ce82759c2",
//                        "https://images.unsplash.com/photo-1503341455253-b2e723bb3dbb"
//                ),
//                true,
//                "M",
//                "Like New",
//                "Yellow"
//        );
//
//        Product p2 = new Product(
//                "pid2",
//                "Evening Party Dress",
//                "Elegant evening dress with sequins, ideal for events.",
//                4800,
//                "cat1",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1520975922284-9e0ce82759c2",
//                        "https://images.unsplash.com/photo-1541099649105-f69ad21f3246"
//                ),
//                true,
//                "L",
//                "Used",
//                "Red"
//        );
//
//        Product p3 = new Product(
//                "pid3",
//                "Casual Top Blouse",
//                "Cotton blend top, comfortable for everyday wear.",
//                1200,
//                "cat2",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"
//                ),
//                true,
//                "S",
//                "Like New",
//                "White"
//        );
//
//        Product p4 = new Product(
//                "pid4",
//                "Chiffon Party Top",
//                "Soft chiffon blouse, perfect for evening outings.",
//                1800,
//                "cat2",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab"
//                ),
//                true,
//                "M",
//                "Used",
//                "Blue"
//        );
//
//        Product p5 = new Product(
//                "pid5",
//                "Striped Casual Shirt",
//                "Cotton shirt, great for office or casual wear.",
//                1500,
//                "cat3",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c"
//                ),
//                true,
//                "L",
//                "Like New",
//                "Blue"
//        );
//
//        Product p6 = new Product(
//                "pid6",
//                "Plaid T-Shirt",
//                "Classic plaid t-shirt, soft fabric and breathable.",
//                900,
//                "cat3",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c"
//                ),
//                true,
//                "M",
//                "Used",
//                "Red"
//        );
//
//        Product p7 = new Product(
//                "pid7",
//                "Denim Shorts",
//                "Blue denim shorts, casual and comfortable.",
//                1300,
//                "cat4",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1541099649105-f69ad21f3246"
//                ),
//                true,
//                "M",
//                "Like New",
//                "Blue"
//        );
//
//        Product p8 = new Product(
//                "pid8",
//                "Black Jeans",
//                "Slim fit black jeans, suitable for all occasions.",
//                2000,
//                "cat4",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1541099649105-f69ad21f3246"
//                ),
//                true,
//                "L",
//                "Used",
//                "Black"
//        );
//
//        Product p9 = new Product(
//                "pid9",
//                "Pleated Skirt",
//                "Elegant pleated skirt, great for casual or office.",
//                1400,
//                "cat5",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1490481651871-ab68de25d43d"
//                ),
//                true,
//                "M",
//                "Like New",
//                "Beige"
//        );
//
//        Product p10 = new Product(
//                "pid10",
//                "Mini Skirt",
//                "Stylish mini skirt, perfect for summer days.",
//                1200,
//                "cat5",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1490481651871-ab68de25d43d"
//                ),
//                true,
//                "S",
//                "Used",
//                "Red"
//        );
//
//        Product p11 = new Product(
//                "pid11",
//                "Leather Bag",
//                "Vintage leather shoulder bag, spacious and trendy.",
//                3500,
//                "cat6",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1591561954557-26941169b49e"
//                ),
//                true,
//                "One Size",
//                "Like New",
//                "Brown"
//        );
//
//        Product p12 = new Product(
//                "pid12",
//                "Canvas Tote Bag",
//                "Casual tote bag, ideal for shopping or beach.",
//                1800,
//                "cat6",
//                Arrays.asList(
//                        "https://images.unsplash.com/photo-1591561954557-26941169b49e"
//                ),
//                true,
//                "One Size",
//                "Used",
//                "Beige"
//        );
//
//        List<Product> products = List.of(p1,p2,p3,p4,p5,p6,p7,p8,p9,p10,p11,p12);
//
//        WriteBatch batch = db.batch();
//        for(Product p : products){
//            DocumentReference ref = db.collection("products").document();
//            batch.set(ref, p);
//        }
//        batch.commit();


        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("status", true) // <-- only active products
                .get()
                .addOnSuccessListener(ds -> {
                    if (!ds.isEmpty()) {
                        List<Product> products = ds.toObjects(Product.class);

                        adapter = new ListingAdapter(products, product -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("productId", product.getProductId());

                            ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();
                            productDetailsFragment.setArguments(bundle);

                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragmentContainer, productDetailsFragment)
                                    .addToBackStack(null)
                                    .commit();
                        });

                        binding.rvProducts.setAdapter(adapter);
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "Error:" + e.getMessage()));

        getActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        MaterialToolbar toolbar = binding.toolbar; // or findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener(v -> {
            // This will go back to the previous fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });



    }
}