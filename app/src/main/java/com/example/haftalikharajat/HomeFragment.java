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
    private EditText etExpName, etExpAmount;
    private Spinner spExpCategory;
    private Button btnSaveExp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        etExpName = view.findViewById(R.id.etExpName);
        etExpAmount = view.findViewById(R.id.etExpAmount);
        spExpCategory = view.findViewById(R.id.spExpCategory);
        btnSaveExp = view.findViewById(R.id.btnSaveExp);

        updateUI();

        btnSaveExp.setOnClickListener(v -> {
            String name = etExpName.getText().toString().trim();
            String amountStr = etExpAmount.getText().toString().trim();
            if (name.isEmpty() || amountStr.isEmpty()) return;

            double amount = Double.parseDouble(amountStr);
            String category = spExpCategory.getSelectedItem().toString();
            ((MainActivity) getActivity()).addExpense(name, amount, category);
            
            etExpName.setText("");
            etExpAmount.setText("");
            updateUI();
            Toast.makeText(getContext(), "Xarajat saqlandi", Toast.LENGTH_SHORT).show();
        });

        return view;
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

            List<String> categories = activity.getCategories();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spExpCategory.setAdapter(adapter);
        }
    }
}