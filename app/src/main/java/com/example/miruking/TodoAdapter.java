package com.example.miruking;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.miruking.activities.CompleteTodo;
import com.example.miruking.activities.DelayTodo;
import com.example.miruking.activities.DeleteTodo;
import com.example.miruking.activities.NagPopup;
import com.example.miruking.activities.ScheduleDialogManager;
import com.example.miruking.activities.Todo;
import com.example.miruking.DB.MirukingDBHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private final List<Todo> todoList;
    private final Context context;
    private int expandedPosition = -1;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    //수정 메뉴(25.06.02)Add commentMore actions
    private final MirukingDBHelper dbHelper;
    private final ScheduleDialogManager dialogManager;
    //수정 메뉴(25.06.02)
    public TodoAdapter(Context context, List<Todo> todoList, MirukingDBHelper dbHelper, ScheduleDialogManager dialogManager) {
        this.context = context;
        this.todoList = todoList;
        this.dbHelper = dbHelper;
        this.dialogManager = dialogManager;
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

            //수정 메뉴(25.06.02)Add commentMore actions
            //다른 일정 리스트 기능 추가후 작동하는지 확인해야함
            Button btnEdit = popupView.findViewById(R.id.btnEdit);
            btnEdit.setOnClickListener(view -> {
                int currentPos = holder.getAdapterPosition();
                if(currentPos != RecyclerView.NO_POSITION){
                    Todo todoToEdit = todoList.get(currentPos);
                    String type = todoToEdit.getTodoField();

                    ScheduleDialogManager.OnScheduleUpdatedListener refreshAndDismiss = (int newTodoId) -> {
                        if (context instanceof MainActivity) {
                            Fragment frag = ((MainActivity) context)
                                    .getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                            if (frag instanceof ScheduleFragment) {
                                ((ScheduleFragment) frag).loadTodosForDate(
                                        ((ScheduleFragment) frag).getCurrentDate()
                                );
                            }
                        }
                        popupWindow.dismiss();
                    };
                    //루틴 수정에서 월 수 금만 불러오는 문제 수정(25.06.06)
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    switch (type) {
                        case "일반":
                            dialogManager.showUpdateTodoDialog(todoToEdit, holder.itemView, refreshAndDismiss);
                            break;
                        case "d-day":
                            dialogManager.showUpdateDdayDialog(todoToEdit.toDday(), holder.itemView, refreshAndDismiss);
                            break;
                        case "routine":
                            dialogManager.showUpdateRoutineDialog(todoToEdit.toRoutine(db), holder.itemView, refreshAndDismiss);
                            break;
                        default:
                            Toast.makeText(context, "알 수 없는 일정 종류입니다: " + type, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });

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
