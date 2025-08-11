package com.example.miruking.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.miruking.R;

/*
상수 분리 : 하드코딩된 팝업 너비 비율(0.9f)과 높이(500dp)를 상수(WIDTH_RATIO, HEIGHT_DP)로
          분리하여 유지보수 편리성 향상.

팝업 크기 조절 코드 분리: 팝업 크기 조절 부분을 setDialogSize()라는 별도 메서드로 분리해
                        show() 메서드가 간결해지고 가독성 개선.

setCancelable 호출 위치 변경: AlertDialog.Builder의 setCancelable(false) 대신, create() 후
                             dialog.setCancelable(false) 호출로 변경. (기능적 차이는 없으며, 코드 스타일 개선 차원)

불필요한 뷰 변수 제거: 타이틀 텍스트뷰(tvNagTitle)가 사용되지 않아 제거함으로써 코드가 더 깔끔해짐.
 */
public class NagPopup {
    private static final float WIDTH_RATIO = 0.9f;
    private static final int HEIGHT_DP = 500;

    public static void show(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_nag_popup, null);

        TextView tvMessage = view.findViewById(R.id.tvNagMessage);
        Button btnClose = view.findViewById(R.id.btnNagClose);

        tvMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        dialog.setCancelable(false);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        setDialogSize(dialog, context);
    }

    private static void setDialogSize(AlertDialog dialog, Context context) {
        if (dialog.getWindow() != null) {
            int width = (int)(context.getResources().getDisplayMetrics().widthPixels * WIDTH_RATIO);
            int height = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, HEIGHT_DP, context.getResources().getDisplayMetrics());
            dialog.getWindow().setLayout(width, height);
        }
    }
}
