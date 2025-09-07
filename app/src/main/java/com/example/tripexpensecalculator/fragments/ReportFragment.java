package com.example.tripexpensecalculator.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import java.util.*;

public class ReportFragment extends Fragment {

    private LinearLayout reportRootLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report, container, false);
        reportRootLayout = root.findViewById(R.id.reportRootLayout);
        displayDetailedReport();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayDetailedReport();
    }

    private void displayDetailedReport() {
        reportRootLayout.removeAllViews();

        List<String> expenseTypes = ExpenseFragment.getExpenseTypes();
        List<Double> expenseAmounts = ExpenseFragment.getExpenseAmounts();
        Map<String, Double> contributions = FriendsFragment.getContributions();

        double totalExpense = sum(expenseAmounts);
        double totalContribution = sum(contributions);
        double balance = totalContribution - totalExpense;

        // Section 1: Transaction Log
        LinearLayout logBox = getCurvedBox();
        logBox.addView(getHeaderTextView("All Expenses (Transaction Log)"));
        for (int i = 0; i < expenseTypes.size(); i++) {
            logBox.addView(getRowTextView(
                (i + 1) + ". " + expenseTypes.get(i),
                "₹" + String.format("%.2f", expenseAmounts.get(i))
            ));
            if (i < expenseTypes.size() - 1) logBox.addView(getDivider());
        }
        reportRootLayout.addView(logBox);

        // Section 2: Category-wise summary
        LinearLayout categoryBox = getCurvedBox();
        categoryBox.addView(getHeaderTextView("Category-wise Summary"));
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        for (int i = 0; i < expenseTypes.size(); i++) {
            String type = expenseTypes.get(i);
            double amt = expenseAmounts.get(i);
            categoryTotals.put(type, categoryTotals.getOrDefault(type, 0.0) + amt);
        }
        int cNo = 0;
        for (Map.Entry<String, Double> e : categoryTotals.entrySet()) {
            double percent = (totalExpense > 0) ? (e.getValue() * 100.0 / totalExpense) : 0.0;
            categoryBox.addView(getRowTextView(
                e.getKey(),
                "₹" + String.format("%.2f", e.getValue()) + " (" + String.format("%.2f", percent) + "%)"
            ));
            if (cNo++ < categoryTotals.size() - 1) categoryBox.addView(getDivider());
        }
        reportRootLayout.addView(categoryBox);

        // Section 3: Totals and Balance
        LinearLayout totalsBox = getCurvedBox();
        totalsBox.addView(getRowTextView("Total Expenses", "₹" + String.format("%.2f", totalExpense)));
        totalsBox.addView(getDivider());
        totalsBox.addView(getRowTextView("Total Contributions", "₹" + String.format("%.2f", totalContribution)));
        totalsBox.addView(getDivider());
        String balanceLabel, balanceValue;
        int balanceColor = Color.BLACK;
        if (balance > 0) {
            balanceLabel = "Balance (Extra Money Left)";
            balanceValue = "₹" + String.format("%.2f", balance);
        } else if (balance < 0) {
            balanceLabel = "Balance (More Money Needed)";
            balanceValue = "-₹" + String.format("%.2f", Math.abs(balance));
            balanceColor = Color.RED;
        } else {
            balanceLabel = "Balance";
            balanceValue = "Settled (0)";
            balanceColor = Color.parseColor("#117c00");
        }
        totalsBox.addView(getRowTextView(balanceLabel, balanceValue, balanceColor));
        reportRootLayout.addView(totalsBox);
    }

    // Helper for big curved white+orange border box with margin
    private LinearLayout getCurvedBox() {
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.curved_box_white_with_orange_border);
        box.setPadding(32, 22, 32, 22);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(32, 0, 32, 20); // 32dp side margin for same look as Summary tab
        box.setLayoutParams(params);
        return box;
    }

    // Section or card title
    private TextView getHeaderTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(getResources().getColor(R.color.primary_orange));
        tv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tv.setTextSize(18);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 14);
        tv.setLayoutParams(params);
        return tv;
    }

    // Expense/summary row
    private LinearLayout getRowTextView(String left, String right) {
        return getRowTextView(left, right, Color.BLACK);
    }
    private LinearLayout getRowTextView(String left, String right, int rightColor) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView leftTv = new TextView(getContext());
        leftTv.setText(left);
        leftTv.setTextColor(Color.BLACK);
        leftTv.setTextSize(16);
        leftTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        leftTv.setLayoutParams(leftParams);

        TextView rightTv = new TextView(getContext());
        rightTv.setText(right);
        rightTv.setTextColor(rightColor);
        rightTv.setTextSize(16);
        rightTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        row.addView(leftTv);
        row.addView(rightTv);
        return row;
    }

    // Divider line
    private View getDivider() {
        View divider = new View(getContext());
        divider.setBackgroundColor(getResources().getColor(R.color.divider));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, 10, 0, 10);
        divider.setLayoutParams(params);
        return divider;
    }

    private double sum(Map<String, Double> map) {
        double s = 0.0;
        for (double v : map.values()) {
            s += v;
        }
        return s;
    }
    private double sum(List<Double> list) {
        double s = 0.0;
        for (double v : list) s += v;
        return s;
    }
}
