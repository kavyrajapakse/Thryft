package lk.fujilanka.thryft.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.fujilanka.thryft.R;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    public interface OnSearchClick {
        void onClick(String query);
        void onDelete(String query);
    }

    private List<String> searches;
    private OnSearchClick listener;

    public RecentSearchAdapter(List<String> searches, OnSearchClick listener) {
        this.searches = searches;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String query = searches.get(position);

        holder.tvQuery.setText(query);

        holder.itemView.setOnClickListener(v -> listener.onClick(query));

        holder.ivDelete.setOnClickListener(v -> listener.onDelete(query));
    }

    @Override
    public int getItemCount() {
        return searches.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuery;
        ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvQuery = itemView.findViewById(R.id.tv_search_query);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}