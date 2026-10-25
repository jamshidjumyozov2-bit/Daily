package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PeriodFragment extends Fragment {

    private int type;
    private TextView tvPeriodHistory;
    private String filterDate = "";

    public void setFilterDate(String date) {
        this.filterDate = date;
        updateData();
    }

    public static PeriodFragment newInstance(int type) {
        PeriodFragment fragment = new PeriodFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_period, container, false);
        tvPeriodHistory = view.findViewById(R.id.tvPeriodHistory);
        updateData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    public void updateData() {
        if (!isAdded()) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<Expense> allExpenses = activity.getExpenses();
        List<Expense> filtered = filterExpenses(allExpenses);
        
        StringBuilder history = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        for (Expense e : filtered) {
            history.append(String.format(Locale.getDefault(), "%s | %s | %,.0f so'm\n", 
                sdf.format(e.timestamp), e.name, e.amount));
        }
        tvPeriodHistory.setText(history.length() > 0 ? history.toString() : "Tarix bo'sh");
    }

    private List<Expense> filterExpenses(List<Expense> all) {
        List<Expense> filtered = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        long now = System.currentTimeMillis();

        if (filterDate != null && !filterDate.isEmpty()) {
            for (Expense e : all) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String expenseDate = sdf.format(e.timestamp);
                if (expenseDate.equals(filterDate)) {
                    filtered.add(e);
                }
            }
            return filtered;
        }
        
        for (Expense e : all) {
            cal.setTimeInMillis(now);
            long startTime;
            
            switch (type) {
                case 0: // Daily
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    startTime = cal.getTimeInMillis();
                    break;
                case 1: // Weekly
                    cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    startTime = cal.getTimeInMillis();
                    break;
                case 2: // Monthly
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    startTime = cal.getTimeInMillis();
                    break;
                case 3: // Yearly
                default:
                    cal.set(Calendar.DAY_OF_YEAR, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    startTime = cal.getTimeInMillis();
                    break;
            }
            
            if (e.timestamp >= startTime) {
                filtered.add(e);
            }
        }
        return filtered;
    }
}