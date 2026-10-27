package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class DailyExpensesFragment extends Fragment {

    private String dayName;
    private List<Expense> expenses;

    public static DailyExpensesFragment newInstance(String dayName, List<Expense> expenses) {
        DailyExpensesFragment fragment = new DailyExpensesFragment();
        Bundle args = new Bundle();
        args.putString("dayName", dayName);
        args.putString("expensesJson", new Gson().toJson(expenses));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayName = getArguments().getString("dayName");
            String json = getArguments().getString("expensesJson");
            expenses = new Gson().fromJson(json, new TypeToken<List<Expense>>(){}.getType());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_list, container, false);
        
        TextView tvTitle = view.findViewById(R.id.tvDailyTitle);
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        RecyclerView rv = view.findViewById(R.id.rvDailyExpenses);

        tvTitle.setText(dayName + " xarajatlari");
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new ExpenseAdapter(expenses));

        return view;
    }

    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
        private final List<Expense> data;

        ExpenseAdapter(List<Expense> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Expense e = data.get(position);
            holder.tvName.setText(e.name);
            holder.tvCategory.setText(e.category);
            holder.tvAmount.setText(String.format("%,.0f so'm", e.amount));
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvCategory, tvAmount;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvExpName);
                tvCategory = v.findViewById(R.id.tvExpCategory);
                tvAmount = v.findViewById(R.id.tvExpAmount);
            }
        }
    }
}