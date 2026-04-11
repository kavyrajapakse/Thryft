package lk.fujilanka.thryft.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lk.fujilanka.thryft.activity.MainActivity;
import lk.fujilanka.thryft.databinding.FragmentCheckoutBinding;
import lk.fujilanka.thryft.model.CartItem;
import lk.fujilanka.thryft.model.Orders;
import lk.fujilanka.thryft.model.Product;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import android.util.Log;
import java.io.IOException;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private double total;
    private boolean paymentActive;

    public CheckoutFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        loadCartSummary();

        binding.btnPlaceOrder.setOnClickListener(v -> startPayment());
    }

    // ---------------- LOAD CART ----------------
    private void loadCartSummary() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(qds -> {

                    List<CartItem> cartItems = qds.toObjects(CartItem.class);
                    if (cartItems.isEmpty()) return;

                    List<String> productIds = new ArrayList<>();
                    for (CartItem item : cartItems) productIds.add(item.getProductId());

                    db.collection("products")
                            .whereIn("productId", productIds)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {

                                Map<String, Product> productMap = new HashMap<>();
                                for (DocumentSnapshot doc : productSnapshot.getDocuments()) {
                                    Product p = doc.toObject(Product.class);
                                    if (p != null) {
                                        p.setDocumentId(doc.getId());
                                        productMap.put(p.getProductId(), p);
                                    }
                                }

                                double subtotal = 0;
                                for (CartItem item : cartItems) {
                                    Product product = productMap.get(item.getProductId());
                                    if (product != null) subtotal += product.getPrice();
                                }

                                double delivery = 300;
                                total = subtotal + delivery;

                                binding.tvItemsCount.setText(cartItems.size() + " item");
                                binding.tvSubtotal.setText("Rs. " + subtotal);
                                binding.tvDeliveryFee.setText("Rs. " + delivery);
                                binding.tvTotal.setText("Rs. " + total);

                                paymentActive = true;
                            });
                });
    }

    // ---------------- START PAYMENT ----------------
    private void startPayment() {
        if (!validateInputs()) return;
        if (!paymentActive) {
            Toast.makeText(getContext(), "Cart not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        InitRequest req = new InitRequest();
        req.setSandBox(true);

        req.setMerchantId("1234254");
        req.setMerchantSecret("MjEzODc0MTUwNDI2ODIyNjA5NjMxNTgzNTk3NTAwNDAwOTc2Mjk2NA==");
        req.setCurrency("LKR");
        req.setAmount(total);
        req.setOrderId("es-" + System.currentTimeMillis());
        req.setItemsDescription("thryft");

        req.getCustomer().setFirstName(binding.etFullName.getText().toString().trim());
        req.getCustomer().setLastName(""); // optional
        req.getCustomer().setEmail(binding.etEmail.getText().toString().trim());
        req.getCustomer().setPhone(binding.etPhone.getText().toString().trim());
        req.getCustomer().getAddress().setAddress(binding.etAddressLine1.getText().toString().trim());
        req.getCustomer().getAddress().setCity(binding.etCity.getText().toString().trim());
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        req.setNotifyUrl("https://thryft.requestcatcher.com/");

        Intent intent = new Intent(getActivity(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);

        payhereLauncher.launch(intent);
    }

    // ---------------- PAYMENT RESULT ----------------
    private final ActivityResultLauncher<Intent> payhereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                        PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);

                        if (response != null && response.isSuccess()) {
                            Log.i("Payhere", "Payment success");
                            placeOrderAfterPayment(); // ← place order here
                        } else {
                            Log.e("Payhere", response.getData().getMessage());
                            Toast.makeText(getContext(), "Payment failed: " + response.getData().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Payhere", "Payment response missing");
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.e("Payhere", "Payment cancelled");
                    Toast.makeText(getContext(), "Payment cancelled", Toast.LENGTH_SHORT).show();
                }
            });

    // ---------------- PLACE ORDER AFTER PAYMENT ----------------
    private void placeOrderAfterPayment() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(qds -> {

                    if (qds.isEmpty()) {
                        Toast.makeText(getContext(), "Cart empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<CartItem> cartItems = qds.toObjects(CartItem.class);
                    List<String> productIds = new ArrayList<>();
                    for (CartItem item : cartItems) productIds.add(item.getProductId());

                    db.collection("products")
                            .whereIn("productId", productIds)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {

                                Map<String, Product> productMap = new HashMap<>();
                                for (DocumentSnapshot doc : productSnapshot.getDocuments()) {
                                    Product p = doc.toObject(Product.class);
                                    if (p != null) {
                                        p.setDocumentId(doc.getId());
                                        productMap.put(p.getProductId(), p);
                                    }
                                }

                                // Prepare order items & batch
                                List<Orders.OrderItem> orderItems = new ArrayList<>();
                                WriteBatch batch = db.batch();
                                double totalAmount = 0;

                                for (CartItem item : cartItems) {
                                    Product product = productMap.get(item.getProductId());
                                    if (product == null) continue;

                                    orderItems.add(
                                            Orders.OrderItem.builder()
                                                    .productId(product.getProductId())
                                                    .unitPrice(product.getPrice())
                                                    .build()
                                    );

                                    totalAmount += product.getPrice();

                                    if (product.getDocumentId() != null) {
                                        batch.update(db.collection("products").document(product.getDocumentId()), "status", false);
                                    }
                                }

                                totalAmount += 300; // delivery

                                Orders.Address address = Orders.Address.builder()
                                        .name(binding.etFullName.getText().toString().trim())
                                        .contact(binding.etPhone.getText().toString().trim())
                                        .email(binding.etEmail.getText().toString().trim())
                                        .address1(binding.etAddressLine1.getText().toString().trim())
                                        .address2(binding.etAddressLine2.getText().toString().trim())
                                        .city(binding.etCity.getText().toString().trim())
                                        .postcode(binding.etPostalCode.getText().toString().trim())
                                        .build();

                                Orders order = Orders.builder()
                                        .orderId(String.valueOf(System.currentTimeMillis()))
                                        .userId(uid)
                                        .totalAmount(totalAmount)
                                        .status("PENDING")
                                        .orderDate(System.currentTimeMillis())
                                        .shippingAddress(address)
                                        .orderItems(orderItems)
                                        .build();

                                // commit batch and save order
                                batch.commit()
                                        .addOnSuccessListener(aVoid -> {
                                            db.collection("orders")
                                                    .document(order.getOrderId())
                                                    .set(order)
                                                    .addOnSuccessListener(unused -> {
                                                        clearCart(uid);
                                                        Toast.makeText(getContext(), "Order Placed Successfully!", Toast.LENGTH_SHORT).show();
                                                        sendInvoiceEmail(userEmail, total);
                                                        Log.d("CheckoutFragment", "Order saved: " + order.getOrderId());

                                                        Map<String, Object> notification = new HashMap<>();
                                                        notification.put("userId", uid);
                                                        notification.put("title", "Order Confirmed");
                                                        notification.put("message", "Your order has been placed successfully.");
                                                        notification.put("timestamp", System.currentTimeMillis());
                                                        notification.put("read", false);

                                                        FirebaseFirestore.getInstance()
                                                                .collection("notifications")
                                                                .add(notification)
                                                                .addOnSuccessListener(doc -> Log.d("NOTIFICATION", "Saved"));



                                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        getActivity().finish();
                                                    })
                                                    .addOnFailureListener(e -> Log.e("CheckoutFragment", "Failed to save order: " + e.getMessage()));
                                        })
                                        .addOnFailureListener(e -> Log.e("CheckoutFragment", "Failed to mark products sold: " + e.getMessage()));

                            });

                });
    }

    private void sendInvoiceEmail(String userEmail, double total) {

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();

        try {
            json.put("sender", new JSONObject()
                    .put("name", "Thryft")
                    .put("email", "tashiyajay0@gmail.com")); // 🔥 must match Brevo sender

            json.put("to", new JSONArray()
                    .put(new JSONObject()
                            .put("email", userEmail)));

            json.put("subject", "Your Order Invoice");

            json.put("htmlContent",
                    "<h2>Order Confirmed 🎉</h2>" +
                            "<p>Thank you for shopping with Thryft!</p>" +
                            "<p><b>Total: LKR " + total + "</b></p>" +
                            "<br><p>We will deliver your items soon.</p>");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                .addHeader("accept", "application/json")
                .addHeader("api-key", key") // 🔥 paste API key
                .addHeader("content-type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("EMAIL", "Failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("EMAIL", "Success: " + response.body().string());
            }
        });
    }

    // ---------------- CLEAR CART ----------------
    private void clearCart(String uid) {
        db.collection("users")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener(qds -> qds.getDocuments().forEach(doc -> doc.getReference().delete()));
    }

    // ---------------- VALIDATE INPUTS ----------------
    private boolean validateInputs() {
        String name = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String address1 = binding.etAddressLine1.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();
        String postcode = binding.etPostalCode.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etFullName.setError("Enter full name");
            binding.etFullName.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            binding.etPhone.setError("Enter contact number");
            binding.etPhone.requestFocus();
            return false;
        }

        if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            binding.etPhone.setError("Enter valid phone number");
            binding.etPhone.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("Enter email");
            binding.etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Enter valid email");
            binding.etEmail.requestFocus();
            return false;
        }

        if (address1.isEmpty()) {
            binding.etAddressLine1.setError("Enter address");
            binding.etAddressLine1.requestFocus();
            return false;
        }

        if (city.isEmpty()) {
            binding.etCity.setError("Enter city");
            binding.etCity.requestFocus();
            return false;
        }

        if (postcode.isEmpty()) {
            binding.etPostalCode.setError("Enter postal code");
            binding.etPostalCode.requestFocus();
            return false;
        }

        return true;
    }
}