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

import com.example.miruking.DB.MirukingDBHelper;
import com.example.miruking.activities.CompleteTodo;
import com.example.miruking.activities.DelayTodo;
import com.example.miruking.activities.DeleteTodo;
import com.example.miruking.activities.NagPopup;
import com.example.miruking.activities.ScheduleDialogManager;
import com.example.miruking.activities.Todo;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 클릭 이벤트 분리 (handleComplete, handleDelay, handleEdit, handleDelete)
 * 팝업 메뉴 추출 (showPopupMenu)
 * 위치 검증 공통 처리 (withValidPosition)
 * onBindViewHolder() 간결화 및 UI 바인딩 분리




 * RecyclerView.Adapter for displaying and interacting with the list of todos.
 */
public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private final List<Todo> todoList;
    private final Context context;
    private int expandedPosition = -1; // currently expanded item position
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MirukingDBHelper dbHelper;
    private final ScheduleDialogManager dialogManager;

    public TodoAdapter(Context context, List<Todo> todoList, MirukingDBHelper dbHelper, ScheduleDialogManager dialogManager) {
        this.context = context;
        this.todoList = todoList;
        this.dbHelper = dbHelper;
        this.dialogManager = dialogManager;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate todo item layout
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todoList.get(position);
        bindTodoData(holder, todo, position);      // UI 요소에 데이터 바인딩
        setupClickEvents(holder, todo);            // 클릭 이벤트 설정
    }

    /**
     * UI 요소에 할 일 데이터를 바인딩하고 확장 상태 표시
     */
    private void bindTodoData(@NonNull TodoViewHolder holder, Todo todo, int position) {
        holder.tvTodoName.setText(todo.getTodoName());
        holder.tvTodoTime.setText(todo.getTodoStartTime() + " ~ " + todo.getTodoEndTime());
        holder.tvTodoField.setText(todo.getTodoField());
        holder.tvTodoDelayStack.setText("미룬 횟수: " + todo.getTodoDelayStack());
        holder.tvTodoMemo.setText(todo.getTodoMemo());

        boolean isExpanded = position == expandedPosition;
        holder.layoutActionButtons.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            int prev = expandedPosition;
            expandedPosition = (isExpanded ? -1 : position);
            if (prev != -1) notifyItemChanged(prev);
            notifyItemChanged(position);
        });
    }

    /**
     * 완료/미루기/더보기 버튼의 클릭 이벤트 설정
     */
    private void setupClickEvents(@NonNull TodoViewHolder holder, Todo todo) {
        holder.btnComplete.setOnClickListener(v -> withValidPosition(holder, pos -> handleComplete(todo, pos)));
        holder.btnDelay.setOnClickListener(v -> withValidPosition(holder, pos -> handleDelay(todo, pos)));
        holder.btnMore.setOnClickListener(v -> showPopupMenu(holder, todo));
    }

    /**
     * 할 일 완료 처리 (DB 업데이트 후 리스트에서 제거)
     */
    private void handleComplete(Todo todo, int position) {
        executor.execute(() -> {
            new CompleteTodo(context).complete(todo);
            handler.post(() -> {
                todoList.remove(position);
                notifyItemRemoved(position);
            });
        });
    }

    /**
     * 할 일 미루기 처리 + 잔소리 팝업 표시
     */
    private void handleDelay(Todo todo, int position) {
        executor.execute(() -> {
            Pair<String, Integer> nagPair = new DelayTodo(context).delay(todo);
            String finalNagText = "이 일정을 미룬지 " + nagPair.second + "일째 입니다.\n\n" + nagPair.first;
            handler.post(() -> {
                todoList.remove(position);
                notifyItemRemoved(position);
                NagPopup.show(context, finalNagText);
            });
        });
    }

    /**
     * 더보기 메뉴 팝업 표시 (수정/삭제 버튼 포함)
     */
    private void showPopupMenu(@NonNull TodoViewHolder holder, Todo todo) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.menu_custom_popup, null);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        Button btnEdit = popupView.findViewById(R.id.btnEdit);
        Button btnDelete = popupView.findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(view -> withValidPosition(holder, pos -> {
            handleEdit(todo, holder, popupWindow);
        }));

        btnDelete.setOnClickListener(view -> withValidPosition(holder, pos -> {
            new DeleteTodo(context).delete(todo, pos, () -> {
                todoList.remove(pos);
                notifyItemRemoved(pos);
                popupWindow.dismiss();
            });
        }));

        popupWindow.showAsDropDown(holder.btnMore);
    }

    /**
     * 할 일 수정 다이얼로그 표시 (일반, 디데이, 루틴 종류에 따라 다름)
     */
    private void handleEdit(Todo todo, TodoViewHolder holder, PopupWindow popupWindow) {
        String type = todo.getTodoField();
        ScheduleDialogManager.OnScheduleUpdatedListener refreshAndDismiss = (int newTodoId) -> {
            if (context instanceof MainActivity) {
                Fragment frag = ((MainActivity) context).getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (frag instanceof ScheduleFragment) {
                    ((ScheduleFragment) frag).loadTodosForDate(((ScheduleFragment) frag).getCurrentDate());
                }
            }
            popupWindow.dismiss();
        };

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        switch (type) {
            case "일반":
                dialogManager.showUpdateTodoDialog(todo, holder.itemView, refreshAndDismiss);
                break;
            case "d-day":
                dialogManager.showUpdateDdayDialog(todo.toDday(), holder.itemView, refreshAndDismiss);
                break;
            case "routine":
                dialogManager.showUpdateRoutineDialog(todo.toRoutine(db), holder.itemView, refreshAndDismiss);
                break;
            default:
                Toast.makeText(context, "알 수 없는 일정 종류입니다: " + type, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 유효한 position인지 확인한 후 action 수행
     */
    private void withValidPosition(TodoViewHolder holder, java.util.function.IntConsumer action) {
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION) {
            action.accept(position);
        }
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    /**
     * 펼쳐진 항목 모두 닫기 (탭 외 영역 터치 시 등)
     */
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
        executor.shutdown(); // 리소스 정리
    }

    /**
     * ViewHolder 정의: 할 일 아이템의 UI 구성요소 바인딩
     */
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