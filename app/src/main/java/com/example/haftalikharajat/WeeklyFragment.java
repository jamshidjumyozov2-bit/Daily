package com.example.haftalikharajat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeeklyFragment extends Fragment {

    private LinearLayout layoutChart;
    private TextView tvAveragePrice;
    private RecyclerView rvDays;
    private String[] dayLabels = {"@", "s", "ch", "p", "j", "sh", "y"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly, container, false);
        layoutChart = view.findViewById(R.id.layoutChart);
        tvAveragePrice = view.findViewById(R.id.tvAveragePrice);
        rvDays = view.findViewById(R.id.rvDays);
        
        rvDays.setLayoutManager(new LinearLayoutManager(getContext()));
        updateData();
        return view;
    }

    public void updateData() {
        if (!isAdded()) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<Expense> all = activity.getExpenses();
        double[] dailyTotals = new double[7];
        List<List<Expense>> dailyExpenses = new ArrayList<>();
        for (int i = 0; i < 7; i++) dailyExpenses.add(new ArrayList<>());

        Calendar cal = Calendar.getInstance();
        // Set to start of current week (Monday)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long weekStart = cal.getTimeInMillis();

        for (Expense e : all) {
            if (e.timestamp >= weekStart && e.timestamp < weekStart + 7 * 24 * 60 * 60 * 1000L) {
                cal.setTimeInMillis(e.timestamp);
                int day = cal.get(Calendar.DAY_OF_WEEK); // Sunday=1, Monday=2...
                int index = (day + 5) % 7; // Map Monday to 0, Sunday to 6
                dailyTotals[index] += e.amount;
                dailyExpenses.get(index).add(e);
            }
        }

        double total = 0;
        double max = 0;
        int activeDays = 0;
        for (double d : dailyTotals) {
            total += d;
            if (d > max) max = d;
            if (d > 0) activeDays++;
        }
        double avg = activeDays > 0 ? total / activeDays : 0;
        tvAveragePrice.setText(String.format(Locale.getDefault(), "o'rtacha narx %,.0f", avg));

        layoutChart.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < 7; i++) {
            View barView = inflater.inflate(R.layout.item_day_bar, layoutChart, false);
            View bar = barView.findViewById(R.id.viewBar);
            TextView tvDay = barView.findViewById(R.id.tvBarDay);
            TextView tvAmt = barView.findViewById(R.id.tvBarAmount);

            tvDay.setText(dayLabels[i]);
            tvAmt.setText(dailyTotals[i] > 0 ? String.format(Locale.getDefault(), "%.0f", dailyTotals[i]/1000) + "k" : "");
            
            int height = max > 0 ? (int) ((dailyTotals[i] / max) * 150 * getResources().getDisplayMetrics().density) : 0;
            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            lp.height = height;
            bar.setLayoutParams(lp);

            layoutChart.addView(barView);
        }

        rvDays.setAdapter(new DayAdapter(dailyExpenses));
    }

    class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        List<List<Expense>> data;
        String[] fullDayNames = {"Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba", "Yakshanba"};

        DayAdapter(List<List<Expense>> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_income, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            double dayTotal = 0;
            for (Expense e : data.get(position)) dayTotal += e.amount;

            holder.tvName.setText(fullDayNames[position]);
            holder.tvAmount.setText(String.format(Locale.getDefault(), "%,.0f so'm", dayTotal));
            holder.tvAmount.setTextColor(getResources().getColor(R.color.black));
            
            holder.itemView.setOnClickListener(v -> {
                showDayDetails(fullDayNames[position], data.get(position));
            });
        }

        @Override
        public int getItemCount() { return 7; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAmount;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvIncSource);
                tvAmount = v.findViewById(R.id.tvIncAmount);
            }
        }
    }

    private void showDayDetails(String dayName, List<Expense> expenses) {
        if (expenses.isEmpty()) {
            Toast.makeText(getContext(), dayName + " kuni xarajatlar yo'q", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        for (Expense e : expenses) {
            sb.append(String.format(Locale.getDefault(), "%s | %s | %,.0f so'm\n", sdf.format(e.timestamp), e.name, e.amount));
        }

        new AlertDialog.Builder(getContext())
                .setTitle(dayName + " xarajatlari")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}