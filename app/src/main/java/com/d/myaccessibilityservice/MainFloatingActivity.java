package com.d.myaccessibilityservice;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class MainFloatingActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 111;
    private WindowManager windowManager;
    private LinearLayout floatingWindowView;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    // 权限被拒绝
                } else {
                    show();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_floating);

        EditText input = (EditText) findViewById(R.id.interval_input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null || TextUtils.isEmpty(s.toString())) {
                    MyAccessibilityService.interval = 8000;
                } else {
                    try {
                        MyAccessibilityService.interval = Long.parseLong(s.toString()) * 1000;
                    } catch (Exception e) {
                        MyAccessibilityService.interval = 8000;
                        input.setText(String.valueOf(8));
                    }
                }
            }
        });
        findViewById(R.id.show).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainFloatingActivity.this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        // 创建悬浮窗
                        show();
                    }
                } else {
                    // 创建悬浮窗
                    show();
                }
            }
        });
        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAccessibilityService.canPull = false;
                if (windowManager != null && floatingWindowView != null
                        && floatingWindowView.isAttachedToWindow()) {
                    windowManager.removeView(floatingWindowView);
                }
            }
        });
    }

    public void show() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatingWindowView = new LinearLayout(this);
        // Create a new View object
        Button open = new Button(this);
        open.setText("开始");
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAccessibilityService.canPull = true;
            }
        });
        Button close = new Button(this);
        close.setText("停止");
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyAccessibilityService.canPull = false;
                if (windowManager != null && floatingWindowView != null
                        && floatingWindowView.isAttachedToWindow()) {
                    windowManager.removeView(floatingWindowView);
                }
            }
        });
        //floatingWindowView.addView(open);
        floatingWindowView.addView(close);
        floatingWindowView.setPadding(50, 50, 50, 50);
        // Set layout parameters for the View object
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSPARENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = windowManager.getDefaultDisplay().getWidth() - 500;
        params.y = windowManager.getDefaultDisplay().getHeight() - 500;
        MyAccessibilityService.canPull = true;

        floatingWindowView.setOnTouchListener(new View.OnTouchListener() {
            private int mLastX;
            private int mLastY;
            private int mStartX;
            private int mStartY;
            private boolean isMoving;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isMoving = false;
                        mLastX = rawX;
                        mLastY = rawY;
                        mStartX = (int) event.getX();
                        mStartY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        isMoving = true;
                        WindowManager.LayoutParams layoutParams =
                                (WindowManager.LayoutParams) params;

                        layoutParams.x = rawX - mStartX;
                        layoutParams.y = rawY - mStartY;
                        getWindowManager().updateViewLayout(floatingWindowView, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {

                        }
                        break;
                }
                return true;
            }

        });

        // Add the View object to the WindowManager
        windowManager.addView(floatingWindowView, params);
    }

}