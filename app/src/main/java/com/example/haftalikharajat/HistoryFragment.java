package com.example.haftalikharajat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Calendar;

public class HistoryFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private EditText etSearchDate;
    private ImageButton btnSearch;
    private View tvHistoryTitle;
    private View searchContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        viewPager = view.findViewById(R.id.historyViewPager);
        tabLayout = view.findViewById(R.id.historyTabLayout);
        etSearchDate = view.findViewById(R.id.etSearchDate);
        btnSearch = view.findViewById(R.id.btnSearch);
        tvHistoryTitle = view.findViewById(R.id.tvHistoryTitle);
        searchContainer = view.findViewById(R.id.searchContainer);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 1) return new WeeklyFragment();
                if (position == 2) return new MonthlyFragment();
                return PeriodFragment.newInstance(position);
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        });

        String[] tabs = {"Kunlik", "Haftalik", "Oylik", "Yillik"};
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabs[position])).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tvHistoryTitle.setVisibility(View.VISIBLE);
                searchContainer.setVisibility(View.VISIBLE);
                updateSearchHint(position);
                updateSearchDateText(position);
            }
        });

        etSearchDate.setOnClickListener(v -> {
            int currentPos = viewPager.getCurrentItem();
            if (currentPos == 2) {
                showMonthYearPicker();
            } else if (currentPos == 3) {
                showYearPicker();
            } else {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                    String date = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year);
                    etSearchDate.setText(date);
                    applyFilter(date);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnSearch.setOnClickListener(v -> {
            applyFilter(etSearchDate.getText().toString());
        });

        return view;
    }

    private void updateSearchHint(int position) {
        if (etSearchDate == null) return;
        switch (position) {
            case 0: etSearchDate.setHint("Kunni tanlang"); break;
            case 1: etSearchDate.setHint("Haftani tanlang"); break;
            case 2: etSearchDate.setHint("Oyni tanlang"); break;
            case 3: etSearchDate.setHint("Yilni tanlang"); break;
        }
    }

    private void updateSearchDateText(int position) {
        if (etSearchDate == null) return;
        Calendar c = Calendar.getInstance();
        switch (position) {
            case 0: 
                etSearchDate.setText(new java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(c.getTime()));
                break;
            case 1:
                etSearchDate.setText("Joriy hafta");
                break;
            case 2:
                String monthName = new java.text.SimpleDateFormat("MMMM", new java.util.Locale("uz")).format(c.getTime());
                etSearchDate.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + c.get(Calendar.YEAR));
                break;
            case 3:
                etSearchDate.setText(String.valueOf(c.get(Calendar.YEAR)));
                break;
        }
    }

    private void applyFilter(String filter) {
        if (filter.isEmpty()) return;
        int pos = viewPager.getCurrentItem();
        Fragment f = getChildFragmentManager().findFragmentByTag("f" + pos);
        if (f instanceof PeriodFragment) {
            ((PeriodFragment) f).setFilterDate(filter);
        }
        // Weekly and Monthly are usually updated via their own set methods or they react to internal state
    }

    private void showYearPicker() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        final android.widget.NumberPicker yearPicker = new android.widget.NumberPicker(getContext());
        yearPicker.setMinValue(2020);
        yearPicker.setMaxValue(2030);
        yearPicker.setValue(Calendar.getInstance().get(Calendar.YEAR));
        builder.setView(yearPicker);
        builder.setTitle("Yilni tanlang");
        builder.setPositiveButton("OK", (dialog, id) -> {
            String yearStr = String.valueOf(yearPicker.getValue());
            etSearchDate.setText(yearStr);
            applyFilter(yearStr);
        });
        builder.setNegativeButton("Bekor qilish", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    private void showMonthYearPicker() {
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (!(currentFragment instanceof MonthlyFragment)) return;
        
        MonthlyFragment monthlyFragment = (MonthlyFragment) currentFragment;
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_month_year_picker, null);
        builder.setView(dialogView);

        final android.widget.NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);
        final android.widget.NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(new String[]{"Yanvar", "Fevral", "Mart", "Aprel", "May", "Iyun", "Iyul", "Avgust", "Sentabr", "Oktabr", "Noyabr", "Dekabr"});
        
        Calendar cal = Calendar.getInstance();
        monthPicker.setValue(cal.get(Calendar.MONTH));

        yearPicker.setMinValue(2020);
        yearPicker.setMaxValue(2030);
        yearPicker.setValue(cal.get(Calendar.YEAR));

        builder.setPositiveButton("OK", (dialog, id) -> {
            monthlyFragment.setMonthYear(monthPicker.getValue(), yearPicker.getValue());
            String dateText = monthPicker.getDisplayedValues()[monthPicker.getValue()] + " " + yearPicker.getValue();
            etSearchDate.setText(dateText);
        });
        builder.setNegativeButton("Bekor qilish", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }

    public void refresh() {
        if (viewPager != null && isAdded()) {
            for (int i = 0; i < 4; i++) {
                Fragment f = getChildFragmentManager().findFragmentByTag("f" + i);
                if (f instanceof PeriodFragment) {
                    ((PeriodFragment) f).updateData();
                } else if (f instanceof WeeklyFragment) {
                    ((WeeklyFragment) f).updateData();
                } else if (f instanceof MonthlyFragment) {
                    ((MonthlyFragment) f).updateData();
                }
            }
        }
    }
}
