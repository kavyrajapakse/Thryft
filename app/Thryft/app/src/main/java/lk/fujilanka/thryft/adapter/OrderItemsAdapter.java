package lk.fujilanka.thryft.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import lk.fujilanka.thryft.databinding.ItemOrderProductBinding;
import lk.fujilanka.thryft.model.Orders;
import lk.fujilanka.thryft.model.Product;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.ViewHolder>{

    private List<Orders.OrderItem> items = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void setItems(List<Orders.OrderItem> items){
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        ItemOrderProductBinding binding =
                ItemOrderProductBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Orders.OrderItem item = items.get(position);

        holder.binding.tvProductPrice.setText("Rs. " + item.getUnitPrice());

        // load product info from Firestore
        db.collection("products")
                .whereEqualTo("productId", item.getProductId())
                .get()
                .addOnSuccessListener(snapshot -> {

                    if(snapshot.isEmpty()) return;

                    Product product = snapshot.getDocuments()
                            .get(0)
                            .toObject(Product.class);

                    if(product == null) return;

                    holder.binding.tvProductName.setText(product.getTitle());

                    holder.binding.tvProductDetails.setText(
                            "Size: " + product.getSize() +
                                    " • Color: " + product.getColor()
                    );

                    if(product.getImages() != null && !product.getImages().isEmpty()){

                        Glide.with(holder.itemView.getContext())
                                .load(product.getImages().get(0))
                                .into(holder.binding.ivProductImage);
                    }

                });
    }

    @Override
    public int getItemCount(){
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ItemOrderProductBinding binding;

        public ViewHolder(ItemOrderProductBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}