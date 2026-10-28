package com.example.haftalikharajat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class MonthAdapter extends RecyclerView.Adapter<MonthAdapter.ViewHolder> {

    public interface OnMonthClickListener {
        void onMonthClick(MonthModel month);
    }

    private List<MonthModel> monthList;
    private OnMonthClickListener listener;

    public MonthAdapter(List<MonthModel> monthList, OnMonthClickListener listener) {
        this.monthList = monthList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_month_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthModel month = monthList.get(position);
        holder.tvMonthName.setText(month.name);
        holder.tvMonthAmount.setText(String.format(new Locale("uz"), "%,.0f", month.amount).replace(",", " "));
        holder.viewIndicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(month.color));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onMonthClick(month);
        });
    }

    @Override
    public int getItemCount() {
        return monthList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonthName, tvMonthAmount;
        View viewIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonthName = itemView.findViewById(R.id.tvMonthName);
            tvMonthAmount = itemView.findViewById(R.id.tvMonthAmount);
            viewIndicator = itemView.findViewById(R.id.viewMonthIndicator);
        }
    }
}
