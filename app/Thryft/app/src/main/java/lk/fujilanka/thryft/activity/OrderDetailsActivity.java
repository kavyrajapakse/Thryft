package lk.fujilanka.thryft.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lk.fujilanka.thryft.adapter.OrderItemsAdapter;
import lk.fujilanka.thryft.databinding.ActivityOrderDetailsBinding;
import lk.fujilanka.thryft.model.Orders;

public class OrderDetailsActivity extends AppCompatActivity {

    private ActivityOrderDetailsBinding binding;
    private FirebaseFirestore db;

    private OrderItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        adapter = new OrderItemsAdapter();

        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrderItems.setAdapter(adapter);

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String orderId = getIntent().getStringExtra("orderId");

        loadOrder(orderId);
    }

    private void loadOrder(String orderId){

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {

                    Orders order = doc.toObject(Orders.class);

                    if(order == null) return;

                    binding.tvOrderId.setText("Order #" + order.getOrderId());

                    binding.tvOrderStatus.setText(order.getStatus());

                    binding.tvOrderDate.setText(
                            "Placed on " +
                                    new SimpleDateFormat("MMM dd, yyyy HH:mm",
                                            Locale.getDefault())
                                            .format(new Date(order.getOrderDate()))
                    );

                    // Address
                    binding.tvCustomerName.setText(order.getShippingAddress().getName());
                    binding.tvCustomerPhone.setText(order.getShippingAddress().getContact());

                    binding.tvDeliveryAddress.setText(
                            order.getShippingAddress().getAddress1() + "\n" +
                                    order.getShippingAddress().getCity() + "\n" +
                                    order.getShippingAddress().getPostcode()
                    );

                    double subtotal = order.getTotalAmount() - 300;

                    binding.tvSubtotal.setText("Rs. " + subtotal);
                    binding.tvDeliveryFee.setText("Rs. 300");
                    binding.tvTotal.setText("Rs. " + order.getTotalAmount());

                    adapter.setItems(order.getOrderItems());
                });
    }
}