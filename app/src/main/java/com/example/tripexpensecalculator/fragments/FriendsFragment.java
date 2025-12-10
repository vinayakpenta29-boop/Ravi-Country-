package com.example.tripexpensecalculator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;
import com.example.tripexpensecalculator.TripManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends Fragment {

    private EditText inputName;
    private Button btnAddFriend;
    private LinearLayout friendsListLayout;
    private ViewGroup rootLayout;

    public static FriendsFragment instance = null;

    // Model: separate cash and online totals for each friend
    public static class FriendTotals {
        public double cash = 0.0;
        public double online = 0.0;
    }

    private static final Map<String, FriendTotals> contributions = new LinkedHashMap<>();
    private static final String PREFS_NAME = "TripExpensePrefs";
    private static final String KEY_ONLINE_MODE = "OnlineMode"; // global toggle flag

    public static Map<String, FriendTotals> getContributions() {
        return contributions;
    }

    // ---------- online mode helpers (used by other fragments too) ----------
    public static boolean isOnlineMode(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONLINE_MODE, false);
    }

    public static void setOnlineMode(Context ctx, boolean value) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONLINE_MODE, value).apply();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;   // no setHasOptionsMenu()
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
        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        // Toolbar inside fragment for 3-dots menu
        Toolbar toolbar = root.findViewById(R.id.friendsToolbar);
        toolbar.inflateMenu(R.menu.friends_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_delete_friend) {
                showDeleteFriendDialog();
                return true;
            } else if (id == R.id.menu_given_amount) {
                showGivenAmountDialog();
                return true;
            } else if (id == R.id.menu_new_trip) {
                showNewTripDialog();
                return true;
            } else if (id == R.id.menu_trips) {
                showTripsDialog();
                return true;
            }
            return false;
        });

        // Online Payment toggle (SwitchCompat in layout)
        SwitchCompat switchOnline = root.findViewById(R.id.switchOnlinePayment);
        boolean current = isOnlineMode(requireContext());
        switchOnline.setChecked(current);
        switchOnline.setOnCheckedChangeListener((buttonView, isChecked) ->
                setOnlineMode(requireContext(), isChecked)
        );

        inputName = root.findViewById(R.id.inputName);
        btnAddFriend = root.findViewById(R.id.btnAddFriend);
        friendsListLayout = root.findViewById(R.id.friendsListLayout);
        rootLayout = (ViewGroup) root;

        btnAddFriend.setOnClickListener(v -> addFriend());

        loadFriendsData();
        refreshUI();
        return root;
    }

    // ---------- logic ----------
    public void safeRefreshUI() {
        if (friendsListLayout != null) refreshUI();
    }

    private void addFriend() {
        String name = inputName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Friend's name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contributions.containsKey(name)) {
            Toast.makeText(getContext(), "Friend already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        FriendTotals totals = new FriendTotals();
        contributions.put(name, totals);

        inputName.setText("");
        saveFriendsData();
        refreshUI();
        Toast.makeText(getContext(), "Friend added.", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteFriendDialog() {
        if (contributions.isEmpty()) {
            Toast.makeText(getContext(), "No friends to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] friendNames = contributions.keySet().toArray(new String[0]);
        final boolean[] checkedItems = new boolean[friendNames.length];

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Friends to Delete")
                .setMultiChoiceItems(friendNames, checkedItems, (dialog, which, isChecked) -> {
                    checkedItems[which] = isChecked;
                })
                .setPositiveButton("OK", (dialog, which) -> {
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            if (count > 0) sb.append(", ");
                            sb.append(friendNames[i]);
                            count++;
                        }
                    }
                    if (count == 0) return;

                    String namesToDelete = sb.toString();

                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Delete Friend" + (count > 1 ? "s" : ""))
                            .setMessage("Are you sure you want to delete " + namesToDelete + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                int deletedCount = 0;
                                for (int i = 0; i < checkedItems.length; i++) {
                                    if (checkedItems[i]) {
                                        contributions.remove(friendNames[i]);
                                        deletedCount++;
                                    }
                                }
                                saveFriendsData();
                                refreshUI();
                                Toast.makeText(getContext(), deletedCount + " friend(s) deleted.", Toast.LENGTH_SHORT).show();
                            })
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Show full history like Ravi = cash+online = total
    private void showGivenAmountDialog() {
        if (contributions.isEmpty()) {
            Toast.makeText(getContext(), "No friends available.", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_given_amount, null);
        LinearLayout container = dialogView.findViewById(R.id.givenAmountContainer);

        int index = 0;
        int size = contributions.size();

        for (Map.Entry<String, FriendTotals> entry : contributions.entrySet()) {
            String name = entry.getKey();
            FriendTotals t = entry.getValue();
            double total = t.cash + t.online;

            String expr = "";
            if (t.cash > 0 && t.online > 0) {
                expr = String.format("Cash %.2f + Online %.2f", t.cash, t.online);
            } else if (t.cash > 0) {
                expr = String.format("Cash %.2f", t.cash);
            } else if (t.online > 0) {
                expr = String.format("Online %.2f", t.online);
            }

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 8, 0, 8);

            TextView lineTv = new TextView(getContext());
            lineTv.setText(name + " = " + expr + " = ₹" + String.format("%.2f", total));
            lineTv.setTextSize(16);
            lineTv.setTextColor(getResources().getColor(R.color.black));
            row.addView(lineTv);

            container.addView(row);

            if (index < size - 1) {
                View divider = new View(getContext());
                divider.setBackgroundColor(getResources().getColor(R.color.divider));
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2);
                p.setMargins(0, 4, 0, 4);
                divider.setLayoutParams(p);
                container.addView(divider);
            }
            index++;
        }

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Given Amount")
                .setView(dialogView)
                .setPositiveButton("OK", null)
                .show();
    }

    // ----- Trips -----

    private void showNewTripDialog() {
        final EditText input = new EditText(getContext());
        input.setHint("Enter trip name");

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("New Trip")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Trip name required.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TripManager.setCurrentTrip(requireContext(), name);

                    // clear in‑memory data for this trip
                    contributions.clear();
                    saveFriendsData();
                    ExpenseFragment.clearExpensesForCurrentTrip(requireContext());

                    refreshUI();
                    if (ExpenseFragment.instance != null) {
                        ExpenseFragment.instance.safeRefreshExpensesUI();
                    }
                    Toast.makeText(getContext(), "Trip created: " + name, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showTripsDialog() {
        List<String> trips = TripManager.getAllTrips(requireContext());
        if (trips.isEmpty()) {
            Toast.makeText(getContext(), "No trips yet. Create a New Trip.", Toast.LENGTH_SHORT).show();
            return;
        }

        String current = TripManager.getCurrentTrip(requireContext());
        int checked = Math.max(0, trips.indexOf(current));

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Trip")
                .setSingleChoiceItems(trips.toArray(new String[0]), checked, null)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (dialog, which) -> {
                    android.app.AlertDialog ad = (android.app.AlertDialog) dialog;
                    int sel = ad.getListView().getCheckedItemPosition();
                    if (sel < 0 || sel >= trips.size()) return;

                    String chosen = trips.get(sel);
                    TripManager.setCurrentTrip(requireContext(), chosen);

                    loadFriendsData();
                    refreshUI();
                    if (ExpenseFragment.instance != null) {
                        ExpenseFragment.instance.loadExpensesDataForCurrentTrip();
                        ExpenseFragment.instance.safeRefreshExpensesUI();
                    }
                    Toast.makeText(getContext(), "Trip selected: " + chosen, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void refreshUI() {
        friendsListLayout.removeAllViews();

        if (btnAddFriend.getParent() != null) {
            ((ViewGroup) btnAddFriend.getParent()).removeView(btnAddFriend);
        }

        int inputNameIndex = rootLayout.indexOfChild(inputName);

        if (contributions.isEmpty()) {
            rootLayout.addView(btnAddFriend, inputNameIndex + 1);
        } else {
            for (Map.Entry<String, FriendTotals> entry : contributions.entrySet()) {
                final String friendName = entry.getKey();
                final FriendTotals totals = entry.getValue();

                double total = totals.cash + totals.online;

                LinearLayout cardBox = new LinearLayout(getContext());
                cardBox.setOrientation(LinearLayout.VERTICAL);
                cardBox.setBackgroundResource(R.drawable.pista_with_border);
                cardBox.setPadding(20, 28, 20, 28);

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 32);
                cardBox.setLayoutParams(cardParams);

                LinearLayout topInner = new LinearLayout(getContext());
                topInner.setOrientation(LinearLayout.HORIZONTAL);
                topInner.setBackgroundResource(R.drawable.white_with_no_border);
                topInner.setPadding(24, 20, 24, 20);
                LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                topInner.setLayoutParams(topParams);

                TextView nameView = new TextView(getContext());
                nameView.setText(friendName);
                nameView.setTextSize(18);
                nameView.setTextColor(getResources().getColor(R.color.input_text));
                nameView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                nameView.setLayoutParams(leftParams);

                TextView amtView = new TextView(getContext());
                amtView.setText("₹" + String.format("%.2f", total));
                amtView.setTextSize(17);
                amtView.setTextColor(getResources().getColor(R.color.black));
                amtView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

                topInner.addView(nameView);
                topInner.addView(amtView);

                View divider = new View(getContext());
                divider.setBackgroundColor(getResources().getColor(R.color.divider));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 2);
                dividerParams.setMargins(0, 16, 0, 16);
                divider.setLayoutParams(dividerParams);

                LinearLayout botInner = new LinearLayout(getContext());
                botInner.setOrientation(LinearLayout.HORIZONTAL);
                botInner.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams botParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                botInner.setLayoutParams(botParams);

                EditText inputAmt = new EditText(getContext());
                inputAmt.setHint("Enter Amount");
                inputAmt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                inputAmt.setBackgroundResource(R.drawable.white_with_no_border);
                inputAmt.setTextColor(getResources().getColor(R.color.black));
                inputAmt.setHintTextColor(getResources().getColor(R.color.grey_hint));
                inputAmt.setTextSize(16);
                inputAmt.setPadding(14, 20, 14, 20);
                LinearLayout.LayoutParams inputAmtParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                inputAmt.setLayoutParams(inputAmtParams);

                Button addAmtBtn = new Button(getContext());
                addAmtBtn.setText("ADDAMOUNT");
                addAmtBtn.setTextColor(getResources().getColor(R.color.white_for_button));
                addAmtBtn.setTextSize(14);
                addAmtBtn.setBackgroundResource(R.drawable.curved_orange_button);
                addAmtBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                addAmtBtn.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams btnParams =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                btnParams.setMargins(24, 0, 0, 0);
                addAmtBtn.setLayoutParams(btnParams);
                addAmtBtn.setPadding(32, 22, 32, 22);

                addAmtBtn.setOnClickListener(v -> {
                    String amtStr = inputAmt.getText().toString().trim();
                    if (amtStr.isEmpty()) return;

                    try {
                        double addVal = Double.parseDouble(amtStr);

                        boolean onlineMode = isOnlineMode(requireContext());
                        if (!onlineMode) {
                            // toggle OFF: everything treated as cash
                            totals.cash += addVal;
                            finishFriendAddAmount(inputAmt);
                            return;
                        }

                        // Custom dialog with cash / online icons
                        android.app.AlertDialog dialog =
                                new android.app.AlertDialog.Builder(requireContext())
                                        .setCancelable(true)
                                        .create();

                        dialog.setOnShowListener(dlg -> {
                            dialog.setContentView(R.layout.dialog_payment_type);

                            TextView btnCash = dialog.findViewById(R.id.btnCash);
                            TextView btnOnline = dialog.findViewById(R.id.btnOnline);

                            if (btnCash != null) {
                                btnCash.setText("Cash");
                                btnCash.setOnClickListener(v1 -> {
                                    totals.cash += addVal;
                                    finishFriendAddAmount(inputAmt);
                                    dialog.dismiss();
                                });
                            }

                            if (btnOnline != null) {
                                btnOnline.setText("Online");
                                btnOnline.setOnClickListener(v12 -> {
                                    totals.online += addVal;
                                    finishFriendAddAmount(inputAmt);
                                    dialog.dismiss();
                                });
                            }
                        });

                        dialog.show();

                    } catch (NumberFormatException ignored) { }
                });

                botInner.addView(inputAmt);
                botInner.addView(addAmtBtn);

                cardBox.addView(topInner);
                cardBox.addView(divider);
                cardBox.addView(botInner);

                friendsListLayout.addView(cardBox);
            }
            rootLayout.addView(btnAddFriend);
        }
    }

    private void finishFriendAddAmount(EditText inputAmt) {
        inputAmt.setText("");
        saveFriendsData();
        refreshUI();
        Toast.makeText(getContext(), "Amount added.", Toast.LENGTH_SHORT).show();
    }

    private void saveFriendsData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String trip = TripManager.getCurrentTrip(requireContext());
        String key = TripManager.keyForFriends(trip);

        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, FriendTotals> entry : contributions.entrySet()) {
                FriendTotals t = entry.getValue();
                JSONObject o = new JSONObject();
                o.put("cash", t.cash);
                o.put("online", t.online);
                obj.put(entry.getKey(), o);
            }
        } catch (Exception ignored) { }
        prefs.edit().putString(key, obj.toString()).apply();
    }

    private void loadFriendsData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String trip = TripManager.getCurrentTrip(requireContext());
        String key = TripManager.keyForFriends(trip);

        String json = prefs.getString(key, null);
        contributions.clear();
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                Iterator<String> keys = obj.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    JSONObject o = obj.getJSONObject(k);
                    FriendTotals t = new FriendTotals();
                    t.cash = o.optDouble("cash", 0.0);
                    t.online = o.optDouble("online", 0.0);
                    contributions.put(k, t);
                }
            } catch (Exception ignored) { }
        }
    }
}
