package com.example.tripexpensecalculator.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SummaryFragment extends Fragment {

    private LinearLayout summaryRootLayout;
    private Typeface loraBoldTypeface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);
        summaryRootLayout = root.findViewById(R.id.summaryRootLayout);

        // Load Lora_Bold.ttf from assets
        loraBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Lora_Bold.ttf");

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

        // FriendsFragment now returns Map<String, List<Double>>; convert to totals
        Map<String, List<Double>> rawContributions = FriendsFragment.getContributions();
        Map<String, Double> contributions = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> e : rawContributions.entrySet()) {
            double total = 0.0;
            for (double v : e.getValue()) total += v;
            contributions.put(e.getKey(), total);
        }

        double totalContribution = sum(contributions);
        double totalExpense = sum(ExpenseFragment.getExpenseAmounts());
        int people = contributions.size();
        double perPerson = (people > 0) ? (totalExpense / people) : 0.0;
        double overallBalance = totalContribution - totalExpense;

        // ---- Main Summary Box (All text in Lora_Bold) ----
        LinearLayout mainBox = getCurvedBox();
        mainBox.addView(getLoraRow("Total Contributions", "₹" + String.format("%.2f", totalContribution), Color.BLACK));
        mainBox.addView(getDivider());
        mainBox.addView(getLoraRow("Total Expenses",     "₹" + String.format("%.2f", totalExpense), Color.BLACK));
        mainBox.addView(getDivider());
        mainBox.addView(getLoraRow("Total Friends", String.valueOf(people), Color.BLACK));
        mainBox.addView(getDivider());
        mainBox.addView(getLoraRow("Each Person Share",  "₹" + String.format("%.2f", perPerson), Color.BLACK));
        summaryRootLayout.addView(mainBox);

        // ---- Friends Balance Box ----
        LinearLayout balanceBox = getCurvedBox();
        List<String> negativeMembers = new ArrayList<>();
        List<Double> negativeBalances = new ArrayList<>();
        int fNo = 0;
        for (Map.Entry<String, Double> entry : contributions.entrySet()) {
            double bal = entry.getValue() - perPerson;
            String sign = (bal >= 0) ? "+" : "-";
            int color = (bal >= 0) ? Color.parseColor("#117c00") : Color.RED;
            String balanceLabel = entry.getKey() + " Paid";
            String balanceValue = "₹" + String.format("%.2f", entry.getValue()) + "  |  " +
                    "Balance: " + sign + "₹" + String.format("%.2f", Math.abs(bal));
            balanceBox.addView(getRow(balanceLabel, balanceValue, color));
            if (fNo++ < contributions.size() - 1) balanceBox.addView(getDivider());
            if (bal < 0) {
                negativeMembers.add(entry.getKey());
                negativeBalances.add(bal);
            }
        }
        summaryRootLayout.addView(balanceBox);

        // ---- Negative Balance Box ----
        if (!negativeMembers.isEmpty()) {
            LinearLayout negativeBox = getCurvedBox();

            // Title sub-box
            LinearLayout subBox = new LinearLayout(getContext());
            subBox.setOrientation(LinearLayout.HORIZONTAL);
            subBox.setBackgroundResource(R.drawable.curved_box_gray_with_border);
            subBox.setPadding(25, 10, 25, 10);
            LinearLayout.LayoutParams subBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int marginBottomPx = (int) (getResources().getDisplayMetrics().density * 12);
            subBoxParams.setMargins(0, 0, 0, marginBottomPx);
            subBox.setLayoutParams(subBoxParams);

            TextView titleTv = new TextView(getContext());
            titleTv.setText("Take The Balance Amount in This Friends This has a Nagative(-) Balance:");
            titleTv.setTextColor(Color.WHITE);
            titleTv.setTextSize(18);
            titleTv.setTypeface(loraBoldTypeface);
            titleTv.setGravity(android.view.Gravity.CENTER);
            titleTv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            subBox.addView(titleTv);

            negativeBox.addView(subBox);

            // Negative friends list
            for (int i = 0; i < negativeMembers.size(); i++) {
                String label = negativeMembers.get(i);
                String val = "-₹" + String.format("%.2f", Math.abs(negativeBalances.get(i)));
                negativeBox.addView(getRow(label, val, Color.RED));
                if (i < negativeMembers.size() - 1) negativeBox.addView(getDivider());
            }
            summaryRootLayout.addView(negativeBox);
        }

        // ---- Overall Balance (Big Orange Box) ----
        LinearLayout orangeBox = new LinearLayout(getContext());
        orangeBox.setOrientation(LinearLayout.VERTICAL);
        orangeBox.setBackgroundResource(R.drawable.curved_orange_button);
        orangeBox.setPadding(32, 21, 32, 21);
        orangeBox.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams orangeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        orangeParams.setMargins(24, 18, 24, 18);
        orangeBox.setLayoutParams(orangeParams);

        TextView labelTv = new TextView(getContext());
        labelTv.setText("Extra Money Left:");
        labelTv.setTextColor(Color.WHITE);
        labelTv.setTextSize(20);
        labelTv.setTypeface(Typeface.DEFAULT_BOLD);
        labelTv.setGravity(android.view.Gravity.CENTER);

        TextView amtTv = new TextView(getContext());
        String amtStr = "₹" + String.format("%.2f", Math.abs(overallBalance));
        if (overallBalance < 0) amtStr = "₹-" + String.format("%.2f", Math.abs(overallBalance));
        amtTv.setText(amtStr);
        amtTv.setTextColor(Color.WHITE);
        amtTv.setTextSize(32);
        amtTv.setTypeface(Typeface.DEFAULT_BOLD);
        amtTv.setGravity(android.view.Gravity.CENTER);

        orangeBox.addView(labelTv);
        orangeBox.addView(amtTv);

        if (overallBalance < 0) {
            TextView warningTv = new TextView(getContext());
            warningTv.setText("(You Need More Money)");
            warningTv.setTextColor(Color.WHITE);
            warningTv.setTextSize(16);
            warningTv.setGravity(android.view.Gravity.CENTER);
            warningTv.setTypeface(Typeface.DEFAULT_BOLD);
            orangeBox.addView(warningTv);
        }

        summaryRootLayout.addView(orangeBox);
    }

    private LinearLayout getCurvedBox() {
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.curved_box_white_with_gray_border);
        box.setPadding(42, 28, 42, 28);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 0, 10, 24);
        box.setLayoutParams(params);
        return box;
    }

    private LinearLayout getRow(String label, String value, int valueColor) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView labelTv = new TextView(getContext());
        labelTv.setText(label);
        labelTv.setTextColor(Color.BLACK);
        labelTv.setTextSize(16);
        labelTv.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        labelTv.setLayoutParams(labelParams);

        TextView valueTv = new TextView(getContext());
        valueTv.setText(value);
        valueTv.setTextColor(valueColor);
        valueTv.setTextSize(16);
        valueTv.setTypeface(Typeface.DEFAULT_BOLD);

        row.addView(labelTv);
        row.addView(valueTv);
        return row;
    }

    private LinearLayout getLoraRow(String label, String value, int valueColor) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView labelTv = new TextView(getContext());
        labelTv.setText(label);
        labelTv.setTextColor(Color.BLACK);
        labelTv.setTextSize(16);
        labelTv.setTypeface(loraBoldTypeface);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        labelTv.setLayoutParams(labelParams);

        TextView valueTv = new TextView(getContext());
        valueTv.setText(value);
        valueTv.setTextColor(valueColor);
        valueTv.setTextSize(16);
        valueTv.setTypeface(loraBoldTypeface);

        row.addView(labelTv);
        row.addView(valueTv);
        return row;
    }

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
