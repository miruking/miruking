package com.example.miruking.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.miruking.dao.StatDao;
import com.example.miruking.DB.MirukingDBHelper;

import android.database.sqlite.SQLiteDatabase;

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
        // StatDao statDao = new StatDao(db);
        // int delayCount = statDao.countTasksByState("미룸", "yesterday");
        // int doneCount = statDao.countTasksByState("완료", "yesterday");
        // statDao.insertDailyStat(delayCount, doneCount);

        // 2. 오늘 하루치 알림 예약
        // AlarmScheduler.scheduleAllTodayAlarms(context, db);

        return Result.success();
    }
}