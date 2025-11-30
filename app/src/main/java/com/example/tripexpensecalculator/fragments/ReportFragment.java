package com.example.tripexpensecalculator.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportFragment extends Fragment {

    private LinearLayout reportRootLayout;
    private Button btnResetAllData;
    private Typeface loraBoldTypeface;

    // fixed password
    private static final String RESET_PASSWORD = "1234";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report, container, false);
        reportRootLayout = root.findViewById(R.id.reportRootLayout);

        // Load your custom Lora_Bold font from assets
        loraBoldTypeface = Typeface.createFromAsset(requireContext().getAssets(), "fonts/Lora_Bold.ttf");

        btnResetAllData = new Button(getContext());
        btnResetAllData.setText("RESET ALL DATA");
        btnResetAllData.setAllCaps(true);
        btnResetAllData.setTextColor(getResources().getColor(android.R.color.white));
        btnResetAllData.setTextSize(18);
        btnResetAllData.setTypeface(Typeface.DEFAULT_BOLD);
        btnResetAllData.setBackgroundResource(R.drawable.curved_orange_button);
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        resetParams.setMargins(32, 12, 32, 32);
        btnResetAllData.setLayoutParams(resetParams);
        btnResetAllData.setGravity(Gravity.CENTER);
        btnResetAllData.setPadding(0, 18, 0, 18);
        btnResetAllData.setOnClickListener(v -> showResetWarning());

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
                    "₹" + String.format("%.2f", expenseAmounts.get(i)),
                    Color.BLACK, false
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
                    "₹" + String.format("%.2f", e.getValue()) + " (" + String.format("%.2f", percent) + "%)",
                    Color.BLACK, false
            ));
            if (cNo++ < categoryTotals.size() - 1) categoryBox.addView(getDivider());
        }
        reportRootLayout.addView(categoryBox);

        // Totals white curved box
        LinearLayout whiteBox = new LinearLayout(getContext());
        whiteBox.setOrientation(LinearLayout.VERTICAL);
        whiteBox.setBackgroundResource(R.drawable.curved_gray_button);
        whiteBox.setPadding(32, 22, 32, 22);
        LinearLayout.LayoutParams whiteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        whiteParams.setMargins(32, 0, 32, 20);
        whiteBox.setLayoutParams(whiteParams);

        int purple = getResources().getColor(R.color.purple_500);

        whiteBox.addView(getLoraRowTextView("Total Expenses", "₹" + String.format("%.2f", totalExpense), purple));
        whiteBox.addView(getDivider());
        whiteBox.addView(getLoraRowTextView("Total Contributions", "₹" + String.format("%.2f", totalContribution), purple));
        whiteBox.addView(getDivider());
        String balanceLabel;
        String balanceValue;
        if (balance > 0) {
            balanceLabel = "Balance (Extra Money Left)";
            balanceValue = "₹" + String.format("%.2f", balance);
        } else if (balance < 0) {
            balanceLabel = "Balance (More Money Needed)";
            balanceValue = "-₹" + String.format("%.2f", Math.abs(balance));
        } else {
            balanceLabel = "Balance";
            balanceValue = "Settled (0)";
        }
        whiteBox.addView(getLoraRowTextView(balanceLabel, balanceValue, purple));
        reportRootLayout.addView(whiteBox);

        // Reset button at the end
        reportRootLayout.addView(btnResetAllData);
    }

    // Step 1: warning dialog
    private void showResetWarning() {
        new AlertDialog.Builder(getContext())
                .setTitle("Warning")
                .setMessage("Are you sure? This will erase all data.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", (dialog, which) -> showPasswordDialog())
                .show();
    }

    // Step 2: password dialog
    private void showPasswordDialog() {
        final EditText input = new EditText(getContext());
        input.setHint("Enter password");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                           android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        LinearLayout container = new LinearLayout(getContext());
        container.setPadding(40, 20, 40, 0);
        container.addView(input);

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm Reset")
                .setMessage("Please enter password to reset all data.")
                .setView(container)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (dialog, which) -> {
                    String pwd = input.getText().toString().trim();
                    if (RESET_PASSWORD.equals(pwd)) {
                        clearAllData();
                    } else {
                        Toast.makeText(getContext(), "Incorrect password.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void clearAllData() {
        FriendsFragment.getContributions().clear();
        ExpenseFragment.getExpenseTypes().clear();
        ExpenseFragment.getExpenseAmounts().clear();

        Context ctx = requireContext();
        ctx.getSharedPreferences("TripExpensePrefs", Context.MODE_PRIVATE).edit().clear().apply();

        if (ExpenseFragment.instance != null) ExpenseFragment.instance.safeRefreshExpensesUI();
        if (FriendsFragment.instance != null) FriendsFragment.instance.safeRefreshUI();

        displayDetailedReport();
        Toast.makeText(ctx, "All data has been reset.", Toast.LENGTH_SHORT).show();
    }

    private LinearLayout getCurvedBox() {
        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.curved_box_white_with_gray_border);
        box.setPadding(32, 22, 32, 22);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(32, 0, 32, 20);
        box.setLayoutParams(params);
        return box;
    }

    private TextView getHeaderTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(getResources().getColor(R.color.purple_500));
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextSize(18);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 14);
        tv.setLayoutParams(params);
        return tv;
    }

    private LinearLayout getRowTextView(String left, String right, int color, boolean useBold) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView leftTv = new TextView(getContext());
        leftTv.setText(left);
        leftTv.setTextColor(color);
        leftTv.setTextSize(16);
        leftTv.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        leftTv.setLayoutParams(leftParams);

        TextView rightTv = new TextView(getContext());
        rightTv.setText(right);
        rightTv.setTextColor(color);
        rightTv.setTextSize(16);
        rightTv.setTypeface(Typeface.DEFAULT_BOLD);

        row.addView(leftTv);
        row.addView(rightTv);
        return row;
    }

    private LinearLayout getLoraRowTextView(String left, String right, int color) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView leftTv = new TextView(getContext());
        leftTv.setText(left);
        leftTv.setTextColor(color);
        leftTv.setTextSize(18);
        leftTv.setTypeface(loraBoldTypeface);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        leftTv.setLayoutParams(leftParams);

        TextView rightTv = new TextView(getContext());
        rightTv.setText(right);
        rightTv.setTextColor(color);
        rightTv.setTextSize(18);
        rightTv.setTypeface(loraBoldTypeface);

        row.addView(leftTv);
        row.addView(rightTv);
        return row;
    }

    private View getDivider() {
        return getDivider(getResources().getColor(R.color.divider));
    }

    private View getDivider(int color) {
        View divider = new View(getContext());
        divider.setBackgroundColor(color);
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
