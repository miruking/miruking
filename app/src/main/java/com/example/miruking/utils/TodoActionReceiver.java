package com.example.miruking.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import com.example.miruking.dao.CompleteTodoDAO;
import com.example.miruking.dao.DelayTodoDAO;
import com.example.miruking.dao.LogDAO;

public class TodoActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int t_id = intent.getIntExtra("t_id", -1);
        if (t_id == -1 || action == null) return;

        NotificationManagerCompat.from(context).cancel(t_id); // 알림 제거

        if ("ACTION_DONE".equals(action)) {
            // ✅ 완료 처리
            CompleteTodoDAO completeDao = new CompleteTodoDAO(context);
            completeDao.completeTodo(t_id);

        } else if ("ACTION_DELAY".equals(action)) {
            String startDate = intent.getStringExtra("start_date");
            String endDate = intent.getStringExtra("end_date");
            int delayStack = intent.getIntExtra("delay_stack", 0);

            DelayTodoDAO delayDao = new DelayTodoDAO(context);
            delayDao.delayTodo(t_id, startDate, endDate, delayStack);
        }
    }
}
