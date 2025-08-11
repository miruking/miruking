package com.example.miruking.activities;

import android.content.Context;
import android.util.Pair;
import com.example.miruking.dao.DelayTodoDAO;

/**
 * 서비스 클래스: DelayTodoDAO를 호출하여 Todo 연기 로직 수행
 */
public class DelayTodo {
    private final DelayTodoDAO dao;

    public DelayTodo(Context context) {
        dao = new DelayTodoDAO(context);
    }

    public Pair<String, Integer> delay(Todo todo) {
        return dao.delayTodo(
                todo.getTodoId(),
                todo.getTodoStartDate(),
                todo.getTodoEndDate(),
                todo.getTodoDelayStack()
        );
    }
}