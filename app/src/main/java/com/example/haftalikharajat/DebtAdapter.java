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
        if (debt == null) return;

        Locale uzLocale = new Locale("uz");
        if (holder.tvPerson != null) holder.tvPerson.setText(debt.personName != null ? debt.personName : "");
        if (holder.tvAmount != null) holder.tvAmount.setText(String.format(uzLocale, "%,.0f", debt.amount));
        if (holder.tvReason != null) holder.tvReason.setText(debt.reason != null ? debt.reason : "");
        
        if (holder.tvType != null) {
            holder.tvType.setText(debt.isOwedToMe ? "Menga berishadi" : "Men berishim kerak");
        }
        
        if (debt.isPaid) {
            if (holder.btnPay != null) holder.btnPay.setVisibility(View.GONE);
            if (holder.tvAmount != null) holder.tvAmount.setAlpha(0.5f);
            if (holder.tvStatus != null) {
                holder.tvStatus.setText("To'langan");
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#10B981"));
            }
        } else {
            if (holder.btnPay != null) {
                holder.btnPay.setVisibility(View.VISIBLE);
                holder.btnPay.setOnClickListener(v -> {
                    if (listener != null) listener.onPaid(debt);
                });
            }
            if (holder.tvAmount != null) holder.tvAmount.setAlpha(1.0f);
            
            // Calculate status
            long now = System.currentTimeMillis();
            if (now > debt.dateDue) {
                long diff = now - debt.dateDue;
                long days = diff / (24 * 60 * 60 * 1000);
                if (holder.tvStatus != null) {
                    holder.tvStatus.setText(days + " kun o'tdi");
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#EF4444"));
                }
            } else {
                if (holder.tvStatus != null) {
                    holder.tvStatus.setText("Faol");
                    holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#3B82F6"));
                }
            }
        }

        if (holder.btnDelete != null) {
            holder.btnDelete.setOnClickListener(v -> {
                new android.app.AlertDialog.Builder(v.getContext())
                        .setTitle("O'chirish")
                        .setMessage("Ushbu qarzni o'chirib tashlamoqchimisiz?")
                        .setPositiveButton("Ha", (dialog, which) -> {
                            if (listener != null) listener.onDelete(debt);
                        })
                        .setNegativeButton("Yo'q", null)
                        .show();
            });
        }

        if (holder.btnCall != null) {
            if (debt.phone != null && !debt.phone.isEmpty()) {
                holder.btnCall.setVisibility(View.VISIBLE);
                holder.btnCall.setOnClickListener(v -> {
                    try {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
                        intent.setData(android.net.Uri.parse("tel:" + debt.phone));
                        v.getContext().startActivity(intent);
                    } catch (Exception e) {
                        android.widget.Toast.makeText(v.getContext(), "Qo'ng'iroq qilib bo'lmadi", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                holder.btnCall.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return debts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPerson, tvAmount, tvReason, tvType, tvStatus;
        View btnPay, btnCall, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvPerson = itemView.findViewById(R.id.tvDebtPerson);
            tvAmount = itemView.findViewById(R.id.tvDebtAmount);
            tvReason = itemView.findViewById(R.id.tvDebtReason);
            tvType = itemView.findViewById(R.id.tvDebtType);
            tvStatus = itemView.findViewById(R.id.tvDebtStatus);
            btnPay = itemView.findViewById(R.id.btnPay);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}