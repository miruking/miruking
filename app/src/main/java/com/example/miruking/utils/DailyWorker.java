package com.example.miruking.utils;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.miruking.dao.StatDao;
import com.example.miruking.DB.MirukingDBHelper;

import android.database.sqlite.SQLiteDatabase;

import com.example.miruking.DB.MirukingDBHelper;

public class DailyWorker extends Worker {

    public DailyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        MirukingDBHelper dbHelper = new MirukingDBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. 어제 성취 통계 기록
        StatDao statDao = new StatDao(context);
        statDao.insertDailyStat();

        // 2. 오늘 하루치 알림 예약
        AlarmReceiver.sendNotifications(context);

        return Result.success();
    }
}