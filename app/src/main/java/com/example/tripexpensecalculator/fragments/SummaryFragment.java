package com.example.tripexpensecalculator.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import java.util.Map;

public class SummaryFragment extends Fragment {

    private TextView summaryTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);

        summaryTextView = root.findViewById(R.id.summaryTextView);

        displaySummary();

        return root;
    }

    private void displaySummary() {
        Map<String, Double> contributions = FriendsFragment.getContributions();
        double totalContribution = sum(contributions);
        double totalExpense = sum(ExpenseFragment.getExpenseAmounts());
        int people = contributions.size();
        double perPerson = (people > 0) ? (totalExpense / people) : 0.0;
        double overallBalance = totalContribution - totalExpense;

        StringBuilder sb = new StringBuilder();
        sb.append("Total Contributions: ₹").append(String.format("%.2f", totalContribution)).append("\n");
        sb.append("Total Expenses:      ₹").append(String.format("%.2f", totalExpense)).append("\n");
        sb.append("Each Person's Share: ₹").append(String.format("%.2f", perPerson)).append("\n\n");

        for (Map.Entry<String, Double> entry : contributions.entrySet()) {
            double bal = entry.getValue() - perPerson;
            String sign = (bal >= 0) ? "+" : "-";
            sb.append(entry.getKey()).append(" paid ₹").append(String.format("%.2f", entry.getValue()))
              .append(" | Balance: ").append(sign).append("₹").append(String.format("%.2f", Math.abs(bal))).append("\n");
        }

        sb.append("\nOverall Balance: ");
        if (overallBalance > 0) sb.append("Extra money left: ₹").append(String.format("%.2f", overallBalance));
        else if (overallBalance < 0) sb.append("More money needed: ₹").append(String.format("%.2f", Math.abs(overallBalance)));
        else sb.append("Settled (0)");

        summaryTextView.setText(sb.toString());
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
