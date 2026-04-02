package com.example.tripexpensecalculator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {

    private String errorText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        errorText = getIntent().getStringExtra("error");

        // Root Layout
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 30, 30, 30);
        root.setBackgroundColor(Color.WHITE);

        // Title
        TextView title = new TextView(this);
        title.setText("❌ App Crashed");
        title.setTextColor(Color.RED);
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);

        // Scrollable Error Text
        TextView errorView = new TextView(this);
        errorView.setText(errorText);
        errorView.setTextColor(Color.BLACK);
        errorView.setTextSize(14);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(errorView);

        // COPY BUTTON
        Button copyBtn = new Button(this);
        copyBtn.setText("Copy Error");

        copyBtn.setOnClickListener(v -> {
            ClipboardManager clipboard =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText("Crash Log", errorText);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "Copied to clipboard ✅", Toast.LENGTH_SHORT).show();
        });

        // Add views
        root.addView(title);
        root.addView(scrollView);
        root.addView(copyBtn);

        setContentView(root);
    }
}
