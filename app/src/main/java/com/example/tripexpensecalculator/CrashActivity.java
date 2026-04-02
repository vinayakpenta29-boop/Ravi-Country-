package com.example.tripexpensecalculator;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String error = getIntent().getStringExtra("error");

        TextView tv = new TextView(this);
        tv.setText("❌ App Crashed\n\n" + error);
        tv.setTextColor(Color.RED);
        tv.setTextSize(14);
        tv.setPadding(30, 30, 30, 30);

        setContentView(tv);
    }
}
