package com.example.miruking.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;

import com.example.miruking.dao.NagDAO;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NagUpdate {
    private final NagDAO nagDAO;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());


    /*

    runAsyncDbOperation 메서드 도입으로 공통적인 비동기 처리 구조를 단일화
    postOnMain 메서드로 null 체크와 handler.post 호출 분리하여 중복 제거
    AlertDialog 빌더 체이닝 개선 및 EditText 임포트 정리
    람다 표현식 및 메서드 참조 활용으로 코드 간결화 가능하지만 가독성 고려해 그대로 유지


    따로 잔소리를 설정 안 할 때만 Nag table에 있는 잔소리 사용. Nag 테이블에 잔소리를 업데이트 할 수 있는 UI가 없음.
    북마크 잔소리 테이블 작동 X, 북마크용 잔소리 추가  UI 없는 듯
     */
    public NagUpdate(Context context) {
        nagDAO = new NagDAO(context);
    }

    // 모든 잔소리 문구 조회
    public List<Nag> getAllNags() {
        return nagDAO.getAllNags();
    }

    // 공통된 비동기 DB 작업 수행 메서드
    private void runAsyncDbOperation(Runnable dbOperation, Runnable onComplete) {
        executor.execute(() -> {
            dbOperation.run();
            postOnMain(onComplete);
        });
    }

    private void postOnMain(Runnable runnable) {
        if (runnable != null) {
            handler.post(runnable);
        }
    }

    // 새 잔소리 문구 추가
    public void addNag(String nagText, Runnable onComplete) {
        runAsyncDbOperation(() -> nagDAO.insertNag(nagText), onComplete);
    }

    // 잔소리 문구 수정
    public void editNag(int nagId, String newText, Runnable onComplete) {
        runAsyncDbOperation(() -> nagDAO.updateNag(nagId, newText), onComplete);
    }

    // 잔소리 문구 삭제
    public void deleteNag(int nagId, Runnable onComplete) {
        runAsyncDbOperation(() -> nagDAO.deleteNag(nagId), onComplete);
    }

    // 리소스 정리
    public void shutdown() {
        executor.shutdown();
    }

    // 잔소리 추가 다이얼로그
    public static void showAddNagDialog(Context context, NagUpdate nagUpdate, Runnable onComplete) {
        EditText input = new EditText(context);
        new AlertDialog.Builder(context)
                .setTitle("새 잔소리 추가")
                .setView(input)
                .setPositiveButton("추가", (dialog, which) -> {
                    String text = input.getText().toString();
                    nagUpdate.addNag(text, onComplete);
                })
                .setNegativeButton("취소", null)
                .show();
    }
}