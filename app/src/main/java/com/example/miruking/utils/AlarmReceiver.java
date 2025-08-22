package com.example.miruking.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.R;
import com.example.miruking.dao.LogDAO;
import com.example.miruking.dao.TodoDAO;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        sendNotifications(context);
    }

    public static void sendNotifications(Context context) {
        SQLiteDatabase db = new MirukingDBHelper(context).getWritableDatabase();
        LogDAO logDao = new LogDAO(db, context);
        TodoDAO todoDao = new TodoDAO(context);
        String today = getDateDaysAgo(0);
        List<NotificationDTO> todayTodos = todoDao.getNotificationItemsByDate(today);
        Set<String> alreadySent = NotificationTracker.getSentTodaySet(context);

        NotificationHelper.createNotificationChannel(context);

        // ✅ 날씨 비동기로 가져온 후 알림 발송
        WeatherUtil.getWeatherMessageAsync(context, weatherMsg -> {
            for (NotificationDTO todo : todayTodos) {
                String t_id_str = String.valueOf(todo.getT_id());
                if (alreadySent.contains(t_id_str)) continue;

                NotificationHelper.showNotification(context, todo, weatherMsg);
                NotificationTracker.markAsSent(context, todo.getT_id());
            }
        });
    }

    private static String getDateDaysAgo(int daysAgo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -daysAgo);
        return String.format("%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    public static class NotificationHelper {
        public static final String CHANNEL_ID = "todo_channel";

        public static void createNotificationChannel(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, "할 일 알림", NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("할 일 목록을 알림으로 표시합니다.");
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
        }

        public static void showNotification(Context context, NotificationDTO item, String weatherMsg) {
            Intent doneIntent = new Intent(context, TodoActionReceiver.class);
            doneIntent.setAction("ACTION_DONE");
            doneIntent.putExtra("t_id", item.getT_id());

            Intent delayIntent = new Intent(context, TodoActionReceiver.class);
            delayIntent.setAction("ACTION_DELAY");
            delayIntent.putExtra("t_id", item.getT_id());
            delayIntent.putExtra("start_date", item.getStartDate());
            delayIntent.putExtra("end_date", item.getEndDate());
            delayIntent.putExtra("delay_stack", item.getDelayStack());

            PendingIntent donePI = PendingIntent.getBroadcast(context, item.getT_id(), doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            PendingIntent delayPI = PendingIntent.getBroadcast(context, -item.getT_id(), delayIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            String contentText = item.getDescription();
            if (item.isBookmarked() && item.getBookmarkName() != null && !item.getBookmarkName().isEmpty()) {
                contentText = "[" + item.getBookmarkName() + "] " + item.getDescription();
            }
            contentText += "\n" + weatherMsg; // ✅ 날씨 표시

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.miru)
                    .setContentTitle(item.getTitle())
                    .setContentText(contentText)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .addAction(R.drawable.success, "완료", donePI)
                    .addAction(R.drawable.delay, "미룸", delayPI);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            NotificationManagerCompat.from(context).notify(item.getT_id(), builder.build());
        }
    }
}
