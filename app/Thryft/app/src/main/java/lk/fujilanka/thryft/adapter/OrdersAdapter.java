package lk.fujilanka.thryft.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.activity.OrderDetailsActivity;
import lk.fujilanka.thryft.databinding.ItemOrderBinding;
import lk.fujilanka.thryft.model.Orders;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder>{

    private List<Orders> orders = new ArrayList<>();

    public void setOrders(List<Orders> orders){
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Orders order = orders.get(position);

        holder.binding.tvOrderId.setText("Order #" + order.getOrderId());

        holder.binding.tvOrderTotal.setText("Rs. " + order.getTotalAmount());

        int count = order.getOrderItems() != null ? order.getOrderItems().size() : 0;
        holder.binding.tvItemsCount.setText(count + " items");

        holder.binding.tvOrderDate.setText(
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(new Date(order.getOrderDate()))
        );

        String status = order.getStatus();
        holder.binding.tvOrderStatus.setText(status);

        switch(status.toLowerCase()){

            case "pending":
                holder.binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;

            case "processing":
                holder.binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_processing);
                break;

            case "shipped":
                holder.binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_shipped);
                break;

            case "delivered":
                holder.binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_delivered);
                break;

            case "cancelled":
                holder.binding.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
        }

        holder.binding.btnViewDetails.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), OrderDetailsActivity.class);

            intent.putExtra("orderId", order.getOrderId());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount(){
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ItemOrderBinding binding;

        public ViewHolder(ItemOrderBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}