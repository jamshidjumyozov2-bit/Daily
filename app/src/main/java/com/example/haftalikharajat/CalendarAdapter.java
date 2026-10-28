package com.example.haftalikharajat;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import java.util.Locale;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<DayModel> days;
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(DayModel day);
    }

    public CalendarAdapter(List<DayModel> days, OnDayClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayModel day = days.get(position);
        holder.tvDayNumber.setText(String.valueOf(day.dayOfMonth));
        
        if (!day.isCurrentMonth) {
            holder.tvDayNumber.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.calendar_text_inactive));
            holder.indicatorDot.setVisibility(View.INVISIBLE);
            holder.tvDayAmount.setVisibility(View.GONE);
            holder.cardDay.setStrokeColor(Color.TRANSPARENT);
        } else {
            holder.tvDayNumber.setTextColor(Color.WHITE);
            if (day.amount > 0) {
                holder.indicatorDot.setVisibility(View.VISIBLE);
                holder.tvDayAmount.setVisibility(View.VISIBLE);
                holder.tvDayAmount.setText(String.format(new Locale("uz"), "%,.0f", day.amount).replace(",", " "));
                
                // Color logic based on screenshot
                int color = 0xFFEF4444; // Red
                if (position % 5 == 1) color = 0xFF3B82F6; // Blue
                else if (position % 5 == 2) color = 0xFFA855F7; // Purple
                else if (position % 5 == 3) color = 0xFF10B981; // Green
                else if (position % 5 == 4) color = 0xFFF59E0B; // Yellow
                
                holder.indicatorDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
                holder.tvDayAmount.setTextColor(color);
            } else {
                holder.indicatorDot.setVisibility(View.INVISIBLE);
                holder.tvDayAmount.setVisibility(View.GONE);
            }

            if (day.isSelected) {
                holder.cardDay.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.calendar_selected));
                holder.cardDay.setStrokeWidth(4);
            } else {
                holder.cardDay.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.calendar_stroke));
                holder.cardDay.setStrokeWidth(2);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onDayClick(day));
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber, tvDayAmount;
        View indicatorDot;
        MaterialCardView cardDay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvDayAmount = itemView.findViewById(R.id.tvDayAmount);
            indicatorDot = itemView.findViewById(R.id.indicatorDot);
            cardDay = itemView.findViewById(R.id.cardDay);
        }
    }
}
