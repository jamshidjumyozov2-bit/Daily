package com.example.haftalikharajat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.List;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder> {

    private final List<CategorySummary> items;
    private final OnCategoryClickListener listener;
    private final Locale uz = new Locale("uz");

    public interface OnCategoryClickListener {
        void onCategoryClick(CategorySummary category);
    }

    public CategorySummaryAdapter(List<CategorySummary> items, OnCategoryClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategorySummary item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvAmount.setText(String.format(uz, "%,.0f so'm", item.amount).replace(",", " "));
        holder.tvPercent.setText(String.format(uz, "%d%%", item.percent));
        holder.progress.setProgress(item.percent);
        holder.ivIcon.setImageResource(item.iconResId);
        holder.cardIcon.setCardBackgroundColor(item.color);
        holder.progress.setIndicatorColor(item.color);

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount, tvPercent;
        LinearProgressIndicator progress;
        ImageView ivIcon;
        MaterialCardView cardIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvAmount = itemView.findViewById(R.id.tvCategoryAmount);
            tvPercent = itemView.findViewById(R.id.tvCategoryPercent);
            progress = itemView.findViewById(R.id.progressCategory);
            ivIcon = itemView.findViewById(R.id.ivCategoryIcon);
            cardIcon = itemView.findViewById(R.id.cardCategoryIcon);
        }
    }
}
