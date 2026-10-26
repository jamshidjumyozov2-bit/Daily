package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvTotalBalance, tvTotalIncome, tvTotalExpense, tvWeekInfo, tvWeeklyLimit, tvLimitPercent;
    private EditText etExpName, etExpAmount, etExpDate, etExpTime;
    private com.google.android.material.chip.ChipGroup chipGroupCategory;
    private android.widget.ProgressBar pbWeeklyLimit;
    private Button btnSaveExp;
    private long selectedTimestamp = System.currentTimeMillis();
    private String selectedCategory = "Boshqa";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        tvWeekInfo = view.findViewById(R.id.tvWeekInfo);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        tvWeeklyLimit = view.findViewById(R.id.tvWeeklyLimit);
        tvLimitPercent = view.findViewById(R.id.tvLimitPercent);
        pbWeeklyLimit = view.findViewById(R.id.pbWeeklyLimit);
        
        etExpName = view.findViewById(R.id.etExpName);
        etExpAmount = view.findViewById(R.id.etExpAmount);
        etExpDate = view.findViewById(R.id.etExpDate);
        etExpTime = view.findViewById(R.id.etExpTime);
        chipGroupCategory = view.findViewById(R.id.chipGroupCategory);
        btnSaveExp = view.findViewById(R.id.btnSaveExp);

        setupCategories();
        updateUI();

        etExpDate.setOnClickListener(v -> showDatePicker());
        etExpTime.setOnClickListener(v -> showTimePicker());

        btnSaveExp.setOnClickListener(v -> {
            String name = etExpName.getText().toString().trim();
            String amountStr = etExpAmount.getText().toString().trim();
            if (name.isEmpty() || amountStr.isEmpty()) return;

            double amount = Double.parseDouble(amountStr);
            ((MainActivity) getActivity()).addExpense(name, amount, selectedCategory, selectedTimestamp);
            
            etExpName.setText("");
            etExpAmount.setText("");
            updateUI();
            Toast.makeText(getContext(), "Xarajat saqlandi", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void setupCategories() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;
        
        List<String> categories = activity.getCategories();
        chipGroupCategory.removeAllViews();
        for (String cat : categories) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(getContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setChipBackgroundColorResource(R.color.bg_input);
            chip.setTextColor(getResources().getColor(R.color.text_primary));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) selectedCategory = cat;
            });
            chipGroupCategory.addView(chip);
        }
    }

    private void showDatePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(selectedTimestamp);
        new android.app.DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            cal.set(java.util.Calendar.YEAR, year);
            cal.set(java.util.Calendar.MONTH, month);
            cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);
            selectedTimestamp = cal.getTimeInMillis();
            updateDateLabel();
        }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(selectedTimestamp);
        new android.app.TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            cal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(java.util.Calendar.MINUTE, minute);
            selectedTimestamp = cal.getTimeInMillis();
            updateTimeLabel();
        }, cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), true).show();
    }

    private void updateDateLabel() {
        etExpDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedTimestamp));
    }

    private void updateTimeLabel() {
        etExpTime.setText(new java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTimestamp));
    }

    public void updateUI() {
        if (!isAdded()) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            setupCategories(); // Refresh categories order
            double balance = activity.getBalance();
            Locale uzLocale = new Locale("uz");
            tvTotalBalance.setText(String.format(uzLocale, "%,.0f so'm", balance));
            
            // Calculate income and expense for the current week
            double weeklyIncome = 0;
            double weeklyExpense = 0;
            
            Calendar cal = Calendar.getInstance();
            int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
            int currentYear = cal.get(Calendar.YEAR);

            for (Expense e : activity.getExpenses()) {
                Calendar eCal = Calendar.getInstance();
                eCal.setTimeInMillis(e.timestamp);
                if (eCal.get(Calendar.WEEK_OF_YEAR) == currentWeek && eCal.get(Calendar.YEAR) == currentYear) {
                    weeklyExpense += e.amount;
                }
            }
            
            for (Income i : activity.getIncomes()) {
                Calendar iCal = Calendar.getInstance();
                iCal.setTimeInMillis(i.timestamp);
                if (iCal.get(Calendar.WEEK_OF_YEAR) == currentWeek && iCal.get(Calendar.YEAR) == currentYear) {
                    weeklyIncome += i.amount;
                }
            }
            
            tvTotalIncome.setText(String.format(uzLocale, "+%,.0f", weeklyIncome));
            tvTotalExpense.setText(String.format(uzLocale, "-%,.0f", weeklyExpense));
            
            tvWeekInfo.setText("JAMI BALANS HAFTA " + currentWeek);
            
            updateDateLabel();
            updateTimeLabel();

            // Handle weekly limit
            double limit = 3000000;
            tvWeeklyLimit.setText(String.format(uzLocale, "Haftalik limit: %,.0f so'm", limit));
            int percent = (int) ((weeklyExpense / limit) * 100);
            if (percent > 100) percent = 100;
            tvLimitPercent.setText(percent + "%");
            pbWeeklyLimit.setProgress(percent);
        }
    }
}
