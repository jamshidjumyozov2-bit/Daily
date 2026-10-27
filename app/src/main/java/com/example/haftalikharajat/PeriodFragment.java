package com.example.haftalikharajat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PeriodFragment extends Fragment {

    private RecyclerView rvPeriodHistory;
    private TextView tvDateLabel, tvTotalAmount;
    private TextView tvCatFood, tvCatTransport, tvCatShopping, tvCatOther;
    private View layoutCategoryBreakdown;
    private EditText etSearch;
    private ImageButton btnSearch;
    private int type;
    private String filterDate = "";
    private List<Expense> fullList = new ArrayList<>();
    private List<Expense> displayList = new ArrayList<>();
    private ExpenseHistoryAdapter adapter;

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
        rvPeriodHistory = view.findViewById(R.id.rvPeriodHistory);
        tvDateLabel = view.findViewById(R.id.tvPeriodDateLabel);
        tvTotalAmount = view.findViewById(R.id.tvPeriodTotalAmount);
        tvCatFood = view.findViewById(R.id.tvPeriodCatFoodValue);
        tvCatTransport = view.findViewById(R.id.tvPeriodCatTransportValue);
        tvCatShopping = view.findViewById(R.id.tvPeriodCatShoppingValue);
        tvCatOther = view.findViewById(R.id.tvPeriodCatOtherValue);
        layoutCategoryBreakdown = view.findViewById(R.id.layoutCategoryBreakdown);
        
        etSearch = view.findViewById(R.id.etSearchPeriod);
        btnSearch = view.findViewById(R.id.btnSearchPeriod);

        rvPeriodHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new ExpenseHistoryAdapter(displayList);
        rvPeriodHistory.setAdapter(adapter);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterByName(s.toString().toLowerCase());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

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
        fullList = filterExpenses(allExpenses);
        Collections.reverse(fullList); 
        
        displayList.clear();
        displayList.addAll(fullList);
        adapter.notifyDataSetChanged();
        updateSummary();
    }

    private void filterByName(String query) {
        displayList.clear();
        if (query.isEmpty()) {
            displayList.addAll(fullList);
        } else {
            for (Expense e : fullList) {
                if (e.name.toLowerCase().contains(query)) {
                    displayList.add(e);
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateSummary();
    }

    private void updateSummary() {
        if (tvTotalAmount == null) return;
        
        double total = 0;
        double food = 0, transport = 0, shopping = 0, other = 0;

        for (Expense e : displayList) {
            total += e.amount;
            String cat = e.category.toLowerCase();
            if (cat.contains("ovqat") || cat.contains("kafe") || cat.contains("restoran")) {
                food += e.amount;
            } else if (cat.contains("transport") || cat.contains("avto") || cat.contains("yoqilg'i")) {
                transport += e.amount;
            } else if (cat.contains("bozor") || cat.contains("do'kon") || cat.contains("kiyim") || cat.contains("shopping")) {
                shopping += e.amount;
            } else {
                other += e.amount;
            }
        }

        Locale uzLocale = new Locale("uz");
        tvTotalAmount.setText(String.format(uzLocale, "-%,.0f so'm", total));

        if (layoutCategoryBreakdown != null) {
            if (type == 0 || (filterDate != null && !filterDate.isEmpty())) {
                layoutCategoryBreakdown.setVisibility(View.VISIBLE);
                if (tvCatFood != null) tvCatFood.setText(formatShort(food));
                if (tvCatTransport != null) tvCatTransport.setText(formatShort(transport));
                if (tvCatShopping != null) tvCatShopping.setText(formatShort(shopping));
                if (tvCatOther != null) tvCatOther.setText(formatShort(other));
            } else {
                layoutCategoryBreakdown.setVisibility(View.GONE);
            }
        }

        String label;
        SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy", new Locale("uz"));
        if (filterDate != null && !filterDate.isEmpty()) {
            label = filterDate;
        } else {
            switch (type) {
                case 0: label = "Bugun, " + sdf.format(new Date()); break;
                case 2: label = new SimpleDateFormat("MMMM yyyy", new Locale("uz")).format(new Date()); break;
                case 3: label = new SimpleDateFormat("yyyy", new Locale("uz")).format(new Date()); break;
                default: label = sdf.format(new Date());
            }
        }
        tvDateLabel.setText(label);
    }

    private String formatShort(double amount) {
        if (amount >= 1000000) return String.format(Locale.US, "%.1fM", amount / 1000000.0);
        if (amount >= 1000) return String.format(Locale.US, "%.0fK", amount / 1000.0);
        return String.format(Locale.US, "%.0f", amount);
    }

    private class ExpenseHistoryAdapter extends RecyclerView.Adapter<ExpenseHistoryAdapter.ViewHolder> {
        private final List<Expense> data;
        private final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private final SimpleDateFormat fullSdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

        ExpenseHistoryAdapter(List<Expense> data) { this.data = data; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense_fancy, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Expense e = data.get(position);
            holder.tvName.setText(e.name);
            
            String detail = (type == 0) ? timeSdf.format(e.timestamp) : fullSdf.format(e.timestamp);
            holder.tvDetail.setText(detail + " | " + e.category);
            holder.tvAmount.setText(String.format(new Locale("uz"), "-%,.0f so'm", e.amount));
            
            // Icon mapping
            int iconRes = R.drawable.ic_more; 
            String cat = e.category.toLowerCase();
            if (cat.contains("ovqat") || cat.contains("kafe") || cat.contains("restoran")) 
                iconRes = R.drawable.ic_food;
            else if (cat.contains("transport") || cat.contains("avto") || cat.contains("yoqilg'i")) 
                iconRes = R.drawable.ic_transport;
            else if (cat.contains("uy") || cat.contains("kommunal"))
                iconRes = R.drawable.ic_home;
            else if (cat.contains("o'yin") || cat.contains("dam") || cat.contains("kulgi"))
                iconRes = R.drawable.ic_entertainment;
            else if (cat.contains("bozor") || cat.contains("do'kon") || cat.contains("kiyim") || cat.contains("shopping"))
                iconRes = R.drawable.ic_shopping;
            
            holder.ivIcon.setImageResource(iconRes);

            holder.btnDelete.setOnClickListener(v -> {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    new android.app.AlertDialog.Builder(getContext())
                        .setTitle("O'chirish")
                        .setMessage("Ushbu xarajatni o'chirishni xohlaysizmi?")
                        .setPositiveButton("Ha", (dialog, which) -> {
                            activity.removeExpense(e);
                            updateData();
                        })
                        .setNegativeButton("Yo'q", null)
                        .show();
                }
            });

            holder.btnEdit.setOnClickListener(v -> {
                showEditDialog(e);
            });
        }

        private void showEditDialog(Expense e) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_expense, null);
            EditText etName = dialogView.findViewById(R.id.etEditExpName);
            EditText etAmount = dialogView.findViewById(R.id.etEditExpAmount);
            EditText etDate = dialogView.findViewById(R.id.etEditExpDate);
            EditText etTime = dialogView.findViewById(R.id.etEditExpTime);
            com.google.android.material.chip.ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupEditCategory);

            etName.setText(e.name);
            etAmount.setText(String.format(Locale.US, "%.0f", e.amount));
            
            SimpleDateFormat sdfD = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat sdfT = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etDate.setText(sdfD.format(e.timestamp));
            etTime.setText(sdfT.format(e.timestamp));

            final long[] selectedTs = {e.timestamp};
            final String[] selectedCat = {e.category};

            MainActivity activity = (MainActivity) getActivity();
            if (activity != null) {
                List<String> categories = activity.getCategories();
                for (String cat : categories) {
                    com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(getContext());
                    chip.setText(cat);
                    chip.setCheckable(true);
                    chip.setChipBackgroundColorResource(R.color.dark_card);
                    chip.setTextColor(getResources().getColor(R.color.dark_text_primary));
                    if (cat.equals(e.category)) chip.setChecked(true);
                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) selectedCat[0] = cat;
                    });
                    chipGroup.addView(chip);
                }
            }

            etDate.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(selectedTs[0]);
                new android.app.DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selectedTs[0] = cal.getTimeInMillis();
                    etDate.setText(sdfD.format(selectedTs[0]));
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            });

            etTime.setOnClickListener(v -> {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(selectedTs[0]);
                new android.app.TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    cal.set(Calendar.MINUTE, minute);
                    selectedTs[0] = cal.getTimeInMillis();
                    etTime.setText(sdfT.format(selectedTs[0]));
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
            });

            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Xarajatni tahrirlash")
                    .setView(dialogView)
                    .setPositiveButton("Saqlash", (dialog, which) -> {
                        String newName = etName.getText().toString().trim();
                        String amtStr = etAmount.getText().toString().trim();
                        if (newName.isEmpty() || amtStr.isEmpty()) return;
                        
                        double newAmount = Double.parseDouble(amtStr);
                        if (activity != null) {
                            activity.updateExpense(e, newName, newAmount, selectedCat[0], selectedTs[0]);
                            updateData();
                        }
                    })
                    .setNegativeButton("Bekor qilish", null)
                    .show();
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetail, tvAmount;
            android.widget.ImageView ivIcon;
            android.widget.ImageButton btnEdit, btnDelete;
            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvExpName);
                tvDetail = v.findViewById(R.id.tvExpDetail);
                tvAmount = v.findViewById(R.id.tvExpAmount);
                ivIcon = v.findViewById(R.id.ivCategoryIcon);
                btnEdit = v.findViewById(R.id.btnEditExp);
                btnDelete = v.findViewById(R.id.btnDeleteExp);
            }
        }
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