package lk.fujilanka.thryft.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.adapter.OrdersAdapter;
import lk.fujilanka.thryft.model.Orders;

public class OrderFragment extends Fragment {

    private RecyclerView rvOrders;
    private TabLayout tabLayout;
    private LinearLayout llEmptyOrders;
    private ProgressBar progressBar;

    private OrdersAdapter ordersAdapter;

    private List<Orders> allOrders = new ArrayList<>();
    private List<Orders> filteredOrders = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public OrderFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        rvOrders = view.findViewById(R.id.rv_orders);
        tabLayout = view.findViewById(R.id.tab_layout);
        llEmptyOrders = view.findViewById(R.id.ll_empty_orders);
        progressBar = view.findViewById(R.id.progress_bar);

        ordersAdapter = new OrdersAdapter();

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(ordersAdapter);

        loadOrders();

        setupTabs();
    }

    private void loadOrders(){

        progressBar.setVisibility(View.VISIBLE);

        String uid = auth.getCurrentUser().getUid();

        db.collection("orders")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    progressBar.setVisibility(View.GONE);

                    allOrders.clear();

                    for(DocumentSnapshot doc : snapshot.getDocuments()){

                        Orders order = doc.toObject(Orders.class);

                        if(order != null){
                            allOrders.add(order);
                        }
                    }

                    filterOrders("All");

                });
    }

    private void setupTabs(){

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                String tabName = tab.getText().toString();

                filterOrders(tabName);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterOrders(String status){

        filteredOrders.clear();

        if(status.equalsIgnoreCase("All")){

            filteredOrders.addAll(allOrders);

        }else{

            for(Orders order : allOrders){

                if(order.getStatus() != null &&
                        order.getStatus().equalsIgnoreCase(status)){

                    filteredOrders.add(order);
                }
            }
        }

        ordersAdapter.setOrders(filteredOrders);

        checkEmptyState();
    }

    private void checkEmptyState(){

        if(filteredOrders.isEmpty()){

            llEmptyOrders.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);

        }else{

            llEmptyOrders.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }
}