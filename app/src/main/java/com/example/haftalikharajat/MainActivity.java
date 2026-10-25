package com.example.haftalikharajat;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.os.Build;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 mainViewPager;
    private BottomNavigationView bottomNav;
    private ArrayList<Expense> expenseList = new ArrayList<>();
    private ArrayList<Income> incomeList = new ArrayList<>();
    private ArrayList<Debt> debtList = new ArrayList<>();
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("ByudjetProPrefs", MODE_PRIVATE);
        loadData();

        mainViewPager = findViewById(R.id.mainViewPager);
        bottomNav = findViewById(R.id.bottomNav);

        mainViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0: return new HomeFragment();
                    case 1: return new IncomeFragment();
                    case 2: return new HistoryFragment();
                    case 3: return new DebtFragment();
                    default: return new HomeFragment();
                }
            }

            @Override
            public int getItemCount() { return 4; }
        });

        mainViewPager.setUserInputEnabled(false);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_expenses) mainViewPager.setCurrentItem(0, false);
            else if (id == R.id.nav_income) mainViewPager.setCurrentItem(1, false);
            else if (id == R.id.nav_history) mainViewPager.setCurrentItem(2, false);
            else if (id == R.id.nav_debts) mainViewPager.setCurrentItem(3, false);
            return true;
        });

        setSupportActionBar(findViewById(R.id.toolbar));

        scheduleDebtWorker();
        requestNotificationPermission();
    }

    private void scheduleDebtWorker() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(DebtWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    public double getBalance() {
        double total = 0;
        for (Income i : incomeList) total += i.amount;
        for (Expense e : expenseList) total -= e.amount;
        for (Debt d : debtList) {
            if (!d.isPaid) {
                if (d.isOwedToMe) total -= d.amount; // Money lent out (cash decreases)
                else total += d.amount; // Money borrowed (cash increases)
            }
        }
        return total;
    }

    public void addExpense(String name, double amount, String category, long timestamp) {
        expenseList.add(0, new Expense(name, amount, category, timestamp));
        saveData();
        refreshAll();
    }

    public void addIncome(String source, double amount) {
        incomeList.add(0, new Income(source, amount, System.currentTimeMillis()));
        saveData();
        refreshAll();
    }

    public void addDebt(Debt debt) {
        debtList.add(0, debt);
        saveData();
        refreshAll();
    }

    public void removeExpense(Expense e) {
        expenseList.remove(e);
        saveData();
        refreshAll();
    }

    public void removeIncome(Income i) {
        incomeList.remove(i);
        saveData();
        refreshAll();
    }

    public void removeDebt(Debt d) {
        debtList.remove(d);
        saveData();
        refreshAll();
    }

    public List<String> getCategories() {
        return List.of("Oziq-ovqat", "Transport", "Uy-joy", "O'yin-kulgi", "Salomatlik", "Kiyim-kechak", "Ta'lim", "Sovg'alar", "Internet", "Boshqa");
    }

    public void refreshAll() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof HomeFragment) ((HomeFragment) f).updateUI();
            if (f instanceof DebtFragment) ((DebtFragment) f).updateList();
            if (f instanceof HistoryFragment) ((HistoryFragment) f).refresh();
        }
    }

    public void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("expenses", gson.toJson(expenseList));
        editor.putString("incomes", gson.toJson(incomeList));
        editor.putString("debts", gson.toJson(debtList));
        editor.apply();
    }

    private void loadData() {
        try {
            String e = prefs.getString("expenses", null);
            if (e != null) expenseList = gson.fromJson(e, new TypeToken<ArrayList<Expense>>(){}.getType());
            String i = prefs.getString("incomes", null);
            if (i != null) incomeList = gson.fromJson(i, new TypeToken<ArrayList<Income>>(){}.getType());
            String d = prefs.getString("debts", null);
            if (d != null) debtList = gson.fromJson(d, new TypeToken<ArrayList<Debt>>(){}.getType());
        } catch (Exception ignored) {}
    }

    public List<Expense> getExpenses() { return expenseList; }
    public List<Debt> getDebts() { return debtList; }
}