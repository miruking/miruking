package com.example.miruking.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.miruking.dao.NagDAO;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NagUpdate {
    private final NagDAO nagDAO;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public NagUpdate(Context context) {
        nagDAO = new NagDAO(context);
    }

    // 모든 잔소리 문구 조회
    public List<Nag> getAllNags() {
        return nagDAO.getAllNags();
    }

    // 새 잔소리 문구 추가
    public void addNag(String nagText, Runnable onComplete) {
        executor.execute(() -> {
            long result = nagDAO.insertNag(nagText);
            handler.post(() -> {
                if (onComplete != null) onComplete.run();
            });
        });
    }

    // 잔소리 문구 수정
    public void editNag(int nagId, String newText, Runnable onComplete) {
        executor.execute(() -> {
            int result = nagDAO.updateNag(nagId, newText);
            handler.post(() -> {
                if (onComplete != null) onComplete.run();
            });
        });
    }

    // 잔소리 문구 삭제
    public void deleteNag(int nagId, Runnable onComplete) {
        executor.execute(() -> {
            int result = nagDAO.deleteNag(nagId);
            handler.post(() -> {
                if (onComplete != null) onComplete.run();
            });
        });
    }

    // 리소스 정리
    public void shutdown() {
        executor.shutdown();
    }

    // 잔소리 추가 다이얼로그
    public static void showAddNagDialog(Context context, NagUpdate nagUpdate, Runnable onComplete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final android.widget.EditText input = new android.widget.EditText(context);
        builder.setTitle("새 잔소리 추가")
                .setView(input)
                .setPositiveButton("추가", (dialog, which) -> {
                    String text = input.getText().toString();
                    nagUpdate.addNag(text, onComplete);
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
