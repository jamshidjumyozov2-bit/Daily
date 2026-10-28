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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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

    private RecyclerView rvCalendar, rvCategoryGrid;
    private TextView tvMonthlyTotalAmount;
    private LineChartView miniChart;
    
    private Calendar currentCalendar = Calendar.getInstance();
    private List<DayModel> calendarDays = new ArrayList<>();
    private CalendarAdapter calendarAdapter;
    private CategorySummaryAdapter categoryAdapter;
    private List<CategorySummary> categorySummaries = new ArrayList<>();
    private Locale uz = new Locale("uz");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly, container, false);

        rvCalendar = view.findViewById(R.id.rvCalendar);
        rvCategoryGrid = view.findViewById(R.id.rvCategoryGrid);
        tvMonthlyTotalAmount = view.findViewById(R.id.tvMonthlyTotalAmount);
        miniChart = view.findViewById(R.id.miniChart);
        
        if (miniChart != null) {
            miniChart.setMiniMode(true);
        }

        rvCalendar.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarAdapter = new CalendarAdapter(calendarDays, this::onDaySelected);
        rvCalendar.setAdapter(calendarAdapter);

        rvCategoryGrid.setLayoutManager(new GridLayoutManager(getContext(), 2));
        categoryAdapter = new CategorySummaryAdapter(categorySummaries, this::onCategorySelected);
        rvCategoryGrid.setAdapter(categoryAdapter);

        updateUI();

        return view;
    }

    public void updateData() {
        if (isAdded()) {
            updateUI();
        }
    }

    private void updateUI() {
        generateCalendar();
        updateGeneralStats();
    }

    private void generateCalendar() {
        calendarDays.clear();
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        // Adjust for Monday start (Uzbek style)
        int offset = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : firstDayOfWeek - 2;
        
        cal.add(Calendar.DAY_OF_MONTH, -offset);

        MainActivity activity = (MainActivity) getActivity();
        List<Expense> allExpenses = activity != null ? activity.getExpenses() : new ArrayList<>();

        for (int i = 0; i < 42; i++) {
            long startOfDay = getStartOfDay(cal.getTimeInMillis());
            long endOfDay = startOfDay + (24 * 60 * 60 * 1000L);
            
            double dayTotal = 0;
            for (Expense e : allExpenses) {
                if (e.timestamp >= startOfDay && e.timestamp < endOfDay) {
                    dayTotal += e.amount;
                }
            }

            boolean isCurrentMonth = cal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH);
            DayModel day = new DayModel(cal.get(Calendar.DAY_OF_MONTH), dayTotal, isCurrentMonth, startOfDay);
            
            // Default select today if in view
            Calendar today = Calendar.getInstance();
            if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && 
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                day.isSelected = true;
            }

            calendarDays.add(day);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendarAdapter.notifyDataSetChanged();
    }

    private void updateGeneralStats() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        long startOfMonth = getStartOfDay(cal.getTimeInMillis());
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        long endOfMonth = getStartOfDay(cal.getTimeInMillis()) + (24 * 60 * 60 * 1000L);

        double total = 0;
        Map<String, Double> categoryTotals = new HashMap<>();
        List<Expense> monthExpenses = new ArrayList<>();

        for (Expense e : activity.getExpenses()) {
            if (e.timestamp >= startOfMonth && e.timestamp < endOfMonth) {
                total += e.amount;
                monthExpenses.add(e);
                categoryTotals.put(e.category, categoryTotals.getOrDefault(e.category, 0.0) + e.amount);
            }
        }

        tvMonthlyTotalAmount.setText(String.format(uz, "%,.0f so'm", total).replace(",", " "));

        if (miniChart != null) {
            double[] dailyData = new double[monthExpenses.size()]; // This is actually wrong, should be daily
            // Let's create proper daily data for the chart
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            double[] chartData = new double[daysInMonth];
            double maxDay = 0;
            
            cal.set(Calendar.DAY_OF_MONTH, 1);
            for (int i = 0; i < daysInMonth; i++) {
                long start = getStartOfDay(cal.getTimeInMillis());
                long end = start + (24 * 60 * 60 * 1000L);
                double dTotal = 0;
                for (Expense e : monthExpenses) {
                    if (e.timestamp >= start && e.timestamp < end) dTotal += e.amount;
                }
                chartData[i] = dTotal;
                if (dTotal > maxDay) maxDay = dTotal;
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            miniChart.setData(chartData, maxDay, "");
        }
        
        updateCategoryBreakdown(categoryTotals, total);
    }

    private void updateCategoryBreakdown(Map<String, Double> totals, double grandTotal) {
        categorySummaries.clear();
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(totals.entrySet());
        sorted.sort((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()));

        for (Map.Entry<String, Double> entry : sorted) {
            int pct = grandTotal > 0 ? (int) (entry.getValue() * 100 / grandTotal) : 0;
            
            // Icon and color mapping
            String cat = entry.getKey();
            int icon = R.drawable.ic_more;
            int color = 0xFF10B981;
            if (cat.contains("Ovqat")) { icon = R.drawable.ic_food; color = 0xFFEF4444; }
            else if (cat.contains("Transport")) { icon = R.drawable.ic_transport; color = 0xFF3B82F6; }
            else if (cat.contains("Xarid")) { icon = R.drawable.ic_shopping; color = 0xFFA855F7; }
            
            categorySummaries.add(new CategorySummary(cat, entry.getValue(), pct, icon, color));
        }
        categoryAdapter.notifyDataSetChanged();
    }

    private void onCategorySelected(CategorySummary category) {
        // Handle category click - e.g. filter or open details
    }

    public void setMonthYear(int month, int year) {
        currentCalendar.set(Calendar.MONTH, month);
        currentCalendar.set(Calendar.YEAR, year);
        updateUI();
    }

    private void openDailyExpenses(long timestamp) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        long startOfDay = getStartOfDay(timestamp);
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000L);

        List<Expense> dayExpenses = new ArrayList<>();
        for (Expense e : activity.getExpenses()) {
            if (e.timestamp >= startOfDay && e.timestamp < endOfDay) {
                dayExpenses.add(e);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date(timestamp));

        DailyExpensesFragment fragment = DailyExpensesFragment.newInstance(dateStr, dayExpenses);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void onDaySelected(DayModel day) {
        for (DayModel d : calendarDays) d.isSelected = false;
        day.isSelected = true;
        calendarAdapter.notifyDataSetChanged();
        openDailyExpenses(day.timestamp);
    }

    private long getStartOfDay(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
}
