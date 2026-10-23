package com.example.haftalikharajat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.ViewHolder> {

    private List<Debt> debts;
    private OnDebtActionListener listener;

    public interface OnDebtActionListener {
        void onPaid(Debt debt);
        void onDelete(Debt debt);
    }

    public DebtAdapter(List<Debt> debts, OnDebtActionListener listener) {
        this.debts = debts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_debt, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Debt debt = debts.get(position);
        holder.tvPerson.setText(debt.personName);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "%,.0f so'm", debt.amount));
        holder.tvReason.setText(debt.reason);
        holder.tvPhone.setText(debt.phone);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        holder.tvDue.setText("Muddat: " + sdf.format(debt.dateDue));
        
        holder.tvType.setText(debt.isOwedToMe ? "Menga berishadi" : "Men berishim kerak");
        holder.tvType.setTextColor(0xFF000000);
        holder.tvType.setBackgroundResource(R.drawable.badge_bg);
        // Remove direct background color setting to use drawable badge_bg which is now black based on primary color

        if (debt.isPaid) {
            holder.btnPay.setVisibility(View.GONE);
            holder.tvAmount.setAlpha(0.5f);
        } else {
            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setOnClickListener(v -> listener.onPaid(debt));
        }

        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(debt);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return debts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPerson, tvAmount, tvReason, tvPhone, tvDue, tvType;
        Button btnPay;

        ViewHolder(View itemView) {
            super(itemView);
            tvPerson = itemView.findViewById(R.id.tvDebtPerson);
            tvAmount = itemView.findViewById(R.id.tvDebtAmount);
            tvReason = itemView.findViewById(R.id.tvDebtReason);
            tvPhone = itemView.findViewById(R.id.tvDebtPhone);
            tvDue = itemView.findViewById(R.id.tvDebtDue);
            tvType = itemView.findViewById(R.id.tvDebtType);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
}