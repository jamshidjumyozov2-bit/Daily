package com.example.haftalikharajat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DebtFragment extends Fragment {

    private EditText etPerson, etAmount, etReason, etPhone;
    private View btnTaken, btnDue;
    private TextView tvDateTaken, tvDateDue, tvTotalGiven, tvGivenCount, tvTotalTaken, tvTakenCount, tvDebtCountDisplay;
    private Button btnSave, btnFilterActive, btnFilterClosed;
    private RadioButton rbGiven;
    private RecyclerView rvDebts;
    private Calendar calTaken = Calendar.getInstance();
    private Calendar calDue = Calendar.getInstance();
    private boolean showingActive = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debts, container, false);
        
        initializeViews(view);
        
        if (rvDebts != null && getContext() != null) {
            rvDebts.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        
        updateDates();
        updateList();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        etPerson = view.findViewById(R.id.etDebtPerson);
        etAmount = view.findViewById(R.id.etDebtAmount);
        etReason = view.findViewById(R.id.etDebtReason);
        etPhone = view.findViewById(R.id.etDebtPhone);
        btnTaken = view.findViewById(R.id.btnDebtDateTaken);
        btnDue = view.findViewById(R.id.btnDebtDateDue);
        tvDateTaken = view.findViewById(R.id.tvDateTaken);
        tvDateDue = view.findViewById(R.id.tvDateDue);
        tvTotalGiven = view.findViewById(R.id.tvTotalGiven);
        tvGivenCount = view.findViewById(R.id.tvGivenCount);
        tvTotalTaken = view.findViewById(R.id.tvTotalTaken);
        tvTakenCount = view.findViewById(R.id.tvTakenCount);
        tvDebtCountDisplay = view.findViewById(R.id.tvDebtCountDisplay);
        btnSave = view.findViewById(R.id.btnSaveDebt);
        btnFilterActive = view.findViewById(R.id.btnFilterActive);
        btnFilterClosed = view.findViewById(R.id.btnFilterClosed);
        rbGiven = view.findViewById(R.id.rbGiven);
        rvDebts = view.findViewById(R.id.rvDebts);
    }

    private void setupListeners() {
        if (btnTaken != null) {
            btnTaken.setOnClickListener(v -> {
                if (!isAdded()) return;
                new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                    calTaken.set(year, month, dayOfMonth);
                    updateDates();
                }, calTaken.get(Calendar.YEAR), calTaken.get(Calendar.MONTH), calTaken.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        if (btnDue != null) {
            btnDue.setOnClickListener(v -> {
                if (!isAdded()) return;
                new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                    calDue.set(year, month, dayOfMonth);
                    updateDates();
                }, calDue.get(Calendar.YEAR), calDue.get(Calendar.MONTH), calDue.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                if (etPerson == null || etAmount == null) return;
                String person = etPerson.getText().toString().trim();
                String amountStr = etAmount.getText().toString().trim();
                if (person.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(getContext(), "Ism va summani kiriting", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    Debt debt = new Debt(person, amount, etReason.getText().toString(),
                            etPhone.getText().toString(), calTaken.getTimeInMillis(), calDue.getTimeInMillis(),
                            rbGiven != null && rbGiven.isChecked());

                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.addDebt(debt);
                        clearFields();
                        updateList();
                        Toast.makeText(getContext(), "Qarz saqlandi", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Noto'g'ri summa", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnFilterActive != null) {
            btnFilterActive.setOnClickListener(v -> {
                showingActive = true;
                updateFilterButtons();
                updateList();
            });
        }

        if (btnFilterClosed != null) {
            btnFilterClosed.setOnClickListener(v -> {
                showingActive = false;
                updateFilterButtons();
                updateList();
            });
        }
    }

    private void updateFilterButtons() {
        if (btnFilterActive != null && btnFilterClosed != null) {
            if (showingActive) {
                btnFilterActive.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3B82F6")));
                btnFilterActive.setTextColor(android.graphics.Color.WHITE);
                btnFilterClosed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1E293B")));
                btnFilterClosed.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
            } else {
                btnFilterActive.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1E293B")));
                btnFilterActive.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
                btnFilterClosed.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#3B82F6")));
                btnFilterClosed.setTextColor(android.graphics.Color.WHITE);
            }
        }
    }

    private void updateDates() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("uz"));
            if (tvDateTaken != null) tvDateTaken.setText(sdf.format(calTaken.getTime()));
            if (tvDateDue != null) tvDateDue.setText(sdf.format(calDue.getTime()));
        } catch (Exception ignored) {}
    }

    private void clearFields() {
        if (etPerson != null) etPerson.setText("");
        if (etAmount != null) etAmount.setText("");
        if (etReason != null) etReason.setText("");
        if (etPhone != null) etPhone.setText("");
        if (rbGiven != null) rbGiven.setChecked(true);
        calTaken = Calendar.getInstance();
        calDue = Calendar.getInstance();
        updateDates();
    }

    public void updateList() {
        if (rvDebts == null || !isAdded()) return;
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<Debt> allDebts = activity.getDebts();
        List<Debt> filteredDebts = new ArrayList<>();
        if (allDebts != null) {
            for (Debt d : allDebts) {
                if (d != null && d.isPaid == !showingActive) {
                    filteredDebts.add(d);
                }
            }
        }
        
        DebtAdapter adapter = new DebtAdapter(filteredDebts, new DebtAdapter.OnDebtActionListener() {
            @Override
            public void onPaid(Debt debt) {
                if (debt != null) {
                    debt.isPaid = true;
                    activity.saveData();
                    updateList();
                    activity.refreshAll();
                }
            }

            @Override
            public void onDelete(Debt debt) {
                if (debt != null) {
                    activity.removeDebt(debt);
                    updateList();
                    activity.refreshAll();
                }
            }
        });
        rvDebts.setAdapter(adapter);

        updateSummary(allDebts != null ? allDebts : new ArrayList<>());
    }

    private void updateSummary(List<Debt> validDebts) {
        double given = 0, taken = 0;
        int gCount = 0, tCount = 0;
        for (Debt d : validDebts) {
            if (!d.isPaid) {
                if (d.isOwedToMe) {
                    given += d.amount;
                    gCount++;
                } else {
                    taken += d.amount;
                    tCount++;
                }
            }
        }
        
        Locale locale = new Locale("uz");
        if (tvTotalGiven != null) tvTotalGiven.setText(String.format(locale, "%,.0f", given));
        if (tvGivenCount != null) tvGivenCount.setText(gCount + " ta burch");
        if (tvTotalTaken != null) tvTotalTaken.setText(String.format(locale, "%,.0f", taken));
        if (tvTakenCount != null) tvTakenCount.setText(tCount + " ta kutilmoqda");
        if (tvDebtCountDisplay != null) tvDebtCountDisplay.setText(validDebts.size() + " TA TRANZAKSIYA");
    }
}
