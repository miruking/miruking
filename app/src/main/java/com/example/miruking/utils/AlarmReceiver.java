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
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.R;
import com.example.miruking.dao.LogDAO;
import com.example.miruking.dao.TodoDAO;

import java.util.Calendar;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        sendNotifications(context);
    }

    public static void sendNotifications(Context context) {
        // ✅ DB 접근
        MirukingDBHelper dbHelper = new MirukingDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        LogDAO logDao = new LogDAO(db, context);
        TodoDAO todoDao = new TodoDAO(context);

        // ✅ 어제 알림 제거 및 로그 기록
        clearYesterdayNotifications(context, todoDao, logDao);

        // ✅ 오늘 알림 전송
        String today = getDateDaysAgo(0);
        List<NotificationDTO> todayTodos = todoDao.getNotificationItemsByDate(today);

        NotificationHelper.createNotificationChannel(context);
        for (NotificationDTO todo : todayTodos) {
            NotificationHelper.showNotification(context, todo);
        }
        Log.d("AlarmReceiver", "💬 sendNotifications triggered");
    }

    public static void clearYesterdayNotifications(Context context, TodoDAO todoDao, LogDAO logDao) {
        String yesterday = getDateDaysAgo(1);
        List<NotificationDTO> yesterdayTodos = todoDao.getNotificationItemsByDate(yesterday);

        for (NotificationDTO todo : yesterdayTodos) {
            dismissNotificationWithLog(context, todo, "미룸", logDao);
        }
    }

    public static void dismissNotificationWithLog(Context context, NotificationDTO todo, String state, LogDAO logDao) {
        NotificationManagerCompat.from(context).cancel(todo.getT_id());

        if (todo.getB_id() > 0) {
            logDao.insertLogWithBookmark(todo.getT_id(), todo.getB_id(), state, System.currentTimeMillis());
        } else {
            logDao.insertLog(todo.getT_id(), state, System.currentTimeMillis());
        }
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

        public static void showNotification(Context context, NotificationDTO item) {
            Intent doneIntent = new Intent(context, TodoActionReceiver.class);
            doneIntent.setAction("ACTION_DONE");
            doneIntent.putExtra("t_id", item.getT_id());

            Intent delayIntent = new Intent(context, TodoActionReceiver.class);
            delayIntent.setAction("ACTION_DELAY");
            delayIntent.putExtra("t_id", item.getT_id());

            PendingIntent donePI = PendingIntent.getBroadcast(context, item.getT_id(), doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            PendingIntent delayPI = PendingIntent.getBroadcast(context, -item.getT_id(), delayIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            String contentText;

            if (item.isBookmarked() && item.getBookmarkName() != null) {
                contentText = "[" + item.getBookmarkName() + "] " + item.getDescription();
            } else {
                contentText = item.getDescription();
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.miru)
                    .setContentTitle(item.getTitle())
                    .setContentText(contentText)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .addAction(R.drawable.success, "완료", donePI)
                    .addAction(R.drawable.delay, "미룸", delayPI);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            NotificationManagerCompat.from(context).notify(item.getT_id(), builder.build());
        }
    }
}