package com.example.tripexpensecalculator.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExpenseFragment extends Fragment {

    private EditText inputCategory, inputAmount;
    private Button btnAddExpense;
    private LinearLayout expensesListLayout, expenseInputCard;
    private ViewGroup rootLayout;

    public static ExpenseFragment instance = null;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;          // no setHasOptionsMenu() now
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        instance = this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_expense, container, false);

        // Toolbar inside fragment for 3-dots menu
        Toolbar toolbar = root.findViewById(R.id.expensesToolbar);
        toolbar.inflateMenu(R.menu.expense_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_delete_expense) {
                showDeleteExpenseDialog();
                return true;
            }
            return false;
        });

        inputCategory = root.findViewById(R.id.inputCategory);
        inputAmount = root.findViewById(R.id.inputAmount);
        btnAddExpense = root.findViewById(R.id.btnAddExpense);
        expensesListLayout = root.findViewById(R.id.expensesListLayout);
        expenseInputCard = root.findViewById(R.id.expenseInputCard);
        rootLayout = (ViewGroup) root;

        btnAddExpense.setOnClickListener(v -> addExpense());

        loadExpensesData();
        refreshExpensesUI();
        return root;
    }

    // ----- Core logic -----

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
        refreshExpensesUI();
        Toast.makeText(getContext(), "Expense added.", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteExpenseDialog() {
        if (expenseTypes.isEmpty()) {
            Toast.makeText(getContext(), "No expenses to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] expenseNames = new String[expenseTypes.size()];
        for (int i = 0; i < expenseTypes.size(); i++) {
            expenseNames[i] = expenseTypes.get(i) + " : ₹" + String.format("%.2f", expenseAmounts.get(i));
        }
        final boolean[] checkedItems = new boolean[expenseNames.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Expenses to Delete");
        builder.setMultiChoiceItems(expenseNames, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
            Button delBtn = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            if (delBtn != null) delBtn.setVisibility(isAnyChecked(checkedItems) ? View.VISIBLE : View.GONE);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Delete", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            final Button delBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            delBtn.setVisibility(View.GONE);
            delBtn.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            delBtn.setAllCaps(true);

            Button cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            cancelBtn.setTextColor(getResources().getColor(android.R.color.darker_gray));
            cancelBtn.setAllCaps(true);

            delBtn.setOnClickListener(v -> {
                List<Integer> toDelete = new ArrayList<>();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) toDelete.add(i);
                }
                if (!toDelete.isEmpty()) {
                    for (int idx : toDelete) {
                        String name = expenseNames[idx];
                        new AlertDialog.Builder(getContext())
                                .setTitle("Delete Expense")
                                .setMessage("Are you sure you want to Delete " + name + "?")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (d, w) -> {
                                    for (int j = toDelete.size() - 1; j >= 0; j--) {
                                        int delIdx = toDelete.get(j);
                                        expenseTypes.remove(delIdx);
                                        expenseAmounts.remove(delIdx);
                                    }
                                    saveExpensesData();
                                    refreshExpensesUI();
                                    Toast.makeText(getContext(), "Expense(s) deleted.", Toast.LENGTH_SHORT).show();
                                })
                                .show();
                    }
                    dialog.dismiss();
                }
            });
        });
        dialog.show();
    }

    private boolean isAnyChecked(boolean[] checkedItems) {
        for (boolean b : checkedItems) if (b) return true;
        return false;
    }

    public void safeRefreshExpensesUI() {
        if (expensesListLayout != null) refreshExpensesUI();
    }

    private void refreshExpensesUI() {
        expensesListLayout.removeAllViews();

        if (btnAddExpense.getParent() != null) {
            ((ViewGroup) btnAddExpense.getParent()).removeView(btnAddExpense);
        }

        int inputCardIdx = ((ViewGroup) expenseInputCard.getParent()).indexOfChild(expenseInputCard);

        if (expenseTypes.isEmpty()) {
            // Show only ADD button under inputs
            rootLayout.addView(btnAddExpense, inputCardIdx + 1);
        } else {
            // Show expenses list
            LinearLayout outerBox = new LinearLayout(getContext());
            outerBox.setOrientation(LinearLayout.VERTICAL);
            outerBox.setBackgroundResource(R.drawable.curved_box_with_border);
            outerBox.setPadding(45, 28, 45, 28);

            LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            outerParams.setMargins(0, 10, 0, 24);
            outerBox.setLayoutParams(outerParams);

            for (int i = 0; i < expenseTypes.size(); i++) {
                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);

                TextView typeView = new TextView(getContext());
                typeView.setText(expenseTypes.get(i));
                typeView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                typeView.setTextColor(getResources().getColor(R.color.input_text));
                typeView.setTextSize(16);
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                typeView.setLayoutParams(leftParams);

                TextView amtView = new TextView(getContext());
                amtView.setText("₹" + String.format("%.2f", expenseAmounts.get(i)));
                amtView.setTextColor(getResources().getColor(R.color.input_text));
                amtView.setTextSize(16);

                row.addView(typeView);
                row.addView(amtView);

                outerBox.addView(row);

                if (i < expenseTypes.size() - 1) {
                    View divider = new View(getContext());
                    divider.setBackgroundColor(getResources().getColor(R.color.divider));
                    LinearLayout.LayoutParams dividParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    dividParams.setMargins(0, 12, 0, 12);
                    divider.setLayoutParams(dividParams);
                    outerBox.addView(divider);
                }
            }
            expensesListLayout.addView(outerBox);

            // Add button below list
            rootLayout.addView(btnAddExpense);
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
