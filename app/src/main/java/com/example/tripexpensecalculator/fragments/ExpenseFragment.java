package com.example.tripexpensecalculator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExpenseFragment extends Fragment {

    private EditText inputCategory, inputAmount;
    private Button btnAddExpense;
    private LinearLayout expensesListLayout;

    private static final List<String> expenseTypes = new ArrayList<>();
    private static final List<Double> expenseAmounts = new ArrayList<>();
    private static final String PREFS_NAME = "TripExpensePrefs";
    private static final String EXPENSES_KEY = "ExpensesList";

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

        loadExpensesData();
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

        saveExpensesData();
        refreshExpensesList();
        Toast.makeText(getContext(), "Expense added.", Toast.LENGTH_SHORT).show();
    }

    private void refreshExpensesList() {
        expensesListLayout.removeAllViews();
        for (int i = 0; i < expenseTypes.size(); i++) {
            TextView tv = new TextView(getContext());
            tv.setText(expenseTypes.get(i) + ": ₹" + String.format("%.2f", expenseAmounts.get(i)));
            tv.setTextSize(16);
            expensesListLayout.addView(tv);
        }
    }

    private void saveExpensesData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray typesArr = new JSONArray();
        JSONArray amtsArr = new JSONArray();
        for (int i = 0; i < expenseTypes.size(); i++) {
            typesArr.put(expenseTypes.get(i));
            amtsArr.put(expenseAmounts.get(i));
        }
        JSONObject data = new JSONObject();
        try {
            data.put("types", typesArr);
            data.put("amounts", amtsArr);
        } catch (Exception ignored) { }
        prefs.edit().putString(EXPENSES_KEY, data.toString()).apply();
    }

    private void loadExpensesData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(EXPENSES_KEY, null);
        expenseTypes.clear();
        expenseAmounts.clear();
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray typesArr = obj.getJSONArray("types");
                JSONArray amtsArr = obj.getJSONArray("amounts");
                for (int i = 0; i < typesArr.length(); i++) {
                    expenseTypes.add(typesArr.getString(i));
                    expenseAmounts.add(amtsArr.getDouble(i));
                }
            } catch (Exception ignored) { }
        }
    }
}
