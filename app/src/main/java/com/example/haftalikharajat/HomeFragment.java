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
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvTotalBalance;
    private EditText etExpName, etExpAmount, etExpDate;
    private Button btnSaveExp;
    private long selectedTimestamp = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        etExpName = view.findViewById(R.id.etExpName);
        etExpAmount = view.findViewById(R.id.etExpAmount);
        etExpDate = view.findViewById(R.id.etExpDate);
        btnSaveExp = view.findViewById(R.id.btnSaveExp);

        updateUI();

        etExpDate.setOnClickListener(v -> showDateTimePicker());

        btnSaveExp.setOnClickListener(v -> {
            String name = etExpName.getText().toString().trim();
            String amountStr = etExpAmount.getText().toString().trim();
            if (name.isEmpty() || amountStr.isEmpty()) return;

            double amount = Double.parseDouble(amountStr);
            long timestamp = selectedTimestamp > 0 ? selectedTimestamp : System.currentTimeMillis();
            ((MainActivity) getActivity()).addExpense(name, amount, "Boshqa", timestamp);
            
            etExpName.setText("");
            etExpAmount.setText("");
            etExpDate.setText("");
            selectedTimestamp = 0;
            updateUI();
            Toast.makeText(getContext(), "Xarajat saqlandi", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void showDateTimePicker() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(java.util.Calendar.YEAR, year);
            calendar.set(java.util.Calendar.MONTH, month);
            calendar.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);

            new android.app.TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                calendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(java.util.Calendar.MINUTE, minute);
                selectedTimestamp = calendar.getTimeInMillis();
                etExpDate.setText(new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(calendar.getTime()));
            }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show();

        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    public void updateUI() {
        if (!isAdded()) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            double balance = activity.getBalance();
            tvTotalBalance.setText(String.format(Locale.getDefault(), "%,.0f so'm", balance));
            
            // Set balance color based on value
            if (balance > 0) {
                tvTotalBalance.setTextColor(getResources().getColor(R.color.white));
            } else if (balance < 0) {
                tvTotalBalance.setTextColor(getResources().getColor(R.color.expense_red));
            } else {
                tvTotalBalance.setTextColor(getResources().getColor(R.color.white));
            }
        }
    }
}