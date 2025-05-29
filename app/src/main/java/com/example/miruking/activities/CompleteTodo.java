package com.example.miruking.activities;

import android.content.Context;
import com.example.miruking.dao.CompleteTodoDAO;

public class CompleteTodo {
    private CompleteTodoDAO completeTodoDAO;

    public CompleteTodo(Context context) {
        completeTodoDAO = new CompleteTodoDAO(context);
    }

    public void complete(Todo todo) {
        completeTodoDAO.insertCompleteLog(todo.getTodoId());
        completeTodoDAO.updateStats();
    }
}
