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

        // ---- Main Summary Box ----
        LinearLayout mainBox = getCurvedBox();
        mainBox.addView(getRow("Total Contributions", "₹" + String.format("%.2f", totalContribution), Color.WHITE));
        mainBox.addView(getDivider());
        mainBox.addView(getRow("Total Expenses",     "₹" + String.format("%.2f", totalExpense), Color.WHITE));
        mainBox.addView(getDivider());
        mainBox.addView(getRow("Each Person Share",  "₹" + String.format("%.2f", perPerson), Color.WHITE));
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
            String balanceLabel = entry.getKey() + " paid";
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
            negativeBox.addView(getSimpleRow("Take Cash from members with Negative Balance", Color.BLUE));
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
        orangeBox.setOrientation(LinearLayout.HORIZONTAL);
        orangeBox.setBackgroundResource(R.drawable.curved_orange_button);
        orangeBox.setPadding(32, 21, 32, 21);
        orangeBox.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams orangeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        orangeParams.setMargins(0, 18, 0, 18);
        orangeBox.setLayoutParams(orangeParams);
        String text = (overallBalance >= 0) ? "Extra Money Left: ₹" : "Overall Deficit: -₹";
        text += String.format("%.2f", Math.abs(overallBalance));
        TextView ov = new TextView(getContext());
        ov.setText(text);
        ov.setTextColor(getResources().getColor(R.color.input_text));
        ov.setTextSize(18);
        ov.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        ov.setGravity(android.view.Gravity.CENTER);
        orangeBox.addView(ov);
        summaryRootLayout.addView(orangeBox);
    }

    private LinearLayout getCurvedBox() {
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.curved_box_white_with_border);
        box.setPadding(32,22,32,22); // Left,Top,Right,Bottom
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0,0,20);
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
        labelTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        labelTv.setLayoutParams(labelParams);

        TextView valueTv = new TextView(getContext());
        valueTv.setText(value);
        valueTv.setTextColor(Color.BLACK);
        valueTv.setTextSize(16);
        valueTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        row.addView(labelTv);
        row.addView(valueTv);
        return row;
    }

    private LinearLayout getSimpleRow(String text, int color) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView labelTv = new TextView(getContext());
        labelTv.setText(text);
        labelTv.setTextColor(color);
        labelTv.setTextSize(16);
        labelTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        labelTv.setLayoutParams(labelParams);

        row.addView(labelTv);
        return row;
    }

    private View getDivider() {
        View divider = new View(getContext());
        divider.setBackgroundColor(getResources().getColor(R.color.divider));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, 10, 0, 10); // vertical space
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
