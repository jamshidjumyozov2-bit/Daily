package com.example.haftalikharajat;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DailyExpensesFragment extends Fragment {

    private String dayName;
    private List<Expense> expenses;
    private List<Expense> displayList;
    private ExpenseAdapter adapter;
    private TextView tvDateLong;
    private TextView tvTotalAmount;
    private TextView tvCatFoodValue, tvCatTransportValue, tvCatShoppingValue, tvCatOtherValue;

    public static DailyExpensesFragment newInstance(String dayName, List<Expense> expenses) {
        DailyExpensesFragment fragment = new DailyExpensesFragment();
        Bundle args = new Bundle();
        args.putString("dayName", dayName);
        args.putString("expensesJson", new Gson().toJson(expenses));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayName = getArguments().getString("dayName");
            String json = getArguments().getString("expensesJson");
            expenses = new Gson().fromJson(json, new TypeToken<List<Expense>>(){}.getType());
            displayList = new ArrayList<>(expenses != null ? expenses : new ArrayList<>());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_list, container, false);
        view.setClickable(true);
        view.setFocusable(true);
        
        TextView tvTitle = view.findViewById(R.id.tvDailyTitle);
        tvDateLong = view.findViewById(R.id.tvDailyDateLong);
        tvTotalAmount = view.findViewById(R.id.tvDailyTotalAmount);
        tvCatFoodValue = view.findViewById(R.id.tvCatFoodValue);
        tvCatTransportValue = view.findViewById(R.id.tvCatTransportValue);
        tvCatShoppingValue = view.findViewById(R.id.tvCatShoppingValue);
        tvCatOtherValue = view.findViewById(R.id.tvCatOtherValue);
        
        ImageButton btnBack = view.findViewById(R.id.btnBack);
        RecyclerView rv = view.findViewById(R.id.rvDailyExpenses);
        
        EditText etSearch = view.findViewById(R.id.etSearchDaily);
        ImageButton btnSearch = view.findViewById(R.id.btnSearchDaily);

        long itemTs = (expenses != null && !expenses.isEmpty()) 
                ? expenses.get(0).timestamp 
                : System.currentTimeMillis();
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("d MMMM yyyy", new Locale("uz"));
        Calendar today = Calendar.getInstance();
        Calendar itemCal = Calendar.getInstance();
        itemCal.setTimeInMillis(itemTs);
        
        boolean isToday = today.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) &&
                          today.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR);
        
        String formattedDate = sdfDate.format(new Date(itemTs));
        
        // Handle title based on dayName
        boolean isDateString = false;
        try {
            if (dayName != null) {
                new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(dayName);
                isDateString = true;
            }
        } catch (Exception ignored) {}

        if (isDateString) {
            tvTitle.setText(formattedDate);
            tvDateLong.setText(isToday ? "Bugun" : "Tanlangan sana");
        } else {
            tvTitle.setText(dayName != null ? dayName + " xarajatlari" : "Xarajatlar");
            tvDateLong.setText(isToday ? "Bugun, " + formattedDate : formattedDate);
        }
        updateTotal();

        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });

        adapter = new ExpenseAdapter(displayList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString().toLowerCase());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                String query = etSearch.getText().toString().toLowerCase();
                filter(query);
            });
        }

        ImageButton btnHeaderEdit = view.findViewById(R.id.btnHeaderEdit);
        ImageButton btnHeaderDelete = view.findViewById(R.id.btnHeaderDelete);

        if (btnHeaderEdit != null) {
            btnHeaderEdit.setOnClickListener(v -> {
                showBulkEditDialog();
            });
        }

        if (btnHeaderDelete != null) {
            btnHeaderDelete.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Barchasini o'chirish")
                        .setMessage("Bugungi barcha xarajatlarni o'chirishni xohlaysizmi?")
                        .setPositiveButton("Ha", (dialog, which) -> {
                            MainActivity activity = (MainActivity) getActivity();
                            if (activity != null && expenses != null) {
                                for (Expense e : new ArrayList<>(expenses)) {
                                    activity.removeExpense(e);
                                }
                                expenses.clear();
                                displayList.clear();
                                adapter.notifyDataSetChanged();
                                updateTotal();
                            }
                        })
                        .setNegativeButton("Yo'q", null)
                        .show();
            });
        }

        return view;
    }

    private void showBulkEditDialog() {
        if (expenses == null || expenses.isEmpty()) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_expense, null);
        EditText etName = dialogView.findViewById(R.id.etEditExpName);
        EditText etAmount = dialogView.findViewById(R.id.etEditExpAmount);
        EditText etDate = dialogView.findViewById(R.id.etEditExpDate);
        EditText etTime = dialogView.findViewById(R.id.etEditExpTime);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupEditCategory);

        etName.setHint("Barcha uchun yangi nom (ixtiyoriy)");
        etAmount.setHint("Barcha uchun yangi miqdor (ixtiyoriy)");
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        etDate.setText(sdfDate.format(expenses.get(0).timestamp));
        etTime.setVisibility(View.GONE);

        final long[] selectedTs = {expenses.get(0).timestamp};
        final String[] selectedCat = {null};

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            List<String> categories = activity.getCategories();
            for (String cat : categories) {
                Chip chip = new Chip(getContext());
                chip.setText(cat);
                chip.setCheckable(true);
                chip.setChipBackgroundColorResource(R.color.dark_card);
                chip.setTextColor(getResources().getColor(R.color.dark_text_primary));
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
                etDate.setText(sdfDate.format(selectedTs[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Kunlik xarajatlarni tahrirlash")
                .setMessage("To'ldirilgan maydonlar barcha xarajatlar uchun o'zgaradi.")
                .setView(dialogView)
                .setPositiveButton("Barchasini yangilash", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    Double newAmount = amtStr.isEmpty() ? null : Double.parseDouble(amtStr);
                    
                    if (activity != null) {
                        for (Expense e : new ArrayList<>(expenses)) {
                            String nameToSet = newName.isEmpty() ? e.name : newName;
                            double amountToSet = newAmount == null ? e.amount : newAmount;
                            String catToSet = selectedCat[0] == null ? e.category : selectedCat[0];
                            
                            // Keep original time, only update date
                            Calendar oldCal = Calendar.getInstance();
                            oldCal.setTimeInMillis(e.timestamp);
                            Calendar newDateCal = Calendar.getInstance();
                            newDateCal.setTimeInMillis(selectedTs[0]);
                            
                            oldCal.set(Calendar.YEAR, newDateCal.get(Calendar.YEAR));
                            oldCal.set(Calendar.MONTH, newDateCal.get(Calendar.MONTH));
                            oldCal.set(Calendar.DAY_OF_MONTH, newDateCal.get(Calendar.DAY_OF_MONTH));
                            
                            activity.updateExpense(e, nameToSet, amountToSet, catToSet, oldCal.getTimeInMillis());
                            
                            // Local update
                            e.name = nameToSet;
                            e.amount = amountToSet;
                            e.category = catToSet;
                            e.timestamp = oldCal.getTimeInMillis();
                        }
                        adapter.notifyDataSetChanged();
                        updateTotal();
                        Toast.makeText(getContext(), "Barcha xarajatlar yangilandi", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Bekor qilish", null)
                .show();
    }

    private void updateTotal() {
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
        
        if (tvCatFoodValue != null) tvCatFoodValue.setText(formatShort(food));
        if (tvCatTransportValue != null) tvCatTransportValue.setText(formatShort(transport));
        if (tvCatShoppingValue != null) tvCatShoppingValue.setText(formatShort(shopping));
        if (tvCatOtherValue != null) tvCatOtherValue.setText(formatShort(other));
    }

    private String formatShort(double amount) {
        if (amount >= 1000000) return String.format(Locale.US, "%.1fM", amount / 1000000.0);
        if (amount >= 1000) return String.format(Locale.US, "%.0fK", amount / 1000.0);
        return String.format(Locale.US, "%.0f", amount);
    }

    private void filterByDate(String dateStr) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity == null) return;

        List<Expense> all = activity.getExpenses();
        displayList.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        for (Expense e : all) {
            if (sdf.format(e.timestamp).equals(dateStr)) {
                displayList.add(e);
            }
        }
        adapter.notifyDataSetChanged();
        updateTotal();

        try {
            SimpleDateFormat sdfTarget = new SimpleDateFormat("d MMM yyyy", new Locale("uz"));
            SimpleDateFormat sdfSource = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            java.util.Date d = sdfSource.parse(dateStr);
            if (d != null) tvDateLong.setText("Bugun, " + sdfTarget.format(d));
        } catch (Exception ignored) {}
    }

    private void filter(String query) {
        displayList.clear();
        if (query.isEmpty()) {
            if (expenses != null) displayList.addAll(expenses);
        } else {
            if (expenses != null) {
                for (Expense e : expenses) {
                    if (e.name.toLowerCase().contains(query)) {
                        displayList.add(e);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        updateTotal();
    }

    private class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
        private final List<Expense> data;
        private final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        ExpenseAdapter(List<Expense> data) { this.data = data; }

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
            holder.tvDetail.setText(timeSdf.format(e.timestamp) + " | " + e.category);
            holder.tvAmount.setText(String.format(new Locale("uz"), "-%,.0f so'm", e.amount));
            
            // Dynamic Category Icon mapping
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
                            data.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, data.size());
                            updateTotal();
                        })
                        .setNegativeButton("Yo'q", null)
                        .show();
                }
            });

            holder.btnEdit.setOnClickListener(v -> {
            showEditDialog(e, position);
        });
    }

    private void showEditDialog(Expense e, int position) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_edit_expense, null);
        EditText etName = dialogView.findViewById(R.id.etEditExpName);
        EditText etAmount = dialogView.findViewById(R.id.etEditExpAmount);
        EditText etDate = dialogView.findViewById(R.id.etEditExpDate);
        EditText etTime = dialogView.findViewById(R.id.etEditExpTime);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupEditCategory);

        etName.setText(e.name);
        etAmount.setText(String.format(Locale.US, "%.0f", e.amount));
        
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        etDate.setText(sdfDate.format(e.timestamp));
        etTime.setText(sdfTime.format(e.timestamp));

        final long[] selectedTs = {e.timestamp};
        final String[] selectedCat = {e.category};

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            List<String> categories = activity.getCategories();
            for (String cat : categories) {
                Chip chip = new Chip(getContext());
                chip.setText(cat);
                chip.setCheckable(true);
                chip.setChipBackgroundColorResource(R.color.dark_card);
                chip.setTextColor(getResources().getColor(R.color.dark_text_primary));
                if (cat.equals(e.category)) {
                    chip.setChecked(true);
                    selectedCat[0] = cat;
                }
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
                etDate.setText(sdfDate.format(selectedTs[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        etTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedTs[0]);
            new android.app.TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                selectedTs[0] = cal.getTimeInMillis();
                etTime.setText(sdfTime.format(selectedTs[0]));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Xarajatni tahrirlash")
                .setView(dialogView)
                .setPositiveButton("Saqlash", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim();
                    if (newName.isEmpty() || amtStr.isEmpty()) return;
                    
                    double newAmount = Double.parseDouble(amtStr);
                    if (activity != null) {
                        activity.updateExpense(e, newName, newAmount, selectedCat[0], selectedTs[0]);
                        // Local update
                        e.name = newName;
                        e.amount = newAmount;
                        e.category = selectedCat[0];
                        e.timestamp = selectedTs[0];
                        adapter.notifyItemChanged(position);
                        updateTotal();
                    }
                })
                .setNegativeButton("Bekor qilish", null)
                .show();
    }

        @Override
        public int getItemCount() { return data != null ? data.size() : 0; }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvDetail, tvAmount;
            ImageView ivIcon;
            ImageButton btnEdit, btnDelete;
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
}
