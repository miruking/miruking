package com.example.miruking.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NotificationTracker {

    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_LAST_SENT_DATE = "last_notification_date";

    private static final String SENT_TASK_IDS = "sent_task_ids";

    // 오늘 알림이 전송되었는지 확인
    public static boolean isTodayNotificationSent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastSent = prefs.getString(KEY_LAST_SENT_DATE, "");

        String today = getTodayDate();
        return today.equals(lastSent);
    }

    // 오늘 날짜로 알림 전송 기록 저장
    public static void markTodayNotificationAsSent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LAST_SENT_DATE, getTodayDate()).apply();
    }

    private static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    public static Set<String> getSentTodaySet(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(SENT_TASK_IDS, new HashSet<>());
    }

    // 발송된 항목 추가
    public static void markAsSent(Context context, int t_id) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> sent = new HashSet<>(getSentTodaySet(context));
        sent.add(String.valueOf(t_id));
        prefs.edit().putStringSet(SENT_TASK_IDS, sent).apply();
    }

    // 자정마다 초기화해야 함 (DailyWorker에서 호출)
    public static void resetSentToday(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(SENT_TASK_IDS, new HashSet<>()).apply();
    }
}
