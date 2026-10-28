package com.example.haftalikharajat;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class YearlyCategoryAdapter extends RecyclerView.Adapter<YearlyCategoryAdapter.ViewHolder> {

    private final List<CategorySummary> items;

    public YearlyCategoryAdapter(List<CategorySummary> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_yearly_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategorySummary item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvPercent.setText(item.percent + "%");
        holder.viewColor.setBackgroundTintList(ColorStateList.valueOf(item.color));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPercent;
        View viewColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            tvPercent = itemView.findViewById(R.id.tvCategoryPercent);
            viewColor = itemView.findViewById(R.id.viewCategoryColor);
        }
    }
}
