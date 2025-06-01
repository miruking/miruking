package com.example.miruking;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miruking.activities.CompleteTodo;
import com.example.miruking.activities.DelayTodo;
import com.example.miruking.activities.DeleteTodo;
import com.example.miruking.activities.NagPopup;
import com.example.miruking.activities.Todo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private final List<Todo> todoList;
    private final Context context;
    private int expandedPosition = -1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

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

        // UI 바인딩 로직
        holder.tvTodoName.setText(todo.getTodoName());
        holder.tvTodoTime.setText(todo.getTodoStartTime() + " ~ " + todo.getTodoEndTime());
        holder.tvTodoField.setText(todo.getTodoField());
        holder.tvTodoDelayStack.setText("미룬 횟수: " + todo.getTodoDelayStack());
        holder.tvTodoMemo.setText(todo.getTodoMemo());

        // 확장/축소 상태 관리
        boolean isExpanded = position == expandedPosition;
        holder.layoutActionButtons.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = expandedPosition;
            expandedPosition = isExpanded ? -1 : position;
            if (previousPosition != -1) notifyItemChanged(previousPosition);
            notifyItemChanged(position);
        });

        // 완료 버튼
        holder.btnComplete.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            executor.execute(() -> {
                new CompleteTodo(context).complete(todo); // 로그 + 통계만 업데이트
                handler.post(() -> {
                    todoList.remove(currentPos);
                    notifyItemRemoved(currentPos);

                    // 통계 화면 갱신 (MainActivity에 구현 필요)
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).refreshStatsFragment();
                    }
                });
            });
        });

        // 미루기 버튼
        holder.btnDelay.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            executor.execute(() -> {
                Pair<String, Integer> nagPair = new DelayTodo(context).delay(todo);
                String finalNagText = "이 일정을 미룬지 " + nagPair.second + "일째 입니다.\n\n" + nagPair.first;
                handler.post(() -> {
                    todoList.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    NagPopup.show(context, finalNagText);
                });
            });
        });

        // 세로 점(⋮) 버튼 - 커스텀 팝업
        holder.btnMore.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(context);
            View popupView = inflater.inflate(R.layout.menu_custom_popup, null);
            PopupWindow popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            Button btnDelete = popupView.findViewById(R.id.btnDelete);
            btnDelete.setOnClickListener(view -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    new DeleteTodo(context).delete(todo, currentPos, () -> {
                        todoList.remove(currentPos);
                        notifyItemRemoved(currentPos);
                        popupWindow.dismiss();
                    });
                }
            });

            popupWindow.showAsDropDown(holder.btnMore);
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public void collapseAllItems() {
        if (expandedPosition != -1) {
            int prev = expandedPosition;
            expandedPosition = -1;
            notifyItemChanged(prev);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        executor.shutdown();
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTodoName, tvTodoTime, tvTodoField, tvTodoDelayStack, tvTodoMemo;
        LinearLayout layoutActionButtons;
        Button btnComplete, btnDelay;
        ImageButton btnMore;

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
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}
