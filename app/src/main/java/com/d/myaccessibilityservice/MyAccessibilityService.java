package com.d.myaccessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    public static boolean canPull;
    public static long interval;
    Handler handler = new Handler();
    private String switchStr = "暂无最新订单";
    private GestureDescription gestureDescription;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent() called with: event = [" + event + "]");
        if (event.getSource() == null) {
            return;
        }
        if (gestureDescription == null) {
            initGestureDescription();
        }

        if (canPull || findSwitchStr(getRootInActiveWindow())) {
            repeatPullDown();
        } else {
            stop();
        }
    }

    private void repeatPullDown() {
        stop();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullDown();
                repeatPullDown();
            }
        }, interval);
    }

    private void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    private void pullDown() {
        Log.d(TAG, "pullDown() called");
        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    private void initGestureDescription() {
        final int screenWidth = 1080;
        final int screenHeight = 1920;

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(screenWidth / 2, screenHeight / 3);
        path.lineTo(screenWidth / 2, screenHeight / 3 * 2);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 20, 400));
        gestureDescription = gestureBuilder.build();
    }

    private boolean findSwitchStr(AccessibilityNodeInfo source) {
        if (source == null) {
            return false;
        }
        boolean childResult = false;
        for (int i = 0; i < source.getChildCount(); i++) {
            childResult = childResult || findSwitchStr(source.getChild(i));
        }
        if (childResult) {
            return true;
        }
        return source.getText() != null && source.getText().toString().contains(switchStr);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @Override
    public void onInterrupt() {
    }
}

