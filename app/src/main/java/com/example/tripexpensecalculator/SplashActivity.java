package com.example.tripexpensecalculator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.Gravity;
import android.graphics.Typeface;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Orange background, vertical center
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(getResources().getColor(R.color.navy_blue));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        // App icon
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher); // your launcher icon
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(220, 220);
        icon.setLayoutParams(iconParams);

        // App name bold, white
        TextView appName = new TextView(this);
        appName.setText(getString(R.string.app_name));
        appName.setTextSize(28);
        appName.setTextColor(Color.WHITE);
        appName.setTypeface(Typeface.DEFAULT_BOLD);
        appName.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams appNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        appNameParams.topMargin = 36;
        appName.setLayoutParams(appNameParams);

        // "Created by RP"
        TextView credit = new TextView(this);
        credit.setText("Created by RP");
        credit.setTextColor(Color.WHITE);
        credit.setTextSize(16);
        credit.setTypeface(Typeface.DEFAULT);
        credit.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams creditParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        creditParams.topMargin = 18;
        credit.setLayoutParams(creditParams);

        layout.addView(icon);
        layout.addView(appName);
        layout.addView(credit);

        setContentView(layout);

        // Delay 1.6s, then launch MainActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 1600);
    }
}
