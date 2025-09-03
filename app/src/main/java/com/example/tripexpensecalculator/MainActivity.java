package com.example.tripexpensecalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

public class MainActivity extends AppCompatActivity {

    private final Map<String, Double> contributions = new LinkedHashMap<>();
    private final List<String> expenseTypes = new ArrayList<>();
    private final List<Double> expenseAmounts = new ArrayList<>();

    private EditText inputName, inputContribution, inputCategory, inputExpense;
    private TextView outputSummary, outputReport;
    private Button btnAddFriend, btnAddExpense, btnShowSummary, btnShowReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputName = findViewById(R.id.inputName);
        inputContribution = findViewById(R.id.inputContribution);
        inputCategory = findViewById(R.id.inputCategory);
        inputExpense = findViewById(R.id.inputExpense);
        outputSummary = findViewById(R.id.outputSummary);
        outputReport = findViewById(R.id.outputReport);

        btnAddFriend = findViewById(R.id.btnAddFriend);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnShowSummary = findViewById(R.id.btnShowSummary);
        btnShowReport = findViewById(R.id.btnShowReport);

        btnAddFriend.setOnClickListener(v -> addFriend());
        btnAddExpense.setOnClickListener(v -> addExpense());
        btnShowSummary.setOnClickListener(v -> showSummary());
        btnShowReport.setOnClickListener(v -> showDetailedReport());
    }

    private void addFriend() {
        String name = inputName.getText().toString().trim();
        if (name.isEmpty()) {
            showToast("Name cannot be empty.");
            return;
        }
        String amountStr = inputContribution.getText().toString().trim();
        if (amountStr.isEmpty()) {
            showToast("Contribution amount cannot be empty.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showToast("Invalid contribution amount.");
            return;
        }

        contributions.put(name, contributions.getOrDefault(name, 0.0) + amount);
        showToast("Saved! " + name + " total contribution: ₹" + format(contributions.get(name)));

        inputName.setText("");
        inputContribution.setText("");
        outputSummary.setText("");
        outputReport.setText("");
    }

    private void addExpense() {
        String category = inputCategory.getText().toString().trim();
        if (category.isEmpty()) {
            showToast("Expense category cannot be empty.");
            return;
        }
        String amountStr = inputExpense.getText().toString().trim();
        if (amountStr.isEmpty()) {
            showToast("Expense amount cannot be empty.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showToast("Invalid expense amount.");
            return;
        }

        expenseTypes.add(category);
        expenseAmounts.add(amount);
        showToast("Expense added: " + category + " - ₹" + format(amount));

        inputCategory.setText("");
        inputExpense.setText("");
        outputSummary.setText("");
        outputReport.setText("");
    }

    private void showSummary() {
        if (contributions.isEmpty() && expenseAmounts.isEmpty()) {
            outputSummary.setText("No data available yet.");
            return;
        }

        double totalExpense = sum(expenseAmounts);
        double totalContribution = sum(new ArrayList<>(contributions.values()));
        int people = contributions.size();
        double perPerson = (people > 0) ? (totalExpense / people) : 0.0;

        StringBuilder summary = new StringBuilder();
        summary.append("Total Contributions: ₹").append(format(totalContribution)).append("\n");
        summary.append("Total Expenses:      ₹").append(format(totalExpense)).append("\n");
        summary.append("Each person's share: ₹").append(format(perPerson)).append("\n\n");

        for (Map.Entry<String, Double> entry : contributions.entrySet()) {
            String name = entry.getKey();
            double paid = entry.getValue();
            double bal = paid - perPerson;
            String sign = (bal >= 0 ? "+" : "-");
            summary.append(name).append(" paid ₹").append(format(paid))
                    .append(" | Balance: ").append(sign).append("₹").append(format(Math.abs(bal))).append("\n");
        }

        double overallBalance = totalContribution - totalExpense;
        summary.append("\n");
        if (overallBalance > 0) {
            summary.append("Overall Balance: Extra money left: ₹").append(format(overallBalance));
        } else if (overallBalance < 0) {
            summary.append("Overall Balance: More money needed: ₹").append(format(Math.abs(overallBalance)));
        } else {
            summary.append("Overall Balance: Settled (0)");
        }

        outputSummary.setText(summary.toString());
        outputReport.setText("");
    }

    private void showDetailedReport() {
        if (expenseTypes.isEmpty()) {
            outputReport.setText("No expenses added yet.");
            outputSummary.setText("");
            return;
        }

        double totalExpense = sum(expenseAmounts);
        double totalContribution = sum(new ArrayList<>(contributions.values()));
        double balance = totalContribution - totalExpense;

        StringBuilder report = new StringBuilder();
        report.append("=== Detailed Expense Report ===\n\n");

        report.append("--- All Expenses (Transaction Log) ---\n");
        for (int i = 0; i < expenseTypes.size(); i++) {
            report.append(i + 1).append(". ").append(expenseTypes.get(i))
                    .append(" - ₹").append(format(expenseAmounts.get(i))).append("\n");
        }

        report.append("\n--- Category-wise Summary ---\n");
        Map<String, Double> categoryTotals = new LinkedHashMap<>();
        for (int i = 0; i < expenseTypes.size(); i++) {
            String type = expenseTypes.get(i);
            double amt = expenseAmounts.get(i);
            categoryTotals.put(type, categoryTotals.getOrDefault(type, 0.0) + amt);
        }
        for (Map.Entry<String, Double> e : categoryTotals.entrySet()) {
            double percent = (totalExpense > 0) ? (e.getValue() * 100.0 / totalExpense) : 0.0;
            report.append(e.getKey()).append(": ₹").append(format(e.getValue())).append(" (")
                    .append(format(percent)).append("%)\n");
        }

        report.append("\nTotal Expenses:      ₹").append(format(totalExpense)).append("\n");
        report.append("Total Contributions: ₹").append(format(totalContribution)).append("\n");
        if (balance > 0) {
            report.append("Balance (Extra Money Left): ₹").append(format(balance));
        } else if (balance < 0) {
            report.append("Balance (More Money Needed): ₹").append(format(Math.abs(balance)));
        } else {
            report.append("Balance: Settled (0)");
        }

        outputReport.setText(report.toString());
        outputSummary.setText("");
    }

    private double sum(List<Double> vals) {
        double s = 0.0;
        for (Double v : vals) {
            if (v != null) s += v;
        }
        return s;
    }

    private String format(double v) {
        return String.format("%.2f", v);
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
