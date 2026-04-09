package com.example.tripexpensecalculator.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;
import com.example.tripexpensecalculator.TripManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpenseFragment extends Fragment {

    private EditText inputCategory, inputAmount;
    private Button btnAddExpense;
    private LinearLayout expensesListLayout, expenseInputCard;
    private ViewGroup rootLayout;

    public static ExpenseFragment instance = null;

    private static final List<String> expenseTypes = new ArrayList<>();
    private static final List<Double> expenseAmounts = new ArrayList<>();
    private static final List<String> expensePaidBy = new ArrayList<>();
    private static final List<Boolean> expenseIsOnline = new ArrayList<>();
    private static final List<List<String>> expenseSplitBetween = new ArrayList<>();

    public static List<List<String>> getExpenseSplitBetween() {
        return expenseSplitBetween;
    }

    private static final String PREFS_NAME = "TripExpensePrefs";

    public static List<String> getExpenseTypes() {
        return expenseTypes;
    }

    public static List<Double> getExpenseAmounts() {
        return expenseAmounts;
    }

    public static List<String> getExpensePaidBy() {
        return expensePaidBy;
    }

    public static List<Boolean> getExpenseIsOnline() {
        return expenseIsOnline;
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

        loadExpensesDataForCurrentTrip();
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

        // 🔥 STEP 1: Get friends
        Map<String, FriendsFragment.FriendTotals> friends = FriendsFragment.getContributions();

        if (friends.isEmpty()) {
            Toast.makeText(getContext(), "Add friends first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] friendNames = friends.keySet().toArray(new String[0]);

        // 🔥 STEP 2: Ask who paid
        new AlertDialog.Builder(requireContext())
                .setTitle("Who Paid?")
                .setItems(friendNames, (dialog, which) -> {

                    String selectedFriend = friendNames[which];

                    // 🔥 STEP: Select people to split with
                    // 🔥 Add "Select All"
                    String[] namesWithSelectAll = new String[friendNames.length + 1];
                    namesWithSelectAll[0] = "Select All";
                    System.arraycopy(friendNames, 0, namesWithSelectAll, 1, friendNames.length);

                    boolean[] checkedItems = new boolean[namesWithSelectAll.length];

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Split Between")
                            .setMultiChoiceItems(namesWithSelectAll, checkedItems, (dlg, index, isChecked) -> {

                                if (index == 0) {
                                    // ✅ Select All clicked
                                   for (int i = 0; i < checkedItems.length; i++) {
                                        checkedItems[i] = isChecked;
                                        ((AlertDialog) dlg).getListView().setItemChecked(i, isChecked);
                                    }
                                } else {
                                    // ❌ If any unchecked → uncheck Select All
                                    if (!isChecked) {
                                        checkedItems[0] = false;
                                        ((AlertDialog) dlg).getListView().setItemChecked(0, false);
                                    }
                                }
                            })
                            .setPositiveButton("OK", (d, w) -> {

                                List<String> selectedPeople = new ArrayList<>();

                                for (int i = 1; i < checkedItems.length; i++) { // skip "Select All"
                                    if (checkedItems[i]) {
                                        selectedPeople.add(namesWithSelectAll[i]);
                                    }
                                }

                                if (selectedPeople.isEmpty()) {
                                    Toast.makeText(getContext(), "Select at least one person", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                boolean onlineMode = FriendsFragment.isOnlineMode(requireContext());

                                if (!onlineMode) {
                                    saveExpenseWithSplit(category, amount, selectedFriend, false, selectedPeople);
                                    return;
                                }

                                // Payment dialog
                                AlertDialog paymentDialog = new AlertDialog.Builder(requireContext())
                                        .setCancelable(false)
                                        .create();

                                paymentDialog.setOnShowListener(dlg -> {
                                paymentDialog.setContentView(R.layout.dialog_payment_type);

                                    TextView btnCash = paymentDialog.findViewById(R.id.btnCash);
                                    TextView btnOnline = paymentDialog.findViewById(R.id.btnOnline);

                                    if (btnCash != null) {
                                        btnCash.setOnClickListener(v -> {
                                            saveExpenseWithSplit(category, amount, selectedFriend, false, selectedPeople);
                                            paymentDialog.dismiss();
                                        });
                                    }

                                    if (btnOnline != null) {
                                        btnOnline.setOnClickListener(v -> {
                                            saveExpenseWithSplit(category, amount, selectedFriend, true, selectedPeople);
                                            paymentDialog.dismiss();
                                        });
                                    }
                                });

                                paymentDialog.show();
                            })
                            .show();
                    })
            .show();
    }

    // common code to actually store the expense
    private void saveExpenseSimple(String category, double amount, String paidBy, boolean isOnline) {
        expenseTypes.add(category);
        expenseAmounts.add(amount);
        expensePaidBy.add(paidBy);
        expenseIsOnline.add(isOnline);

        inputCategory.setText("");
        inputAmount.setText("");

        saveExpensesDataForCurrentTrip();
        refreshExpensesUI();
        Toast.makeText(getContext(), "Expense added.", Toast.LENGTH_SHORT).show();
    }

    private void saveExpenseWithSplit(String category, double amount, String paidBy, boolean isOnline, List<String> splitPeople) {

        expenseTypes.add(category);
        expenseAmounts.add(amount);
        expensePaidBy.add(paidBy);
        expenseIsOnline.add(isOnline);
        expenseSplitBetween.add(splitPeople);

        // 🔥 UPDATE FRIEND PAID AMOUNT
        Map<String, FriendsFragment.FriendTotals> friends = FriendsFragment.getContributions();

        if (friends.containsKey(paidBy)) {
            FriendsFragment.FriendTotals totals = friends.get(paidBy);

            if (isOnline) {
                totals.online += amount;
                totals.onlineEntries.add(amount);
            } else {
                totals.cash += amount;
                totals.cashEntries.add(amount);
            }
        }

        inputCategory.setText("");
        inputAmount.setText("");

        saveExpensesDataForCurrentTrip();
        refreshExpensesUI();

        // 🔄 Save updated friend data
        if (FriendsFragment.instance != null) {
            FriendsFragment.instance.safeRefreshUI();
        }

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
                                        if (delIdx < expenseSplitBetween.size()) {
                                            expenseSplitBetween.remove(delIdx);
                                        }
                                        if (delIdx < expenseIsOnline.size()) {
                                            expenseIsOnline.remove(delIdx);
                                        }
                                    }
                                    saveExpensesDataForCurrentTrip();
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
                int position = i; // IMPORTANT (for click reference)

                row.setOnClickListener(v -> {
                    showExpenseDetailsPopup(position);
                });
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);

                // icon depending on payment type
                android.widget.ImageView icon = new android.widget.ImageView(getContext());
                int sizePx = (int) (20 * getResources().getDisplayMetrics().density);
                LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(sizePx, sizePx);
                iconLp.setMargins(0, 0, 16, 0);
                icon.setLayoutParams(iconLp);
                boolean isOnline = (i < expenseIsOnline.size()) && Boolean.TRUE.equals(expenseIsOnline.get(i));
                icon.setImageResource(isOnline ? R.mipmap.ic_online : R.mipmap.ic_cash);

                LinearLayout textContainer = new LinearLayout(getContext());
                textContainer.setOrientation(LinearLayout.HORIZONTAL);
                textContainer.setLayoutParams(new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                ));

                // 🔹 Expense Name
                TextView typeView = new TextView(getContext());
                typeView.setText(expenseTypes.get(i));
                typeView.setSingleLine(true);
                typeView.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
                typeView.setMarqueeRepeatLimit(-1); // infinite scroll
                typeView.setSelected(true); // VERY IMPORTANT
                typeView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                typeView.setTextColor(getResources().getColor(R.color.input_text));
                typeView.setTextSize(16);

                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                );
                typeView.setLayoutParams(leftParams);

                // 🔹 Combined Badge (Name + Amount)
                LinearLayout badgeContainer = new LinearLayout(getContext());
                badgeContainer.setOrientation(LinearLayout.HORIZONTAL);

                // 👉 Rounded background
                badgeContainer.setBackgroundResource(R.drawable.bg_combined_badge);

                // 👉 Name part
                TextView namePart = new TextView(getContext());
                String paidBy = (i < expensePaidBy.size()) ? expensePaidBy.get(i) : "Unknown";
                namePart.setText(" " + paidBy + " ");
                namePart.setTextSize(12);
                namePart.setTextColor(Color.BLACK);
                namePart.setPadding(20, 8, 20, 8);

                // 👉 Divider line
                View dividerLine = new View(getContext());
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(2, LinearLayout.LayoutParams.MATCH_PARENT);
                dividerLine.setLayoutParams(dividerParams);
                dividerLine.setBackgroundColor(Color.DKGRAY);

                // 👉 Amount part
                TextView amountPart = new TextView(getContext());
                amountPart.setText(" ₹" + String.format("%.0f", expenseAmounts.get(i)) + " ");
                amountPart.setTextSize(12);
                amountPart.setTextColor(Color.WHITE);
                amountPart.setPadding(20, 8, 20, 8);

                // 👉 Background for amount side
                amountPart.setBackgroundResource(R.drawable.bg_amount_part);

                // add all
                badgeContainer.addView(namePart);
                badgeContainer.addView(dividerLine);
                badgeContainer.addView(amountPart);

                // margin
                LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                badgeParams.setMargins(16, 0, 0, 0);
                badgeContainer.setLayoutParams(badgeParams);

                

                // add both
                textContainer.addView(typeView);
                textContainer.addView(badgeContainer);
                
                row.addView(icon);
                row.addView(textContainer);
        
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

    // ----- Trip‑specific persistence -----

    private String getKeyForCurrentTrip() {
        String trip = TripManager.getCurrentTrip(requireContext());
        return TripManager.keyForExpenses(trip);
    }

    private void showExpenseDetailsPopup(int index) {

    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

    LinearLayout layout = new LinearLayout(getContext());
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 40, 40, 40);

    // 🔹 Title (Category)
    TextView title = new TextView(getContext());
    title.setText(expenseTypes.get(index));
    title.setTextSize(20);
    title.setTypeface(Typeface.DEFAULT_BOLD);
    title.setGravity(android.view.Gravity.CENTER);
    title.setTextColor(Color.BLACK);
    title.setPadding(0, 0, 0, 20);
    layout.addView(title);

    // 🔹 Amount
    TextView amountTv = new TextView(getContext());
    amountTv.setText("Amount : ₹" + String.format("%.2f", expenseAmounts.get(index)));
    amountTv.setTextSize(16);
    amountTv.setTextColor(Color.BLACK);
    amountTv.setPadding(0, 10, 0, 10);
    layout.addView(amountTv);

    // 🔹 Paid By
    String paidBy = (index < expensePaidBy.size()) ? expensePaidBy.get(index) : "Unknown";

    TextView paidByTv = new TextView(getContext());
    paidByTv.setText("Paid by : " + paidBy);
    paidByTv.setTextSize(16);
    paidByTv.setTextColor(Color.BLACK);
    paidByTv.setPadding(0, 10, 0, 20);
    layout.addView(paidByTv);

    // 🔹 Split Members Title
    TextView splitTitle = new TextView(getContext());
    splitTitle.setText("Split Between");
    splitTitle.setTextSize(16);
    splitTitle.setTypeface(Typeface.DEFAULT_BOLD);
    splitTitle.setTextColor(Color.BLACK);
    splitTitle.setPadding(0, 10, 0, 10);
    layout.addView(splitTitle);

    // 🔹 Get Split List
    List<List<String>> splits = getExpenseSplitBetween();
    List<String> splitList;

    if (index < splits.size() && splits.get(index) != null && !splits.get(index).isEmpty()) {
        splitList = splits.get(index);
    } else {
        splitList = new ArrayList<>(FriendsFragment.getContributions().keySet());
    }

    // 🔹 Show Members
    for (String member : splitList) {
        TextView memberTv = new TextView(getContext());
        memberTv.setText("• " + member);
        memberTv.setTextSize(14);
        memberTv.setTextColor(Color.DKGRAY);
        memberTv.setPadding(0, 4, 0, 4);
        layout.addView(memberTv);
    }

    builder.setView(layout);
    builder.setPositiveButton("OK", null);
    builder.show();
}

    private void saveExpensesDataForCurrentTrip() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray typesArr = new JSONArray();
        JSONArray amtsArr = new JSONArray();
        JSONArray paidByArr = new JSONArray();
        JSONArray onlineArr = new JSONArray();
        JSONArray splitArr = new JSONArray();

        for (List<String> group : expenseSplitBetween) {
            JSONArray inner = new JSONArray();
            for (String name : group) inner.put(name);
            splitArr.put(inner);
        }
        for (int i = 0; i < expenseTypes.size(); i++) {
            typesArr.put(expenseTypes.get(i));
            amtsArr.put(expenseAmounts.get(i));
            paidByArr.put(expensePaidBy.get(i));
            onlineArr.put(expenseIsOnline.get(i) ? 1 : 0);
        }
        JSONObject data = new JSONObject();
        try {
            data.put("types", typesArr);
            data.put("amounts", amtsArr);
            data.put("paidBy", paidByArr);
            data.put("onlineFlags", onlineArr);
            data.put("splitBetween", splitArr);
        } catch (Exception ignored) { }
        prefs.edit().putString(getKeyForCurrentTrip(), data.toString()).apply();
    }

    public void loadExpensesDataForCurrentTrip() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(getKeyForCurrentTrip(), null);
        expenseTypes.clear();
        expenseAmounts.clear();
        expensePaidBy.clear();
        expenseIsOnline.clear();
        expenseSplitBetween.clear();
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                JSONArray typesArr = obj.getJSONArray("types");
                JSONArray amtsArr = obj.getJSONArray("amounts");
                JSONArray paidByArr = obj.optJSONArray("paidBy");
                JSONArray onlineArr = obj.optJSONArray("onlineFlags");
                JSONArray splitArr = obj.optJSONArray("splitBetween");
                
                for (int i = 0; i < typesArr.length(); i++) {
                    expenseTypes.add(typesArr.getString(i));
                    expenseAmounts.add(amtsArr.getDouble(i));
                    String paidBy = "Unknown";
                    if (paidByArr != null && i < paidByArr.length()) {
                        paidBy = paidByArr.getString(i);
                    }
                    expensePaidBy.add(paidBy);
                    boolean isOnline = false;
                    if (onlineArr != null && i < onlineArr.length()) {
                        isOnline = onlineArr.getInt(i) == 1;
                    }
                    expenseIsOnline.add(isOnline);
                    

                    List<String> splitList = new ArrayList<>();

                    if (splitArr != null && i < splitArr.length()) {
                        JSONArray inner = splitArr.getJSONArray(i);
                        for (int j = 0; j < inner.length(); j++) {
                            splitList.add(inner.getString(j));
                        }
                    }

                    if (splitList.isEmpty()) {
                        splitList = new ArrayList<>(FriendsFragment.getContributions().keySet());
                    }

                    expenseSplitBetween.add(splitList);
                }
            } catch (Exception ignored) { }
        }
    }

    public static void clearExpensesForCurrentTrip(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = TripManager.keyForExpenses(TripManager.getCurrentTrip(ctx));
        prefs.edit().remove(key).apply();
        expenseTypes.clear();
        expenseAmounts.clear();
        expensePaidBy.clear();
        expenseIsOnline.clear();
        expenseSplitBetween.clear();
    }
}
