package lk.fujilanka.thryft.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.fujilanka.thryft.databinding.ItemProductGridSearchBinding;
import lk.fujilanka.thryft.model.Product;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ViewHolder>{

    private List<Product> products;

    private OnProductClickListener listener;

    public ProductGridAdapter(List<Product> products, OnProductClickListener listener){
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        ItemProductGridSearchBinding binding =
                ItemProductGridSearchBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){

        Product product = products.get(position);

        holder.binding.tvProductName.setText(product.getTitle());

        holder.binding.tvProductPrice.setText("Rs. " + product.getPrice());

        holder.binding.tvProductCategory.setText(product.getSize());

        Glide.with(holder.itemView.getContext())
                .load(product.getImages().get(0))
                .into(holder.binding.ivProductImage);

        holder.itemView.setOnClickListener(v -> {
            if(listener != null){
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount(){
        return products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ItemProductGridSearchBinding binding;

        public ViewHolder(ItemProductGridSearchBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnProductClickListener{
        void onProductClick(Product product);
    }
}