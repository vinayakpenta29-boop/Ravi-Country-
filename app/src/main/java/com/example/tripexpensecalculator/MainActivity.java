package com.example.tripexpensecalculator;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpensecalculator.fragments.ExpenseFragment;
import com.example.tripexpensecalculator.fragments.FriendsFragment;
import com.example.tripexpensecalculator.fragments.ReportFragment;
import com.example.tripexpensecalculator.fragments.SummaryFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Friends"); break;
                case 1: tab.setText("Expenses"); break;
                case 2: tab.setText("Summary"); break;
                case 3: tab.setText("Report"); break;
            }
        }).attach();
    }
}
