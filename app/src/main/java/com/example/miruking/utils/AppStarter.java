package com.example.miruking.utils;

import android.content.Context;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.ExistingPeriodicWorkPolicy;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AppStarter {

    public static void scheduleDailyWorker(Context context) {
        PeriodicWorkRequest dailyRequest =
                new PeriodicWorkRequest.Builder(DailyWorker.class, 1, TimeUnit.DAYS)
                        .setInitialDelay(getInitialDelayUntilTargetTime(0), TimeUnit.HOURS) // 예: 오전 6시
                        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyWorker", // 고유 이름
                ExistingPeriodicWorkPolicy.UPDATE, // 이미 예약되어 있다면 갱신
                dailyRequest
        );
    }

    private static long getInitialDelayUntilTargetTime(int targetHour) {
        Calendar now = Calendar.getInstance();
        Calendar target = (Calendar) now.clone();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        long diffMillis = target.getTimeInMillis() - now.getTimeInMillis();
        return TimeUnit.MILLISECONDS.toHours(diffMillis); // 처음 실행까지 대기 시간 (시간 단위)
    }
}