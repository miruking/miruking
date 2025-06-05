package com.example.miruking.utils;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.miruking.dao.StatDao;
import com.example.miruking.DB.MirukingDBHelper;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.miruking.DB.MirukingDBHelper;

public class DailyWorker extends Worker {

    public DailyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("DailyWorker", "doWork() called");

        Context context = getApplicationContext();
        StatDao statDao = new StatDao(context);
        statDao.insertDailyStat();

        NotificationTracker.resetSentToday(context); // 발송 목록 초기화
        AlarmReceiver.sendNotifications(context);    // 당일 알림 다시 설정

        return Result.success();
    }
}