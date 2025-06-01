package com.example.miruking.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.miruking.R;

public class NagPopup {
    public static void show(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_nag_popup, null);

        TextView tvTitle = view.findViewById(R.id.tvNagTitle);
        TextView tvMessage = view.findViewById(R.id.tvNagMessage);
        Button btnClose = view.findViewById(R.id.btnNagClose);

        tvMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        // 팝업 크기 조절 (예: 화면 90% 너비, 400dp 높이)
        if (dialog.getWindow() != null) {
            int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.9);
            int height = (int)TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 500, context.getResources().getDisplayMetrics());
            dialog.getWindow().setLayout(width, height);
        }
    }
}
