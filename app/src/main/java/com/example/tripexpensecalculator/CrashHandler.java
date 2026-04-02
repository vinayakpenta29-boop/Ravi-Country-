package com.example.tripexpensecalculator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    public CrashHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {

        String error = android.util.Log.getStackTraceString(throwable);

        Intent intent = new Intent(context, CrashActivity.class);
        intent.putExtra("error", error);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(intent);

        // Kill app safely after launching crash screen
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
