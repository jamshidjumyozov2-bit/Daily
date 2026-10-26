package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.DatePickerDialog;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class IncomeFragment extends Fragment {

    private EditText etIncSource, etIncAmount, etIncDate;
    private AutoCompleteTextView actvAccountType;
    private Button btnSaveInc;
    private TextView tvIncomeCount, tvTotalMonthlyIncome, tvSummaryIncCount, tvTopSource;
    private RecyclerView rvIncomes;
    private IncomeAdapter adapter;
    private Calendar calendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);
        
        initializeViews(view);
        setupDatePickers(view);
        setupAccountTypeDropdown();
        setupRecyclerView();
        
        btnSaveInc.setOnClickListener(v -> saveIncome());

        updateList();

        return view;
    }

    private void initializeViews(View view) {
        etIncSource = view.findViewById(R.id.etIncSource);
        etIncAmount = view.findViewById(R.id.etIncAmount);
        etIncDate = view.findViewById(R.id.etIncDate);
        actvAccountType = view.findViewById(R.id.actvAccountType);
        btnSaveInc = view.findViewById(R.id.btnSaveInc);
        tvIncomeCount = view.findViewById(R.id.tvIncomeCount);
        tvTotalMonthlyIncome = view.findViewById(R.id.tvTotalMonthlyIncome);
        tvSummaryIncCount = view.findViewById(R.id.tvSummaryIncCount);
        tvTopSource = view.findViewById(R.id.tvTopSource);
        rvIncomes = view.findViewById(R.id.rvIncomes);
    }

    private void setupDatePickers(View view) {
        updateLabel();
        
        View.OnClickListener dateClickListener = v -> {
            if (!isAdded()) return;
            new DatePickerDialog(requireContext(), (view1, year, month, day) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateLabel();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        etIncDate.setOnClickListener(dateClickListener);
        
        View ivCalendarIcon = view.findViewById(R.id.ivCalendarIcon);
        if (ivCalendarIcon != null) {
            ivCalendarIcon.setOnClickListener(dateClickListener);
        }
        
        View rlDateContainer = view.findViewById(R.id.rlDateContainer);
        if (rlDateContainer != null) {
            rlDateContainer.setOnClickListener(dateClickListener);
        }
    }

    private void setupAccountTypeDropdown() {
        if (!isAdded()) return;
        String[] accountTypes = {
            "Karta (Uzcard/Humo)", 
            "Naqd pul", 
            "Oylik maosh", 
            "Biznes/Savdo", 
            "Frilans", 
            "Sovg'a", 
            "Bank depozit", 
            "Payme/Click", 
            "Boshqa"
        };
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, accountTypes);
        actvAccountType.setAdapter(accountAdapter);
        
        // Dropdownni bosganda darrov ro'yxatni chiqarish uchun
        actvAccountType.setOnClickListener(v -> actvAccountType.showDropDown());
        actvAccountType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) actvAccountType.showDropDown();
        });
    }

    private void setupRecyclerView() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        adapter = new IncomeAdapter(activity.getIncomes(), new IncomeAdapter.OnIncomeClickListener() {
            @Override
            public void onDeleteClick(Income income) {
                activity.removeIncome(income);
                updateList();
            }

            @Override
            public void onEditClick(Income income) {
                Toast.makeText(getContext(), "Tahrirlash: " + income.source, Toast.LENGTH_SHORT).show();
            }
        });

        rvIncomes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIncomes.setAdapter(adapter);
    }

    private void saveIncome() {
        String source = etIncSource.getText().toString().trim();
        String amountStr = etIncAmount.getText().toString().trim();
        String accountType = actvAccountType.getText().toString().trim();

        if (source.isEmpty() || amountStr.isEmpty() || accountType.isEmpty()) {
            Toast.makeText(getContext(), "Barcha maydonlarni to'ldiring", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                activity.addIncome(source, amount, accountType, calendar.getTimeInMillis());
                clearFields();
                updateList();
                Toast.makeText(getContext(), "Daromad qo'shildi", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Noto'g'ri summa", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        etIncSource.setText("");
        etIncAmount.setText("");
        actvAccountType.setText("");
        calendar = Calendar.getInstance();
        updateLabel();
    }

    public void updateList() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null || adapter == null) return;

        List<Income> incomes = activity.getIncomes();
        adapter.updateList(incomes);
        updateSummary(incomes);
    }

    private void updateSummary(List<Income> incomes) {
        double totalMonthly = 0;
        int count = incomes.size();
        Map<String, Double> sourceTotals = new HashMap<>();

        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        for (Income income : incomes) {
            Calendar incCal = Calendar.getInstance();
            incCal.setTimeInMillis(income.timestamp);
            
            // Only count for current month
            if (incCal.get(Calendar.MONTH) == currentMonth && incCal.get(Calendar.YEAR) == currentYear) {
                totalMonthly += income.amount;
            }

            // Track totals by source for "Top Source"
            String source = income.source.toLowerCase().trim();
            sourceTotals.put(source, sourceTotals.getOrDefault(source, 0.0) + income.amount);
        }

        String topSource = "Mavjud emas";
        if (!sourceTotals.isEmpty()) {
            topSource = Collections.max(sourceTotals.entrySet(), Map.Entry.comparingByValue()).getKey();
        }

        Locale locale = new Locale("uz");
        if (tvTotalMonthlyIncome != null) {
            tvTotalMonthlyIncome.setText(String.format(locale, "+%,.0f so'm", totalMonthly));
        }
        if (tvIncomeCount != null) {
            tvIncomeCount.setText(count + " TA KIRIM");
        }
        if (tvSummaryIncCount != null) {
            tvSummaryIncCount.setText(count + " ta kirim");
        }
        if (tvTopSource != null) {
            tvTopSource.setText(topSource);
        }
    }

    private void updateLabel() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        etIncDate.setText(sdf.format(calendar.getTime()));
    }
}
