package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YearlyFragment extends Fragment {

    private TextView tvYearlyTotalAmount, tvYearlyGrowth;
    private RecyclerView rvMonthsGrid, rvYearlyCategoryList;
    private MonthAdapter monthAdapter;
    private YearlyCategoryAdapter categoryAdapter;
    private DonutChartView donutChartView;
    private List<MonthModel> monthList = new ArrayList<>();
    private List<CategorySummary> categorySummaries = new ArrayList<>();
    private Calendar currentCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_yearly, container, false);

        tvYearlyTotalAmount = view.findViewById(R.id.tvYearlyTotalAmount);
        tvYearlyGrowth = view.findViewById(R.id.tvYearlyGrowth);
        rvMonthsGrid = view.findViewById(R.id.rvMonthsGrid);
        rvYearlyCategoryList = view.findViewById(R.id.rvYearlyCategoryList);
        donutChartView = view.findViewById(R.id.donutChart);

        rvMonthsGrid.setLayoutManager(new GridLayoutManager(getContext(), 3));
        monthAdapter = new MonthAdapter(monthList, this::onMonthClicked);
        rvMonthsGrid.setAdapter(monthAdapter);

        rvYearlyCategoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new YearlyCategoryAdapter(categorySummaries);
        rvYearlyCategoryList.setAdapter(categoryAdapter);

        updateData();

        return view;
    }

    public void updateData() {
        if (!isAdded()) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<Expense> allExpenses = activity.getExpenses();
        int currentYear = currentCalendar.get(Calendar.YEAR);
        
        double yearlyTotal = 0;
        double lastYearTotal = 0;
        double[] monthlyTotals = new double[12];
        Map<String, Double> categoryTotals = new HashMap<>();

        Calendar cal = Calendar.getInstance();
        for (Expense e : allExpenses) {
            cal.setTimeInMillis(e.timestamp);
            int year = cal.get(Calendar.YEAR);
            if (year == currentYear) {
                yearlyTotal += e.amount;
                monthlyTotals[cal.get(Calendar.MONTH)] += e.amount;
                categoryTotals.put(e.category, categoryTotals.getOrDefault(e.category, 0.0) + e.amount);
            } else if (year == currentYear - 1) {
                lastYearTotal += e.amount;
            }
        }

        tvYearlyTotalAmount.setText(String.format(new Locale("uz"), "%,.0f so'm", yearlyTotal).replace(",", " "));
        
        if (lastYearTotal > 0) {
            double growth = ((yearlyTotal - lastYearTotal) / lastYearTotal) * 100;
            tvYearlyGrowth.setText(String.format(Locale.US, "%+.0f%%", growth));
            tvYearlyGrowth.setTextColor(growth >= 0 ? 0xFF10B981 : 0xFFEF4444);
        } else {
            tvYearlyGrowth.setText("0%");
        }

        // Update Months
        monthList.clear();
        String[] monthNames = {"Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun", "Iyul", "Avgust", "Sentabr", "Oktabr", "Noyabr", "Dekabr"};
        int[] colors = {0xFF2563EB, 0xFFA855F7, 0xFFF59E0B, 0xFF10B981, 0xFFEC4899, 0xFF8B5CF6, 0xFF3B82F6, 0xFF6366F1, 0xFFF97316, 0xFF06B6D4, 0xFF84CC16, 0xFFEF4444};
        
        for (int i = 0; i < 12; i++) {
            monthList.add(new MonthModel(monthNames[i], monthlyTotals[i], colors[i % colors.length], i));
        }
        monthAdapter.notifyDataSetChanged();

        // Update Categories
        categorySummaries.clear();
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(categoryTotals.entrySet());
        sorted.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        for (Map.Entry<String, Double> entry : sorted) {
            double rawPct = yearlyTotal > 0 ? (entry.getValue() * 100.0 / yearlyTotal) : 0;
            int pct = (int) Math.round(rawPct);
            String cat = entry.getKey();
            int icon = R.drawable.ic_more;
            int color = 0xFF10B981;
            
            // Map more categories to colors/icons
            if (cat.equalsIgnoreCase("Oziq-ovqat") || cat.contains("Ovqat")) { icon = R.drawable.ic_food; color = 0xFFEF4444; }
            else if (cat.equalsIgnoreCase("Transport")) { icon = R.drawable.ic_transport; color = 0xFF3B82F6; }
            else if (cat.contains("Xarid") || cat.contains("Shopping")) { icon = R.drawable.ic_shopping; color = 0xFFA855F7; }
            else if (cat.equalsIgnoreCase("Uy-joy")) { icon = R.drawable.ic_home; color = 0xFFF59E0B; }
            else if (cat.equalsIgnoreCase("O'yin-kulgi")) { icon = R.drawable.ic_entertainment; color = 0xFFEC4899; }
            else {
                // Generate a consistent color based on category name
                color = getCategoryColor(cat);
            }
            
            categorySummaries.add(new CategorySummary(cat, entry.getValue(), pct, icon, color));
        }
        categoryAdapter.notifyDataSetChanged();

        if (donutChartView != null) {
            String shortTotal = yearlyTotal > 0 ? formatShort(yearlyTotal) : "0";
            donutChartView.setData(categorySummaries, shortTotal);
        }
    }

    private int getCategoryColor(String name) {
        int hash = name.hashCode();
        int[] palette = {0xFF10B981, 0xFF3B82F6, 0xFFF59E0B, 0xFFEF4444, 0xFFA855F7, 0xFFEC4899, 0xFF06B6D4, 0xFF84CC16};
        return palette[Math.abs(hash) % palette.length];
    }

    private String formatShort(double amount) {
        if (amount >= 1000000) return String.format(Locale.US, "%.1fM", amount / 1000000.0);
        if (amount >= 1000) return String.format(Locale.US, "%.0fK", amount / 1000.0);
        return String.format(Locale.US, "%.0f", amount);
    }
    
    public void setYear(int year) {
        currentCalendar.set(Calendar.YEAR, year);
        updateData();
    }

    private void onMonthClicked(MonthModel month) {
        int year = currentCalendar.get(Calendar.YEAR);
        MonthlyFragment fragment = MonthlyFragment.newInstance(month.monthIndex, year);
        
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
