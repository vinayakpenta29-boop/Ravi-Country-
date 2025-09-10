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

        // Load fonts from assets
        Typeface garamondSemiBold = Typeface.createFromAsset(getAssets(), "fonts/EBGaramond_SemiBold.ttf");
        Typeface loraBold = Typeface.createFromAsset(getAssets(), "fonts/Lora_Bold.ttf");

        // Splash screen layout
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(getResources().getColor(R.color.navy_blue));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        // App icon with rounded background
        ImageView icon = new ImageView(this);
        icon.setImageResource(R.mipmap.ic_launcher); // use your app icon or splash icon
        icon.setBackgroundResource(R.drawable.rounded_white_bg); // apply rounded corner white background
        icon.setPadding(24, 24, 24, 24); // inner padding so icon is nicely inset in rounded bg
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(220, 220);
        icon.setLayoutParams(iconParams);

        // App name in EBGaramond_SemiBold
        TextView appName = new TextView(this);
        appName.setText(getString(R.string.app_name));
        appName.setTextSize(28);
        appName.setTextColor(Color.WHITE);
        appName.setTypeface(garamondSemiBold);
        appName.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams appNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        appNameParams.topMargin = 36;
        appName.setLayoutParams(appNameParams);

        // "Created by RP" in Lora_Bold
        TextView credit = new TextView(this);
        credit.setText("Created by RP");
        credit.setTextColor(Color.WHITE);
        credit.setTextSize(16);
        credit.setTypeface(loraBold);
        credit.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams creditParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        creditParams.topMargin = 18;
        credit.setLayoutParams(creditParams);

        layout.addView(icon);
        layout.addView(appName);
        layout.addView(credit);

        setContentView(layout);

        // Transition to MainActivity after 1.6 sec
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 1600);
    }
}
