package com.example.miruking.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.R;
import com.example.miruking.activities.Todo;
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
        LogDAO logDao = new LogDAO(db);
        TodoDAO todoDao = new TodoDAO(context);

        // ✅ 어제 알림 제거 및 로그 기록
        clearYesterdayNotifications(context, todoDao, logDao);

        // ✅ 오늘 알림 전송
        String today = getDateDaysAgo(0);
        List<Todo> todayTodos = todoDao.getTodosByDate(today);

        NotificationHelper.createNotificationChannel(context);
        for (Todo todo : todayTodos) {
            NotificationHelper.showTodoNotification(context, todo);
        }
    }

    public static void clearYesterdayNotifications(Context context, TodoDAO todoDao, LogDAO logDao) {
        String yesterday = getDateDaysAgo(1);
        List<Todo> yesterdayTodos = todoDao.getTodosByDate(yesterday);

        for (Todo todo : yesterdayTodos) {
            dismissNotificationWithLog(context, todo, "미룸", logDao);
        }
    }

    public static void dismissNotificationWithLog(Context context, Todo todo, String state, LogDAO logDao) {
        NotificationManagerCompat.from(context).cancel(todo.getT_id());

        if (todo.getBookmarkNum() > 0) {
            logDao.insertLogWithBookmark(todo.getT_id(), todo.getBookmarkNum(), state, System.currentTimeMillis());
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

        public static void showTodoNotification(Context context, Todo todo) {
            Intent doneIntent = new Intent(context, TodoActionReceiver.class);
            doneIntent.setAction("ACTION_DONE");
            doneIntent.putExtra("t_id", todo.getT_id());

            Intent delayIntent = new Intent(context, TodoActionReceiver.class);
            delayIntent.setAction("ACTION_DELAY");
            delayIntent.putExtra("t_id", todo.getT_id());

            PendingIntent donePI = PendingIntent.getBroadcast(context, todo.getT_id(), doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            PendingIntent delayPI = PendingIntent.getBroadcast(context, -todo.getT_id(), delayIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(todo.getTitle())
                    .setContentText(todo.getDescription())
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .addAction(R.drawable.ic_done, "완료", donePI)
                    .addAction(R.drawable.ic_delay, "미룸", delayPI);

            NotificationManagerCompat.from(context).notify(todo.getT_id(), builder.build());
        }
    }
}