package com.example.tripexpensecalculator.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import java.util.ArrayList;
import java.util.List;

public class ExpenseFragment extends Fragment {

    private EditText inputCategory, inputAmount;
    private Button btnAddExpense;
    private LinearLayout expensesListLayout;

    // Shared data store (simplest approach)
    private static final List<String> expenseTypes = new ArrayList<>();
    private static final List<Double> expenseAmounts = new ArrayList<>();

    public static List<String> getExpenseTypes() {
        return expenseTypes;
    }

    public static List<Double> getExpenseAmounts() {
        return expenseAmounts;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_expense, container, false);

        inputCategory = root.findViewById(R.id.inputCategory);
        inputAmount = root.findViewById(R.id.inputAmount);
        btnAddExpense = root.findViewById(R.id.btnAddExpense);
        expensesListLayout = root.findViewById(R.id.expensesListLayout);

        btnAddExpense.setOnClickListener(v -> addExpense());

        refreshExpensesList();
        return root;
    }

    private void addExpense() {
        String category = inputCategory.getText().toString().trim();
        String amountStr = inputAmount.getText().toString().trim();

        if (TextUtils.isEmpty(category)) {
            Toast.makeText(getContext(), "Expense category cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(getContext(), "Expense amount cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount < 0) {
                Toast.makeText(getContext(), "Expense amount cannot be negative.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid expense amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        expenseTypes.add(category);
        expenseAmounts.add(amount);

        inputCategory.setText("");
        inputAmount.setText("");

        refreshExpensesList();
        Toast.makeText(getContext(), "Expense added.", Toast.LENGTH_SHORT).show();
    }

    private void refreshExpensesList() {
        expensesListLayout.removeAllViews();
        for (int i = 0; i < expenseTypes.size(); i++) {
            TextView tv = new TextView(getContext());
            tv.setText(expenseTypes.get(i) + ": ₹" + String.format("%.2f", expenseAmounts.get(i)));
            expensesListLayout.addView(tv);
        }
    }
}
