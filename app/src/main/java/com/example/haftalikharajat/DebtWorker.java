package com.example.haftalikharajat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DebtWorker extends Worker {

    public DebtWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String debtsJson = getApplicationContext().getSharedPreferences("ByudjetProPrefs", Context.MODE_PRIVATE).getString("debts", null);
        if (debtsJson != null) {
            ArrayList<Debt> debts = new Gson().fromJson(debtsJson, new TypeToken<ArrayList<Debt>>(){}.getType());
            String today = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());

            for (Debt d : debts) {
                String dueDateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(d.dateDue));
                if (!d.isPaid && dueDateStr.equals(today)) {
                    sendNotification(d);
                }
            }
        }
        return Result.success();
    }

    private void sendNotification(Debt d) {
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "debt_reminders";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Qarz Eslatmalari", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Qarz to'lash vaqti keldi")
                .setContentText(d.personName + (d.isOwedToMe ? "dan oladigan" : "ga bo'lgan") + " " + d.amount + " so'm qarzni yodingizdan chiqarmang")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        nm.notify((int) System.currentTimeMillis(), builder.build());
    }
}