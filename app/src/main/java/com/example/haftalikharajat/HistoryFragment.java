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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        viewPager = view.findViewById(R.id.historyViewPager);
        tabLayout = view.findViewById(R.id.historyTabLayout);
        etSearchDate = view.findViewById(R.id.etSearchDate);
        btnSearch = view.findViewById(R.id.btnSearch);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 1) return new WeeklyFragment();
                return PeriodFragment.newInstance(position);
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        });

        String[] tabs = {"Kunlik", "Haftalik", "Oylik", "Yillik"};
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabs[position])).attach();

        etSearchDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String date = String.format("%02d.%02d.%d", dayOfMonth, month + 1, year);
                etSearchDate.setText(date);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSearch.setOnClickListener(v -> {
            String date = etSearchDate.getText().toString();
            if (!date.isEmpty()) {
                Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                if (currentFragment instanceof PeriodFragment) {
                    ((PeriodFragment) currentFragment).setFilterDate(date);
                }
            }
        });

        return view;
    }

    public void refresh() {
        if (viewPager != null) {
            Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
            if (currentFragment instanceof PeriodFragment) {
                ((PeriodFragment) currentFragment).updateData();
            } else if (currentFragment instanceof WeeklyFragment) {
                ((WeeklyFragment) currentFragment).updateData();
            }
        }
    }
}
