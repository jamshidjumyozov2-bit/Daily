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
    private java.util.Map<String, Integer> categoryUsage = new java.util.HashMap<>();
    private SharedPreferences prefs;
    private Gson gson = new Gson();

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Oziq-ovqat", "Transport", "Uy-joy", "O'yin-kulgi", "Salomatlik",
            "Kiyim-kechak", "Ta'lim", "Sovg'alar", "Internet", "Kommunal",
            "Xayriya", "Kredit", "Sug'urta", "Maishiy texnika", "Restoran",
            "Kafe", "Dorixona", "Sport", "Avtomobil", "Yoqilg'i",
            "Go'zallik", "Xizmatlar", "Sayohat", "Hobbi", "Boshqa"
    );

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
        if (incomeList != null) {
            for (Income i : incomeList) {
                if (i != null) total += i.amount;
            }
        }
        if (expenseList != null) {
            for (Expense e : expenseList) {
                if (e != null) total -= e.amount;
            }
        }
        if (debtList != null) {
            for (Debt d : debtList) {
                if (d != null && !d.isPaid) {
                    if (d.isOwedToMe) {
                        // "Berilgan" (Lent out) - money gone from pocket but expected back.
                        // For cash balance, it should be subtracted.
                        total -= d.amount;
                    } else {
                        // "Olingan" (Borrowed) - money added to pocket but owed.
                        // For cash balance, it should be added.
                        total += d.amount;
                    }
                }
            }
        }
        return total;
    }

    public void addExpense(String name, double amount, String category, long timestamp) {
        expenseList.add(0, new Expense(name, amount, category, timestamp));
        
        // Update category usage
        categoryUsage.put(category, categoryUsage.getOrDefault(category, 0) + 1);
        
        saveData();
        refreshAll();
    }

    public void addIncome(String source, double amount, String accountType, long timestamp) {
        incomeList.add(0, new Income(source, amount, accountType, timestamp));
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
        List<String> sortedCategories = new ArrayList<>(DEFAULT_CATEGORIES);
        sortedCategories.sort((c1, c2) -> {
            int count1 = categoryUsage.getOrDefault(c1, 0);
            int count2 = categoryUsage.getOrDefault(c2, 0);
            return Integer.compare(count2, count1); // Kamayish tartibida
        });
        return sortedCategories;
    }

    public void refreshAll() {
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f instanceof HomeFragment) ((HomeFragment) f).updateUI();
            if (f instanceof IncomeFragment) ((IncomeFragment) f).updateList();
            if (f instanceof DebtFragment) ((DebtFragment) f).updateList();
            if (f instanceof HistoryFragment) ((HistoryFragment) f).refresh();
        }
    }

    public void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("expenses", gson.toJson(expenseList));
        editor.putString("incomes", gson.toJson(incomeList));
        editor.putString("debts", gson.toJson(debtList));
        editor.putString("category_usage", gson.toJson(categoryUsage));
        editor.apply();
    }

    private void loadData() {
        try {
            String e = prefs.getString("expenses", null);
            if (e != null) {
                ArrayList<Expense> list = gson.fromJson(e, new TypeToken<ArrayList<Expense>>(){}.getType());
                if (list != null) expenseList = list;
            }
            String i = prefs.getString("incomes", null);
            if (i != null) {
                ArrayList<Income> list = gson.fromJson(i, new TypeToken<ArrayList<Income>>(){}.getType());
                if (list != null) incomeList = list;
            }
            String d = prefs.getString("debts", null);
            if (d != null) {
                ArrayList<Debt> list = gson.fromJson(d, new TypeToken<ArrayList<Debt>>(){}.getType());
                if (list != null) debtList = list;
            }
            String cu = prefs.getString("category_usage", null);
            if (cu != null) {
                java.util.Map<String, Integer> usage = gson.fromJson(cu, new TypeToken<java.util.Map<String, Integer>>(){}.getType());
                if (usage != null) categoryUsage = usage;
            }
        } catch (Exception ignored) {}
        
        if (expenseList == null) expenseList = new ArrayList<>();
        if (incomeList == null) incomeList = new ArrayList<>();
        if (debtList == null) debtList = new ArrayList<>();
        if (categoryUsage == null) categoryUsage = new java.util.HashMap<>();
    }

    public List<Expense> getExpenses() { return expenseList; }
    public List<Income> getIncomes() { return incomeList; }
    public List<Debt> getDebts() { return debtList; }
}