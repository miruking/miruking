package com.example.miruking.activities;

import android.content.Context;
import android.util.Pair;
import com.example.miruking.dao.DelayTodoDAO;

public class DelayTodo {
    private final DelayTodoDAO dao;

    public DelayTodo(Context context) {
        dao = new DelayTodoDAO(context);
    }

    public Pair<String, Integer> delay(Todo todo) {
        return dao.delayTodo(todo.getTodoId(), todo.getTodoStartDate(), todo.getTodoEndDate(), todo.getTodoDelayStack());
    }
}