package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class IncomeFragment extends Fragment {

    private EditText etIncSource, etIncAmount;
    private Button btnSaveInc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);
        etIncSource = view.findViewById(R.id.etIncSource);
        etIncAmount = view.findViewById(R.id.etIncAmount);
        btnSaveInc = view.findViewById(R.id.btnSaveInc);

        btnSaveInc.setOnClickListener(v -> {
            String source = etIncSource.getText().toString().trim();
            String amountStr = etIncAmount.getText().toString().trim();
            if (source.isEmpty() || amountStr.isEmpty()) return;

            double amount = Double.parseDouble(amountStr);
            ((MainActivity) getActivity()).addIncome(source, amount);
            
            etIncSource.setText("");
            etIncAmount.setText("");
            Toast.makeText(getContext(), "Daromad qo'shildi", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}