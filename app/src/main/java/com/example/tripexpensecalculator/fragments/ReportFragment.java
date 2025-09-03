package com.example.tripexpensecalculator.fragments;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import java.util.*;

public class ReportFragment extends Fragment {

    private TextView reportTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report, container, false);
        reportTextView = root.findViewById(R.id.reportTextView);
        reportTextView.setMovementMethod(new ScrollingMovementMethod());
        displayDetailedReport();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayDetailedReport();
    }

    private void displayDetailedReport() {
        java.util.List<String> expenseTypes = ExpenseFragment.getExpenseTypes();
        java.util.List<Double> expenseAmounts = ExpenseFragment.getExpenseAmounts();
        Map<String, Double> contributions = FriendsFragment.getContributions();

        double totalExpense = sum(expenseAmounts);
        double totalContribution = sum(contributions);
        double balance = totalContribution - totalExpense;

        StringBuilder sb = new StringBuilder();
        sb.append("=== Detailed Expense Report ===\n\n");

        // Transaction Log
        sb.append("--- All Expenses (Transaction Log) ---\n");
        for (int i = 0; i < expenseTypes.size(); i++) {
            sb.append(i + 1).append(". ").append(expenseTypes.get(i))
              .append(" - ₹").append(String.format("%.2f", expenseAmounts.get(i))).append("\n");
        }

        // Category-wise Summary
        sb.append("\n--- Category-wise Summary ---\n");
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        for (int i = 0; i < expenseTypes.size(); i++) {
            String type = expenseTypes.get(i);
            double amt = expenseAmounts.get(i);
            categoryTotals.put(type, categoryTotals.getOrDefault(type, 0.0) + amt);
        }
        for (Map.Entry<String, Double> e : categoryTotals.entrySet()) {
            double percent = (totalExpense > 0) ? (e.getValue() * 100.0 / totalExpense) : 0.0;
            sb.append(e.getKey()).append(": ₹").append(String.format("%.2f", e.getValue()))
              .append(" (").append(String.format("%.2f", percent)).append("%)\n");
        }

        sb.append("\nTotal Expenses:      ₹").append(String.format("%.2f", totalExpense)).append("\n");
        sb.append("Total Contributions: ₹").append(String.format("%.2f", totalContribution)).append("\n");
        if (balance > 0) {
            sb.append("Balance (Extra Money Left): ₹").append(String.format("%.2f", balance)).append("\n");
        } else if (balance < 0) {
            sb.append("Balance (More Money Needed): ₹").append(String.format("%.2f", Math.abs(balance))).append("\n");
        } else {
            sb.append("Balance: Settled (0)\n");
        }

        reportTextView.setText(sb.toString());
    }

    private double sum(Map<String, Double> map) {
        double s = 0.0;
        for (double v : map.values()) {
            s += v;
        }
        return s;
    }

    private double sum(java.util.List<Double> list) {
        double s = 0.0;
        for (double v : list) {
            s += v;
        }
        return s;
    }
}
