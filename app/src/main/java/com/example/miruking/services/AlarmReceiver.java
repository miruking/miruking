package com.example.miruking.dao;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.miruking.receiver.AlarmReceiver;

public class AlarmReceiver {

    public static void scheduleAllTodayAlarms(Context context, SQLiteDatabase db) {
        String sql = "SELECT t_id, t_start_time FROM todos WHERE date('now') BETWEEN date(t_start_date) AND date(t_end_date)";
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            int tId = cursor.getInt(0);
            String time = cursor.getString(1); // "HH:MM" 형식

            long triggerTime = convertTimeToMillis(time); // 오늘 기준 밀리초로
            scheduleAlarm(context, tId, triggerTime);
        }

        cursor.close();
    }

    private static long convertTimeToMillis(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static void scheduleAlarm(Context context, int tId, long timeInMillis) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("t_id", tId);

        PendingIntent pi = PendingIntent.getBroadcast(context, tId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
    }
}
    
}
