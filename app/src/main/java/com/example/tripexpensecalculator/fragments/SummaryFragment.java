package com.example.tripexpensecalculator.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;

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

    // ---- Friends ----
    Map<String, FriendsFragment.FriendTotals> rawContributions = FriendsFragment.getContributions();
    Map<String, Double> contributions = new LinkedHashMap<>();

    double totalCashGiven = 0.0;
    double totalOnlineGiven = 0.0;

    for (Map.Entry<String, FriendsFragment.FriendTotals> e : rawContributions.entrySet()) {
        double total = e.getValue().cash + e.getValue().online;
        contributions.put(e.getKey(), total);
        totalCashGiven += e.getValue().cash;
        totalOnlineGiven += e.getValue().online;
    }

    // ---- Expenses ----
    List<Double> expenseAmounts = ExpenseFragment.getExpenseAmounts();
    List<Boolean> expenseIsOnline = ExpenseFragment.getExpenseIsOnline();

    double totalExpense = 0.0;
    double totalCashExpenses = 0.0;
    double totalOnlineExpenses = 0.0;

    for (int i = 0; i < expenseAmounts.size(); i++) {
        double amt = expenseAmounts.get(i);
        totalExpense += amt;

        boolean isOnline = (i < expenseIsOnline.size()) && Boolean.TRUE.equals(expenseIsOnline.get(i));

        if (isOnline) totalOnlineExpenses += amt;
        else totalCashExpenses += amt;
    }

    int people = contributions.size();
    double perPerson = (people > 0) ? totalExpense / people : 0.0;
    double overallBalance = totalCashGiven + totalOnlineGiven - totalExpense;

    // ---- Main Box ----
    LinearLayout mainBox = getCurvedBox();
    mainBox.addView(getLoraRow("Total Contributions", "₹" + String.format("%.2f", totalCashGiven + totalOnlineGiven), Color.BLACK));
    mainBox.addView(getDivider());
    mainBox.addView(getLoraRow("Total Expenses", "₹" + String.format("%.2f", totalExpense), Color.BLACK));
    mainBox.addView(getDivider());
    mainBox.addView(getLoraRow("Total Friends", String.valueOf(people), Color.BLACK));
    mainBox.addView(getDivider());
    mainBox.addView(getLoraRow("Each Person Share", "₹" + String.format("%.2f", perPerson), Color.BLACK));
    summaryRootLayout.addView(mainBox);

    // ---- Cash / Online ----
    LinearLayout cashOnlineBox = getCurvedBox();
    cashOnlineBox.addView(buildBalanceLine(R.mipmap.ic_cash, "Cash", totalCashGiven, totalCashGiven - totalCashExpenses));
    cashOnlineBox.addView(getDivider());
    cashOnlineBox.addView(buildBalanceLine(R.mipmap.ic_online, "Online", totalOnlineGiven, totalOnlineGiven - totalOnlineExpenses));
    summaryRootLayout.addView(cashOnlineBox);

    // ---- Expense Share Calculation ----
    Map<String, Double> expenseShareMap = new LinkedHashMap<>();
    for (String name : contributions.keySet()) {
        expenseShareMap.put(name, 0.0);
    }

    for (int i = 0; i < expenseAmounts.size(); i++) {
        double amount = expenseAmounts.get(i);

        List<List<String>> splits = ExpenseFragment.getExpenseSplitBetween();
        List<String> splitList;

        if (i < splits.size() && splits.get(i) != null && !splits.get(i).isEmpty()) {
            splitList = splits.get(i);
        } else {
            splitList = new ArrayList<>(contributions.keySet());
        }

        double perHead = amount / splitList.size();

        for (String person : splitList) {
            expenseShareMap.put(person, expenseShareMap.get(person) + perHead);
        }
    }

    // ---- Final Balances ----
    Map<String, Double> finalBalances = new LinkedHashMap<>();
    for (String name : contributions.keySet()) {
        double paid = contributions.get(name);
        double expense = expenseShareMap.get(name);
        finalBalances.put(name, paid - expense);
    }

    // ---- TABLE UI ----
    LinearLayout balanceBox = getCurvedBox();

    LinearLayout header = new LinearLayout(getContext());
    header.setOrientation(LinearLayout.HORIZONTAL);
    header.addView(createCell("Name", true));
    header.addView(createCell("Paid", true));
    header.addView(createCell("Expense", true));
    header.addView(createCell("Balance", true));

    balanceBox.addView(header);
    balanceBox.addView(getDivider());

    List<String> negativeMembers = new ArrayList<>();
    List<Double> negativeBalances = new ArrayList<>();

    for (String name : contributions.keySet()) {

        double paid = contributions.get(name);
        double expense = expenseShareMap.get(name);
        double balance = paid - expense;

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(createCell(name, false));
        row.addView(createCell("₹" + String.format("%.2f", paid), false));
        row.addView(createCell("₹" + String.format("%.2f", expense), false));

        String balText = (balance >= 0 ? "+₹" : "-₹") + String.format("%.2f", Math.abs(balance));
        TextView balCell = createCell(balText, false);
        balCell.setTextColor(balance >= 0 ? Color.parseColor("#117c00") : Color.RED);

        balCell.setOnClickListener(v -> {
            showMemberExpensePopup(name);
        });

        row.addView(balCell);

        balanceBox.addView(row);
        balanceBox.addView(getDivider());

        if (balance < 0) {
            negativeMembers.add(name);
            negativeBalances.add(balance);
        }
    }

    summaryRootLayout.addView(balanceBox);

    // ---- Settlement ----
    List<Map.Entry<String, Double>> positive = new ArrayList<>();
    List<Map.Entry<String, Double>> negative = new ArrayList<>();

    for (Map.Entry<String, Double> e : finalBalances.entrySet()) {
        if (e.getValue() > 0) positive.add(e);
        else if (e.getValue() < 0) negative.add(e);
    }

    if (!positive.isEmpty() && !negative.isEmpty()) {

        LinearLayout settlementBox = getCurvedBox();

        int i = 0, j = 0;

        while (i < negative.size() && j < positive.size()) {

            String debtor = negative.get(i).getKey();
            double debt = Math.abs(negative.get(i).getValue());

            String creditor = positive.get(j).getKey();
            double credit = positive.get(j).getValue();

            double amount = Math.min(debt, credit);

            settlementBox.addView(getRow(debtor + " → " + creditor,
                    "₹" + String.format("%.2f", amount), Color.RED));
            settlementBox.addView(getDivider());

            debt -= amount;
            credit -= amount;

            negative.set(i, new java.util.AbstractMap.SimpleEntry<>(debtor, -debt));
            positive.set(j, new java.util.AbstractMap.SimpleEntry<>(creditor, credit));

            if (debt == 0) i++;
            if (credit == 0) j++;
        }

        summaryRootLayout.addView(settlementBox);
    }

    // ---- Orange Box ----
    LinearLayout orangeBox = new LinearLayout(getContext());
    orangeBox.setOrientation(LinearLayout.VERTICAL);
    orangeBox.setBackgroundResource(R.drawable.curved_orange_button);
    orangeBox.setPadding(32, 21, 32, 21);
    orangeBox.setGravity(android.view.Gravity.CENTER);

    TextView labelTv = new TextView(getContext());
    labelTv.setText("Extra Money Left:");
    labelTv.setTextColor(Color.WHITE);
    labelTv.setTextSize(20);
    labelTv.setTypeface(Typeface.DEFAULT_BOLD);
    labelTv.setGravity(android.view.Gravity.CENTER);

    TextView amtTv = new TextView(getContext());
    amtTv.setText("₹" + String.format("%.2f", overallBalance));
    amtTv.setTextColor(Color.WHITE);
    amtTv.setTextSize(32);
    amtTv.setTypeface(Typeface.DEFAULT_BOLD);
    amtTv.setGravity(android.view.Gravity.CENTER);

    orangeBox.addView(labelTv);
    orangeBox.addView(amtTv);

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

    // ----- helper for cash/online chip row -----
    private View buildBalanceLine(int iconResId,
                                  String label,
                                  double totalGiven,
                                  double balance) {

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(rowLp);

        // icon - 32dp
        android.widget.ImageView icon = new android.widget.ImageView(getContext());
        icon.setImageResource(iconResId);
        int sizePx = (int) (32 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(sizePx, sizePx);
        iconLp.setMargins(0, 0, 16, 0);
        icon.setLayoutParams(iconLp);
        row.addView(icon);

        // green chip: "Cash : 5000"
        TextView totalTv = new TextView(getContext());
        totalTv.setText(label + " : " + String.format("%.0f", totalGiven));
        totalTv.setTextColor(Color.WHITE);
        totalTv.setTextSize(14);
        totalTv.setTypeface(loraBoldTypeface);
        totalTv.setGravity(android.view.Gravity.CENTER);
        totalTv.setBackgroundResource(R.drawable.curved_green_chip);
        totalTv.setPadding(22, 10, 22, 10);
        LinearLayout.LayoutParams totalLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        totalLp.setMargins(0, 0, 12, 0);
        row.addView(totalTv, totalLp);

        // spacer to push balance section to right
        View spacer = new View(getContext());
        LinearLayout.LayoutParams spacerLp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(spacer, spacerLp);

        // container for "Balance :  ₹xxx" aligned right
        LinearLayout rightBox = new LinearLayout(getContext());
        rightBox.setOrientation(LinearLayout.HORIZONTAL);
        rightBox.setGravity(android.view.Gravity.END | android.view.Gravity.CENTER_VERTICAL);

        TextView labelTv = new TextView(getContext());
        labelTv.setText("Balance : ");
        labelTv.setTextColor(Color.BLACK);
        labelTv.setTextSize(14);
        labelTv.setTypeface(loraBoldTypeface);
        rightBox.addView(labelTv);

        TextView balTv = new TextView(getContext());
        String balStr = "₹" + String.format("%.0f", Math.abs(balance));
        if (balance < 0) balStr = "₹-" + String.format("%.0f", Math.abs(balance));
        balTv.setText(balStr);
        balTv.setTextColor(Color.WHITE);
        balTv.setTextSize(14);
        balTv.setTypeface(loraBoldTypeface);
        balTv.setGravity(android.view.Gravity.CENTER);
        balTv.setBackgroundResource(R.drawable.curved_pink_chip);
        balTv.setPadding(22, 10, 22, 10);
        rightBox.addView(balTv);

        row.addView(rightBox);

        return row;
    }

    private TextView createCell(String text, boolean isHeader) {
    TextView tv = new TextView(getContext());
    tv.setText(text);
    tv.setTextSize(14);
    tv.setPadding(8, 8, 8, 8);
    tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

    if (isHeader) {
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(Color.BLACK);
    } else {
        tv.setTextColor(Color.BLACK);
    }

    return tv;
}

    private TextView createTableCell(String text, boolean isHeader) {
    TextView tv = new TextView(getContext());
    tv.setText(text);
    tv.setPadding(16, 12, 16, 12);
    tv.setTextSize(14);
    tv.setGravity(android.view.Gravity.CENTER);

    LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
    params.setMargins(2, 2, 2, 2);
    tv.setLayoutParams(params);

    if (isHeader) {
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
    } else {
        tv.setTextColor(Color.BLACK);
    }

    tv.setBackgroundResource(R.drawable.bg_popup_card);

    return tv;
}
    
    
    private void showMemberExpensePopup(String memberName) {

    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

    LinearLayout mainLayout = new LinearLayout(getContext());
    mainLayout.setOrientation(LinearLayout.VERTICAL);
    mainLayout.setPadding(30, 30, 30, 30);

    // 🔹 Title (Name Centered)
    TextView title = new TextView(getContext());
    title.setText(memberName);
    title.setTextSize(20);
    title.setTypeface(loraBoldTypeface);
    title.setGravity(android.view.Gravity.CENTER);
    title.setTextColor(Color.parseColor("#990F4B"));
    title.setPadding(0, 0, 0, 20);

    mainLayout.addView(title);

    // 🔹 Table Layout
    LinearLayout table = new LinearLayout(getContext());
    table.setOrientation(LinearLayout.VERTICAL);
    table.setBackgroundColor(Color.LTGRAY);

    // 🔹 HEADER ROW
    LinearLayout header = new LinearLayout(getContext());
    header.setOrientation(LinearLayout.HORIZONTAL);
    header.setBackgroundColor(Color.parseColor("#c01587"));
    

    header.addView(createTableCell("Category", true));
    header.addView(createTableCell("Split Share", true));
    header.addView(createTableCell("Expense", true));
    header.addView(createTableCell("Paid by", true));

    table.addView(header);

    // 🔹 DATA
    List<String> types = ExpenseFragment.getExpenseTypes();
    List<Double> amounts = ExpenseFragment.getExpenseAmounts();
    List<String> paidByList = ExpenseFragment.getExpensePaidBy();
    List<List<String>> splits = ExpenseFragment.getExpenseSplitBetween();

    boolean hasExpense = false;

    for (int i = 0; i < types.size(); i++) {

        List<String> splitList;

        if (i < splits.size() && splits.get(i) != null && !splits.get(i).isEmpty()) {
            splitList = splits.get(i);
        } else {
            splitList = new ArrayList<>(FriendsFragment.getContributions().keySet());
        }

        if (splitList.contains(memberName)) {

            hasExpense = true;

            double total = amounts.get(i);
            double share = total / splitList.size();
            String paidBy = (i < paidByList.size()) ? paidByList.get(i) : "Unknown";

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBackgroundColor(Color.WHITE);

            row.addView(createTableCell(types.get(i), false));
            row.addView(createTableCell("₹" + String.format("%.0f", share), false));
            row.addView(createTableCell("₹" + String.format("%.0f", total), false));
            row.addView(createTableCell(paidBy, false));

            table.addView(row);
        }
    }

    if (!hasExpense) {
        TextView empty = new TextView(getContext());
        empty.setText("No expenses found");
        empty.setGravity(android.view.Gravity.CENTER);
        empty.setPadding(20, 20, 20, 20);
        mainLayout.addView(empty);
    } else {
        mainLayout.addView(table);
    }

    // 🔥 Scroll Support
    android.widget.ScrollView scrollView = new android.widget.ScrollView(getContext());
    scrollView.addView(mainLayout);

    builder.setView(scrollView);
    builder.setPositiveButton("OK", null);
    builder.show();
}
}
