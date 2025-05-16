package com.example.miruking.dao;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.R;
import com.example.miruking.activities.Todo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<Todo> todoList;
    private Context context;
    private int expandedPosition = -1;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public TodoAdapter(Context context, List<Todo> todoList) {
        this.context = context;
        this.todoList = todoList;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);

        holder.tvTodoName.setText(todo.getTodoName());
        holder.tvTodoTime.setText(todo.getTodoStartTime() + " ~ " + todo.getTodoEndTime());
        holder.tvTodoField.setText(todo.getTodoField());
        holder.tvTodoDelayStack.setText("미룬 횟수: " + todo.getTodoDelayStack());
        holder.tvTodoMemo.setText(todo.getTodoMemo());

        boolean isExpanded = position == expandedPosition;
        holder.layoutActionButtons.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;
            if (previousPosition != -1) notifyItemChanged(previousPosition);
            notifyItemChanged(position);
        });

        holder.btnComplete.setOnClickListener(v -> completeTodo(todo, position));
        holder.btnDelay.setOnClickListener(v -> delayTodo(todo, position));
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTodoName, tvTodoTime, tvTodoField, tvTodoDelayStack, tvTodoMemo;
        LinearLayout layoutActionButtons;
        Button btnComplete, btnDelay;

        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTodoName = itemView.findViewById(R.id.tvTodoName);
            tvTodoTime = itemView.findViewById(R.id.tvTodoTime);
            tvTodoField = itemView.findViewById(R.id.tvTodoField);
            tvTodoDelayStack = itemView.findViewById(R.id.tvTodoDelayStack);
            tvTodoMemo = itemView.findViewById(R.id.tvTodoMemo);
            layoutActionButtons = itemView.findViewById(R.id.layoutActionButtons);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnDelay = itemView.findViewById(R.id.btnDelay);
        }
    }

    // 날짜 바뀔 때 확장 상태 초기화
    public void collapseAllItems() {
        if (expandedPosition != -1) {
            int prev = expandedPosition;
            expandedPosition = -1;
            notifyItemChanged(prev);
        }
    }

    private void completeTodo(Todo todo, int position) {
        new Thread(() -> {
            MirukingDBHelper dbHelper = new MirukingDBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.beginTransaction();
            try {
                // 1. 로그 추가
                ContentValues logValues = new ContentValues();
                logValues.put("todo_ID", todo.getTodoId());
                logValues.put("todo_state", "완료");
                logValues.put("timestamp", System.currentTimeMillis());
                db.insert("TODO_LOGS", null, logValues);

                // 2. 통계 업데이트
                updateStats("done_num");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }

            mainHandler.post(() -> {
                todoList.remove(position);
                notifyItemRemoved(position);
            });
        }).start();
    }

    private void delayTodo(Todo todo, int position) {
        new Thread(() -> {
            MirukingDBHelper dbHelper = new MirukingDBHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.beginTransaction();
            Pair<String, Integer> nagPair = null;
            try {
                // 1. 일정 날짜/미룬 횟수 업데이트
                ContentValues todoValues = new ContentValues();
                todoValues.put("todo_start_date", getNextDay(todo.getTodoStartDate()));
                todoValues.put("todo_end_date", getNextDay(todo.getTodoEndDate()));
                todoValues.put("todo_delay_stack", todo.getTodoDelayStack() + 1);
                db.update("TODOS", todoValues, "todo_ID=?", new String[]{String.valueOf(todo.getTodoId())});

                // 2. 로그 추가
                ContentValues logValues = new ContentValues();
                logValues.put("todo_ID", todo.getTodoId());
                logValues.put("todo_state", "미룸");
                logValues.put("timestamp", System.currentTimeMillis());
                db.insert("TODO_LOGS", null, logValues);

                // 3. 통계 업데이트
                updateStats("delay_num");

                // 4. 잔소리 문구 + 미룬 횟수 조회
                nagPair = getNagMessage(db, todo.getTodoId());

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                db.close();
            }

            final String finalNagText;
            if (nagPair != null) {
                finalNagText = "이 일정을 미룬지 " + nagPair.second + "일째 입니다.\n\n" + nagPair.first;
            } else {
                finalNagText = "이 일정을 미룬지 ?일째 입니다.\n\n오늘도 미루는구나...";
            }

            mainHandler.post(() -> {
                todoList.remove(position);
                notifyItemRemoved(position);
                showNagPopup(finalNagText);
            });
        }).start();
    }

    private void updateStats(String column) {
        SQLiteDatabase db = new MirukingDBHelper(context).getWritableDatabase();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Cursor cursor = db.rawQuery("SELECT stat_num FROM STATS WHERE reference_date = ?", new String[]{today});
        if (cursor.moveToFirst()) {
            db.execSQL("UPDATE STATS SET " + column + " = " + column + " + 1 WHERE reference_date = ?", new String[]{today});
        } else {
            ContentValues values = new ContentValues();
            values.put("reference_date", today);
            values.put(column, 1);
            db.insert("STATS", null, values);
        }
        cursor.close();
        db.close();
    }

    private String getNextDay(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);

            return sdf.format(cal.getTime());
        } catch (Exception e) {
            return dateStr;
        }
    }

    // 잔소리 문구 + 미룬 횟수 조회
    private Pair<String, Integer> getNagMessage(SQLiteDatabase db, int todoId) {
        String nagText = "오늘도 미루는구나...";
        int delayStack = 0;
        Cursor cursor = null;
        try {
            // 연결된 잔소리 + 미룬 횟수 조회
            cursor = db.rawQuery(
                    "SELECT n.nag_txt, t.todo_delay_stack FROM TODOS t " +
                            "LEFT JOIN TODOS_NAGS tn ON t.todo_ID = tn.todo_ID " +
                            "LEFT JOIN NAGS n ON tn.nag_ID = n.nag_ID " +
                            "WHERE t.todo_ID = ? AND n.nag_txt IS NOT NULL",
                    new String[]{String.valueOf(todoId)}
            );
            if (cursor.moveToFirst()) {
                nagText = cursor.getString(0);
                delayStack = cursor.getInt(1);
            } else {
                // 연결된 잔소리가 없으면 랜덤 선택 + 미룬 횟수 조회
                cursor.close();
                cursor = db.rawQuery(
                        "SELECT todo_delay_stack FROM TODOS WHERE todo_ID = ?",
                        new String[]{String.valueOf(todoId)}
                );
                if (cursor.moveToFirst()) {
                    delayStack = cursor.getInt(0);
                }
                cursor.close();
                cursor = db.rawQuery(
                        "SELECT nag_txt FROM NAGS ORDER BY RANDOM() LIMIT 1",
                        null
                );
                if (cursor.moveToFirst()) {
                    nagText = cursor.getString(0);
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return new Pair<>(nagText, delayStack);
    }

    private void showNagPopup(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("미루지 말고 지금 해야죠!")
                .setMessage(message)
                .setPositiveButton("닫기", (dialog, which) -> dialog.dismiss())
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.parseColor("#B22222"));
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.nag_popup_bg);
            }
        });
        dialog.show();
    }
}
