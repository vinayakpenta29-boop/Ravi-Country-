package com.example.tripexpensecalculator.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.tripexpensecalculator.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SummaryFragment extends Fragment {

    private LinearLayout summaryRootLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);
        summaryRootLayout = root.findViewById(R.id.summaryRootLayout);
        displaySummary();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        displaySummary();
    }

    private void displaySummary() {
        summaryRootLayout.removeAllViews();

        Map<String, Double> contributions = FriendsFragment.getContributions();
        double totalContribution = sum(contributions);
        double totalExpense = sum(ExpenseFragment.getExpenseAmounts());
        int people = contributions.size();
        double perPerson = (people > 0) ? (totalExpense / people) : 0.0;
        double overallBalance = totalContribution - totalExpense;

        addSimpleText("Total Contributions: ₹" + String.format("%.2f", totalContribution), Color.BLACK);
        addSimpleText("Total Expenses:      ₹" + String.format("%.2f", totalExpense), Color.BLACK);
        addSimpleText("Each Person's Share: ₹" + String.format("%.2f", perPerson), Color.BLACK);
        addSimpleText("", Color.BLACK);

        List<String> negativeMembers = new ArrayList<>();
        List<Double> negativeBalances = new ArrayList<>();

        for (Map.Entry<String, Double> entry : contributions.entrySet()) {
            double bal = entry.getValue() - perPerson;
            String sign = (bal >= 0) ? "+" : "-";
            String balanceText = entry.getKey() + " paid ₹" + String.format("%.2f", entry.getValue())
                    + " | Balance: " + sign + "₹" + String.format("%.2f", Math.abs(bal));
            int color = (bal >= 0) ? Color.parseColor("#117c00") : Color.RED;
            addSimpleText(balanceText, color);

            if (bal < 0) {
                negativeMembers.add(entry.getKey());
                negativeBalances.add(bal);
            }
        }

        addSimpleText("", Color.BLACK);

        // Show overall balance line with red amount if negative only
        String text = "Overall Balance: Extra money left: ";
        SpannableStringBuilder spanBuilder = new SpannableStringBuilder(text);
        String amountText;
        if (overallBalance >= 0) {
            amountText = "₹" + String.format("%.2f", overallBalance);
            spanBuilder.append(amountText);
        } else {
            amountText = "-₹" + String.format("%.2f", Math.abs(overallBalance));
            spanBuilder.append(amountText);
            spanBuilder.setSpan(new ForegroundColorSpan(Color.RED),
                    text.length(), text.length() + amountText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        TextView overallBalanceLine = new TextView(getContext());
        overallBalanceLine.setText(spanBuilder);
        overallBalanceLine.setTextSize(16);
        summaryRootLayout.addView(overallBalanceLine);

        if (!negativeMembers.isEmpty()) {
            addSimpleText("", Color.BLACK);
            addSimpleText("Take Cash from this Members who has a Negative Balance.", Color.BLUE);
            for (int i = 0; i < negativeMembers.size(); i++) {
                String name = negativeMembers.get(i);
                double bal = negativeBalances.get(i);
                addSimpleText("• " + name + ": ₹" + String.format("%.2f", Math.abs(bal)), Color.RED);
            }
        }
    }

    private void addSimpleText(String text, int color) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(16);
        tv.setPadding(0, 2, 0, 2);
        summaryRootLayout.addView(tv);
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
