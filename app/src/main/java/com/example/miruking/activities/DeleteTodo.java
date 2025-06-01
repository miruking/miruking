package com.example.miruking.activities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.miruking.dao.DeleteTodoDAO;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeleteTodo {
    private final DeleteTodoDAO deleteTodoDAO;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public DeleteTodo(Context context) {
        deleteTodoDAO = new DeleteTodoDAO(context);
    }

    public void delete(Todo todo, int position, Runnable onSuccess) {
        executor.execute(() -> {
            deleteTodoDAO.deleteTodo(todo.getTodoId());
            handler.post(onSuccess);
        });
    }
}
