package com.example.haftalikharajat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IncomeAdapter extends RecyclerView.Adapter<IncomeAdapter.ViewHolder> {

    private List<Income> incomeList;
    private OnIncomeClickListener listener;

    public interface OnIncomeClickListener {
        void onDeleteClick(Income income);
        void onEditClick(Income income);
    }

    public IncomeAdapter(List<Income> incomeList, OnIncomeClickListener listener) {
        this.incomeList = incomeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_income, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Income income = incomeList.get(position);
        holder.tvSource.setText(income.source);
        
        SimpleDateFormat sdf = new SimpleDateFormat("d-MMMM", new Locale("uz"));
        String dateStr = sdf.format(income.timestamp);
        String subtitle = (income.accountType != null ? income.accountType : "Karta") + " · " + dateStr;
        holder.tvAccountType.setText(subtitle);
        
        holder.tvAmount.setText(String.format(Locale.getDefault(), "+%,.0f so'm", income.amount));

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(income));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(income));
    }

    @Override
    public int getItemCount() {
        return incomeList.size();
    }

    public void updateList(List<Income> newList) {
        this.incomeList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSource, tvAccountType, tvAmount;
        ImageButton btnDelete, btnEdit;

        ViewHolder(View view) {
            super(view);
            tvSource = view.findViewById(R.id.tvIncSource);
            tvAccountType = view.findViewById(R.id.tvIncAccountType);
            tvAmount = view.findViewById(R.id.tvIncAmount);
            btnDelete = view.findViewById(R.id.btnDeleteInc);
            btnEdit = view.findViewById(R.id.btnEditInc);
        }
    }
}
