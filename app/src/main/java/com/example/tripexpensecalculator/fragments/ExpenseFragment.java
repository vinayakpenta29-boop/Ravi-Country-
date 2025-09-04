package com.example.tripexpensecalculator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
            final int idx = i;

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 0, 0, 8);

            TextView tv = new TextView(getContext());
            tv.setText(expenseTypes.get(idx) + ": ₹" + String.format("%.2f", expenseAmounts.get(idx)));
            tv.setTextSize(16);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tv);

            Button editBtn = new Button(getContext());
            editBtn.setText("Edit");
            editBtn.setTextSize(14);
            editBtn.setOnClickListener(v -> showEditExpenseDialog(idx));
            row.addView(editBtn);

            Button deleteBtn = new Button(getContext());
            deleteBtn.setText("Delete");
            deleteBtn.setTextSize(14);
            deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Expense")
                    .setMessage("Are you sure you want to delete this expense?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        expenseTypes.remove(idx);
                        expenseAmounts.remove(idx);
                        saveExpensesData();
                        refreshExpensesList();
                        Toast.makeText(getContext(), "Expense deleted!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
            row.addView(deleteBtn);

            expensesListLayout.addView(row);
        }
    }

    private void showEditExpenseDialog(int idx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Expense");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        final EditText editCategory = new EditText(requireContext());
        final EditText editAmount = new EditText(requireContext());
        editCategory.setInputType(InputType.TYPE_CLASS_TEXT);
        editAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editCategory.setText(expenseTypes.get(idx));
        editAmount.setText(String.format("%.2f", expenseAmounts.get(idx)));
        layout.addView(editCategory);
        layout.addView(editAmount);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newCategory = editCategory.getText().toString().trim();
            String newAmtStr = editAmount.getText().toString().trim();
            if (!newCategory.isEmpty() && !newAmtStr.isEmpty()) {
                try {
                    double newAmt = Double.parseDouble(newAmtStr);
                    expenseTypes.set(idx, newCategory);
                    expenseAmounts.set(idx, newAmt);
                    saveExpensesData();
                    refreshExpensesList();
                    Toast.makeText(getContext(), "Updated expense!", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid data.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
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
        } catch (Exception e) { }
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
            } catch (Exception e) { }
        }
    }
}
