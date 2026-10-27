package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyFragment extends Fragment {

    private LinearLayout layoutMonthlyChart, layoutMonthlyCategories;
    private TextView tvMonthlyTotalAmount, tvMonthlyDateRange;
    private TextView tvMonthlyMainTitle, tvMonthlySubTitle;
    private ImageView ivMonthlyTypeIcon;
    private RecyclerView rvMonthlyDays;
    private com.google.android.material.button.MaterialButtonToggleGroup toggleGroup;
    private Locale uz = new Locale("uz");
    private boolean isMonthlyMode = true; // true = 30 days, false = 4 weeks

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly, container, false);
        layoutMonthlyChart = view.findViewById(R.id.layoutMonthlyChart);
        layoutMonthlyCategories = view.findViewById(R.id.layoutMonthlyCategories);
        tvMonthlyTotalAmount = view.findViewById(R.id.tvMonthlyTotalAmount);
        tvMonthlyDateRange = view.findViewById(R.id.tvMonthlyDateRange);
        tvMonthlyMainTitle = view.findViewById(R.id.tvMonthlyMainTitle);
        tvMonthlySubTitle = view.findViewById(R.id.tvMonthlySubTitle);
        ivMonthlyTypeIcon = view.findViewById(R.id.ivMonthlyTypeIcon);
        rvMonthlyDays = view.findViewById(R.id.rvMonthlyDays);
        toggleGroup = view.findViewById(R.id.toggleGroupMonthly);

        rvMonthlyDays.setLayoutManager(new LinearLayoutManager(getContext()));

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isMonthlyMode = checkedId == R.id.btn30Days;
                updateData();
            }
        });

        updateData();
        return view;
    }

    public void updateData() {
        if (!isAdded()) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<Expense> all = activity.getExpenses();
        
        if (isMonthlyMode) {
            setup30DaysView(all);
        } else {
            setup4WeeksView(all);
        }
    }

    private void setup30DaysView(List<Expense> all) {
        tvMonthlyMainTitle.setText("30 kunlik xarajatlar");
        tvMonthlySubTitle.setText("Har bir kunning xarajati, ming so'mda.");
        ivMonthlyTypeIcon.setImageResource(R.drawable.ic_income);

        double[] dailyTotals = new double[30];
        List<List<Expense>> dailyExpenses = new ArrayList<>();
        for (int i = 0; i < 30; i++) dailyExpenses.add(new ArrayList<>());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long endTs = cal.getTimeInMillis() + 24 * 60 * 60 * 1000L;
        long startTs = endTs - 30L * 24 * 60 * 60 * 1000L;

        SimpleDateFormat sdfRange = new SimpleDateFormat("d MMM yyyy", uz);
        tvMonthlyDateRange.setText(sdfRange.format(new Date(startTs)) + " - " + sdfRange.format(new Date(System.currentTimeMillis())));

        double totalAmount = 0;
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense e : all) {
            if (e.timestamp >= startTs && e.timestamp < endTs) {
                int dayIndex = (int) ((e.timestamp - startTs) / (24 * 60 * 60 * 1000L));
                if (dayIndex >= 0 && dayIndex < 30) {
                    dailyTotals[dayIndex] += e.amount;
                    dailyExpenses.get(dayIndex).add(e);
                    totalAmount += e.amount;
                    categoryTotals.put(e.category, categoryTotals.getOrDefault(e.category, 0.0) + e.amount);
                }
            }
        }

        tvMonthlyTotalAmount.setText(String.format(uz, "%,.0f so'm", totalAmount));
        renderChart(dailyTotals, dailyExpenses, 30, "Kun ");
        updateCategories(categoryTotals, totalAmount);

        List<DaySummary> summaries = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            if (dailyTotals[i] > 0) {
                long dateTs = startTs + i * 24 * 60 * 60 * 1000L;
                summaries.add(new DaySummary(new SimpleDateFormat("d MMMM", uz).format(new Date(dateTs)), dailyTotals[i], dailyExpenses.get(i)));
            }
        }
        Collections.sort(summaries, (a, b) -> Double.compare(b.amount, a.amount));
        rvMonthlyDays.setAdapter(new MonthlyDayAdapter(summaries));
    }

    private void setup4WeeksView(List<Expense> all) {
        tvMonthlyMainTitle.setText("4 haftalik xarajatlar");
        tvMonthlySubTitle.setText("Har bir haftaning jami xarajatlari, so'mda.");
        ivMonthlyTypeIcon.setImageResource(R.drawable.ic_income);

        double[] weeklyTotals = new double[4];
        List<List<Expense>> weeklyExpenses = new ArrayList<>();
        for (int i = 0; i < 4; i++) weeklyExpenses.add(new ArrayList<>());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long endTs = cal.getTimeInMillis() + 24 * 60 * 60 * 1000L;
        long startTs = endTs - 28L * 24 * 60 * 60 * 1000L;

        SimpleDateFormat sdfRange = new SimpleDateFormat("d MMM yyyy", uz);
        tvMonthlyDateRange.setText(sdfRange.format(new Date(startTs)) + " - " + sdfRange.format(new Date(System.currentTimeMillis())));

        double totalAmount = 0;
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense e : all) {
            if (e.timestamp >= startTs && e.timestamp < endTs) {
                int weekIndex = (int) ((e.timestamp - startTs) / (7L * 24 * 60 * 60 * 1000L));
                if (weekIndex >= 0 && weekIndex < 4) {
                    weeklyTotals[weekIndex] += e.amount;
                    weeklyExpenses.get(weekIndex).add(e);
                    totalAmount += e.amount;
                    categoryTotals.put(e.category, categoryTotals.getOrDefault(e.category, 0.0) + e.amount);
                }
            }
        }

        tvMonthlyTotalAmount.setText(String.format(uz, "%,.0f so'm", totalAmount));
        renderChart(weeklyTotals, weeklyExpenses, 4, "-hafta");
        updateCategories(categoryTotals, totalAmount);

        List<DaySummary> summaries = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (weeklyTotals[i] > 0) {
                summaries.add(new DaySummary((i + 1) + "-hafta", weeklyTotals[i], weeklyExpenses.get(i)));
            }
        }
        rvMonthlyDays.setAdapter(new MonthlyDayAdapter(summaries));
    }

    private void renderChart(double[] values, List<List<Expense>> expenses, int count, String labelPrefix) {
        layoutMonthlyChart.removeAllViews();
        double maxValue = 0;
        for (double v : values) if (v > maxValue) maxValue = v;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        float maxChartHeightDp = 150f;
        float density = getResources().getDisplayMetrics().density;
        int barWidthDp = count == 4 ? 40 : 12;

        for (int i = 0; i < count; i++) {
            View barContainer = inflater.inflate(R.layout.item_day_bar, layoutMonthlyChart, false);
            View bar = barContainer.findViewById(R.id.viewBar);
            TextView tvDay = barContainer.findViewById(R.id.tvBarDay);

            String label = (labelPrefix.startsWith("-")) ? (i + 1) + labelPrefix : labelPrefix + (i + 1);
            tvDay.setText(label);
            tvDay.setTextSize(count == 4 ? 10 : 8);

            int height = maxValue > 0 ? (int) ((values[i] / maxValue) * maxChartHeightDp * density) : 0;
            if (height < 4 && values[i] > 0) height = (int) (4 * density);

            ViewGroup.LayoutParams lp = bar.getLayoutParams();
            lp.width = (int) (barWidthDp * density);
            lp.height = height;
            bar.setLayoutParams(lp);

            final int finalI = i;
            final String finalLabel = label;
            bar.setOnClickListener(v -> {
                if (!expenses.get(finalI).isEmpty()) {
                    openDayDetails(finalLabel, expenses.get(finalI));
                }
            });
            layoutMonthlyChart.addView(barContainer);
        }
    }

    private void updateCategories(Map<String, Double> categoryTotals, double total) {
        layoutMonthlyCategories.removeAllViews();
        if (total == 0) return;

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(categoryTotals.entrySet());
        Collections.sort(sorted, (a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (int i = 0; i < Math.min(4, sorted.size()); i++) {
            Map.Entry<String, Double> entry = sorted.get(i);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_history_simple, layoutMonthlyCategories, false);
            TextView tvName = view.findViewById(R.id.tvHistoryName);
            TextView tvAmount = view.findViewById(R.id.tvHistoryAmount);
            ImageView ivIcon = view.findViewById(R.id.ivHistoryIcon);

            tvName.setText(entry.getKey());
            double percent = (entry.getValue() / total) * 100;
            tvAmount.setText(String.format(uz, "%,.0f so'm (%.0f%%)", entry.getValue(), percent));
            
            // Map icon
            int iconRes = R.drawable.ic_more;
            String cat = entry.getKey().toLowerCase();
            if (cat.contains("ovqat")) iconRes = R.drawable.ic_food;
            else if (cat.contains("transport")) iconRes = R.drawable.ic_transport;
            else if (cat.contains("xarid") || cat.contains("shopping")) iconRes = R.drawable.ic_shopping;
            ivIcon.setImageResource(iconRes);
            ivIcon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary)));

            layoutMonthlyCategories.addView(view);
        }
    }

    private void openDayDetails(String title, List<Expense> expenses) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.fragment_container, DailyExpensesFragment.newInstance(title, expenses))
                .addToBackStack(null)
                .commit();
    }

    static class DaySummary {
        String date;
        double amount;
        List<Expense> expenses;
        DaySummary(String date, double amount, List<Expense> expenses) {
            this.date = date;
            this.amount = amount;
            this.expenses = expenses;
        }
    }

    class MonthlyDayAdapter extends RecyclerView.Adapter<MonthlyDayAdapter.ViewHolder> {
        List<DaySummary> data;
        MonthlyDayAdapter(List<DaySummary> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_income, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DaySummary item = data.get(position);
            holder.tvName.setText(item.date);
            holder.tvAmount.setText(String.format(uz, "%,.0f so'm", item.amount));
            holder.tvName.setTextColor(getResources().getColor(R.color.white));
            holder.tvAmount.setTextColor(getResources().getColor(R.color.white));
            
            holder.v.findViewById(R.id.btnEditInc).setVisibility(View.GONE);
            holder.v.findViewById(R.id.btnDeleteInc).setVisibility(View.GONE);
            holder.v.findViewById(R.id.tvIncAccountType).setVisibility(View.GONE);

            if (holder.v instanceof com.google.android.material.card.MaterialCardView) {
                ((com.google.android.material.card.MaterialCardView) holder.v).setCardBackgroundColor(android.content.res.ColorStateList.valueOf(0x1AFFFFFF));
            }

            holder.itemView.setOnClickListener(v -> openDayDetails(item.date, item.expenses));
        }

        @Override
        public int getItemCount() { return data.size(); }

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
