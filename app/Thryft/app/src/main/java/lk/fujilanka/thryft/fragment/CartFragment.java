package lk.fujilanka.thryft.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.adapter.CartAdapter;
import lk.fujilanka.thryft.databinding.FragmentCartBinding;
import lk.fujilanka.thryft.model.CartItem;
import lk.fujilanka.thryft.model.Product;


public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private List<CartItem> cartItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            String uid = firebaseAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).collection("cart").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot qds) {

                        cartItems = new ArrayList<>();

                        for (DocumentSnapshot ds : qds.getDocuments()) {
                            CartItem cartItem = ds.toObject(CartItem.class);
                            if (cartItem != null) {
                                String documentId = ds.getId();
                                cartItem.setDocumentId(documentId);

                                cartItems.add(cartItem);
                            }
                        }

                        binding.tvCartCount.setText(cartItems.size() + (cartItems.size() == 1 ? " item" : " items"));

                        if (cartItems.isEmpty()) {

                            binding.llEmptyCart.setVisibility(View.VISIBLE);
                            binding.rvCartItems.setVisibility(View.GONE);
                            binding.llCartSummary.setVisibility(View.GONE);

                        } else {

                            binding.llEmptyCart.setVisibility(View.GONE);
                            binding.rvCartItems.setVisibility(View.VISIBLE);
                            binding.llCartSummary.setVisibility(View.VISIBLE);
                        }


                        //   cartItems = qds.toObjects(CartItem.class);

                        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                        binding.rvCartItems.setLayoutManager(layoutManager);

                        CartAdapter adapter = new CartAdapter(cartItems);

                        adapter.setOnRemoveListener(position -> {

                            String documentId = cartItems.get(position).getDocumentId();
                            db.collection("users").document(uid).collection("cart").document(documentId)
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {

                                        cartItems.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        adapter.notifyItemRangeChanged(position, cartItems.size());

                                        binding.tvCartCount.setText(cartItems.size() + (cartItems.size() == 1 ? " item" : " items"));

                                        updateTotal();

                                        if (cartItems.isEmpty()) {
                                            binding.llEmptyCart.setVisibility(View.VISIBLE);
                                            binding.rvCartItems.setVisibility(View.GONE);
                                            binding.llCartSummary.setVisibility(View.GONE);
                                        }

                                        Toast.makeText(getContext(), "Item has been removed!", Toast.LENGTH_SHORT).show();
                                    });


                        });

                        binding.rvCartItems.setAdapter(adapter);
                        updateTotal();

                }
            });

        }

        binding.btnCheckout.setOnClickListener(v -> {

            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            CheckoutFragment checkoutFragment = new CheckoutFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, checkoutFragment)
                    .addToBackStack(null)
                    .commit();
        });

    }

    private void updateTotal() {
        if (cartItems == null || cartItems.isEmpty()) {
            binding.tvSubtotal.setText(String.format(Locale.US, "LKR %,.2f", 0.00));
            binding.tvDeliveryFee.setText(String.format(Locale.US, "LKR %,.2f", 0.00));
            binding.tvTotal.setText(String.format(Locale.US, "LKR %,.2f", 0.00));
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<String> productIds = new ArrayList<>();
        cartItems.forEach(cartItem -> {
            productIds.add(cartItem.getProductId());
        });


        db.collection("products")
                .whereIn("productId", productIds)
                .whereEqualTo("status", true).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot qds) {

                Map<String, Product> productMap = new HashMap<>();

                qds.getDocuments().forEach(ds -> {
                    Product product = ds.toObject(Product.class);
                    if (product != null) {
                        productMap.put(product.getProductId(), product);
                    }
                });


//                        final double[] total = {0};
//
//                        cartItems.forEach(cartItem -> {
//                            Product product = productMap.get(cartItem.getProductId());
//
//                            if (product != null){
//                                total[0] += product.getPrice() * cartItem.getQuantity();
//                            }
//                        });


                double total = 0;
                for (CartItem cartItem : cartItems) {
                    Product product = productMap.get(cartItem.getProductId());
                    if (product != null) {
                        total += product.getPrice();
                    }
                }

                double subtotal = total;
                double delivery = 300;

                binding.tvSubtotal.setText(String.format(Locale.US, "LKR %,.2f", subtotal));
                binding.tvDeliveryFee.setText(String.format(Locale.US, "LKR %,.2f", delivery));
                binding.tvTotal.setText(String.format(Locale.US, "LKR %,.2f", subtotal + delivery));

            }
        });


    }
}