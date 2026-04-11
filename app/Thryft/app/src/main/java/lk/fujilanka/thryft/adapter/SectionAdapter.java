package lk.fujilanka.thryft.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import lk.fujilanka.thryft.R;
import lk.fujilanka.thryft.databinding.ItemProductBinding;
import lk.fujilanka.thryft.model.Product;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private List<Product> productList;
    private OnProductClickListener listener;

    public SectionAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ItemProductBinding binding = ItemProductBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = productList.get(position);

        holder.binding.tvProductName.setText(product.getTitle());
        holder.binding.tvProductPrice.setText("LKR " + product.getPrice());
        holder.binding.tvProductCategory.setText("Size - "+product.getSize());

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(product.getImages().get(0))
                    .into(holder.binding.ivProductImage);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ItemProductBinding binding;

        public ViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}