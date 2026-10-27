package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class WeeklyFragment extends Fragment {

    private LinearLayout layoutChart;
    private TextView tvAveragePrice, tvTotalWeeklyExpense, tvMaxDay, tvActiveDays;
    private RecyclerView rvDays;
    private String[] dayLabels = {"D", "S", "Ch", "P", "J", "Sh", "Y"};
    private String[] fullDayNames = {"Dushanba", "Seshanba", "Chorshanba", "Payshanba", "Juma", "Shanba", "Yakshanba"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly, container, false);
        layoutChart = view.findViewById(R.id.layoutChart);
        tvAveragePrice = view.findViewById(R.id.tvAveragePrice);
        tvTotalWeeklyExpense = view.findViewById(R.id.tvTotalWeeklyExpense);
        tvMaxDay = view.findViewById(R.id.tvMaxDay);
        tvActiveDays = view.findViewById(R.id.tvActiveDays);
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
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long weekStart = cal.getTimeInMillis();

        for (Expense e : all) {
            if (e.timestamp >= weekStart && e.timestamp < weekStart + 7 * 24 * 60 * 60 * 1000L) {
                cal.setTimeInMillis(e.timestamp);
                int day = cal.get(Calendar.DAY_OF_WEEK); 
                int index = (day + 5) % 7; 
                dailyTotals[index] += e.amount;
                dailyExpenses.get(index).add(e);
            }
        }

        double total = 0;
        double max = 0;
        int maxIndex = 0;
        int activeCount = 0;
        for (int i = 0; i < 7; i++) {
            double d = dailyTotals[i];
            total += d;
            if (d > max) {
                max = d;
                maxIndex = i;
            }
            if (d > 0) activeCount++;
        }
        
        double avg = total / 7;
        Locale uz = new Locale("uz");
        tvAveragePrice.setText(String.format(uz, "%,.0f so'm", avg));
        tvTotalWeeklyExpense.setText(String.format(uz, "%,.0f so'm", total));
        tvMaxDay.setText(String.format(uz, "%s (%,.0f k)", fullDayNames[maxIndex], max / 1000));
        tvActiveDays.setText(String.format(uz, "%d kun / 7 kun", activeCount));

        layoutChart.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        float maxChartHeightDp = 180f;
        float density = getResources().getDisplayMetrics().density;
        
        for (int i = 0; i < 7; i++) {
            View barContainer = inflater.inflate(R.layout.item_day_bar, layoutChart, false);
            View bar = barContainer.findViewById(R.id.viewBar);
            TextView tvDay = barContainer.findViewById(R.id.tvBarDay);

            tvDay.setText(dayLabels[i]);
            
            int height = max > 0 ? (int) ((dailyTotals[i] / max) * maxChartHeightDp * density) : 0;
            if (height < 10 && dailyTotals[i] > 0) height = (int)(10 * density);

            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            lp.height = height;
            bar.setLayoutParams(lp);

            final int finalI = i;
            bar.setOnClickListener(v -> openDayDetails(fullDayNames[finalI], dailyExpenses.get(finalI)));
            
            layoutChart.addView(barContainer);
        }

        rvDays.setAdapter(new DayAdapter(dailyExpenses));
    }

    private void openDayDetails(String dayName, List<Expense> expenses) {
        if (expenses.isEmpty()) return;
        
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.fragment_container, DailyExpensesFragment.newInstance(dayName, expenses))
                .addToBackStack(null)
                .commit();
    }

    class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        List<List<Expense>> data;

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
            holder.tvAmount.setText(String.format(new Locale("uz"), "%,.0f so'm", dayTotal));
            holder.tvAmount.setTextColor(getResources().getColor(R.color.dark_text_primary));
            holder.tvName.setTextColor(getResources().getColor(R.color.dark_text_primary));
            
            // Hide delete/edit buttons for this summary view
            holder.v.findViewById(R.id.btnEditInc).setVisibility(View.GONE);
            holder.v.findViewById(R.id.btnDeleteInc).setVisibility(View.GONE);
            holder.v.findViewById(R.id.tvIncAccountType).setVisibility(View.GONE);
            
            // Make card match dark theme if possible (it's using item_income which has a background)
            // But better to just update colors here
            View card = holder.v;
            if (card instanceof com.google.android.material.card.MaterialCardView) {
                ((com.google.android.material.card.MaterialCardView) card).setCardBackgroundColor(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.dark_card)));
                ((com.google.android.material.card.MaterialCardView) card).setStrokeColor(android.content.res.ColorStateList.valueOf(0));
            }

            holder.itemView.setOnClickListener(v -> openDayDetails(fullDayNames[position], data.get(position)));
        }

        @Override
        public int getItemCount() { return 7; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvAmount;
            View v;
            ViewHolder(View v) {
                super(v);
                this.v = v;
                tvName = v.findViewById(R.id.tvIncSource);
                tvAmount = v.findViewById(R.id.tvIncAmount);
            }
        }
    }
}