package com.example.haftalikharajat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;

public class DebtFragment extends Fragment {

    private EditText etPerson, etAmount, etReason, etPhone;
    private Button btnTaken, btnDue, btnSave;
    private CheckBox cbIsOwedToMe;
    private RecyclerView rvDebts;
    private Calendar calTaken = Calendar.getInstance();
    private Calendar calDue = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debts, container, false);
        
        etPerson = view.findViewById(R.id.etDebtPerson);
        etAmount = view.findViewById(R.id.etDebtAmount);
        etReason = view.findViewById(R.id.etDebtReason);
        etPhone = view.findViewById(R.id.etDebtPhone);
        btnTaken = view.findViewById(R.id.btnDebtDateTaken);
        btnDue = view.findViewById(R.id.btnDebtDateDue);
        btnSave = view.findViewById(R.id.btnSaveDebt);
        cbIsOwedToMe = view.findViewById(R.id.cbIsOwedToMe);
        rvDebts = view.findViewById(R.id.rvDebts);

        rvDebts.setLayoutManager(new LinearLayoutManager(getContext()));
        updateList();

        btnTaken.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                calTaken.set(year, month, dayOfMonth);
            }, calTaken.get(Calendar.YEAR), calTaken.get(Calendar.MONTH), calTaken.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnDue.setOnClickListener(v -> {
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                calDue.set(year, month, dayOfMonth);
            }, calDue.get(Calendar.YEAR), calDue.get(Calendar.MONTH), calDue.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSave.setOnClickListener(v -> {
            String person = etPerson.getText().toString().trim();
            String amountStr = etAmount.getText().toString().trim();
            if (person.isEmpty() || amountStr.isEmpty()) return;

            Debt debt = new Debt(person, Double.parseDouble(amountStr), etReason.getText().toString(),
                    etPhone.getText().toString(), calTaken.getTimeInMillis(), calDue.getTimeInMillis(),
                    cbIsOwedToMe.isChecked());

            ((MainActivity) getActivity()).addDebt(debt);
            clearFields();
            updateList();
            Toast.makeText(getContext(), "Qarz saqlandi", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void clearFields() {
        etPerson.setText("");
        etAmount.setText("");
        etReason.setText("");
        etPhone.setText("");
        cbIsOwedToMe.setChecked(false);
    }

    public void updateList() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            DebtAdapter adapter = new DebtAdapter(activity.getDebts(), new DebtAdapter.OnDebtActionListener() {
                @Override
                public void onPaid(Debt debt) {
                    debt.isPaid = true;
                    activity.saveData();
                    updateList();
                }

                @Override
                public void onDelete(Debt debt) {
                    activity.removeDebt(debt);
                    updateList();
                }
            });
            rvDebts.setAdapter(adapter);
        }
    }
}