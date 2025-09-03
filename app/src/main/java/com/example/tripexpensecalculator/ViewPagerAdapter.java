package com.example.tripexpensecalculator;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.tripexpensecalculator.fragments.ExpenseFragment;
import com.example.tripexpensecalculator.fragments.FriendsFragment;
import com.example.tripexpensecalculator.fragments.ReportFragment;
import com.example.tripexpensecalculator.fragments.SummaryFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0: return new FriendsFragment();
            case 1: return new ExpenseFragment();
            case 2: return new SummaryFragment();
            case 3: return new ReportFragment();
            default: return new FriendsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
