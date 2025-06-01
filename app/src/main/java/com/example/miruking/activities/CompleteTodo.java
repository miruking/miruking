package com.example.miruking.activities;

import android.content.Context;
import com.example.miruking.dao.CompleteTodoDAO;

public class CompleteTodo {
    private final CompleteTodoDAO completeTodoDAO;

    public CompleteTodo(Context context) {
        completeTodoDAO = new CompleteTodoDAO(context);
    }

    public void complete(Todo todo) {
        // 완료 처리 한 번에!
        completeTodoDAO.completeTodo(todo.getTodoId());
    }
}
