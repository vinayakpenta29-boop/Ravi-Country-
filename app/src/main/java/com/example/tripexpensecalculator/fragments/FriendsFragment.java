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

import org.json.JSONArray;
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

    // list of amounts per friend (total, independent of cash/online for now)
    private static final Map<String, List<Double>> contributions = new LinkedHashMap<>();
    private static final String PREFS_NAME = "TripExpensePrefs";
    private static final String FRIENDS_KEY = "FriendsList";
    private static final String KEY_ONLINE_MODE = "OnlineMode"; // toggle flag

    public static Map<String, List<Double>> getContributions() {
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

        contributions.put(name, new ArrayList<>());

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

    // Show full history like Ravi = 100+300+400 = ₹800
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

        for (Map.Entry<String, List<Double>> entry : contributions.entrySet()) {
            String name = entry.getKey();
            List<Double> list = entry.getValue();
            if (list == null || list.isEmpty()) continue;

            double total = 0.0;
            StringBuilder expr = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                double v = list.get(i);
                total += v;
                if ((long) v == v) expr.append((long) v);
                else expr.append(v);
                if (i < list.size() - 1) expr.append(" + ");
            }

            // row
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 8, 0, 8);

            TextView lineTv = new TextView(getContext());
            lineTv.setText(name + " = " + expr + " = ₹" + String.format("%.2f", total));
            lineTv.setTextSize(16);
            lineTv.setTextColor(getResources().getColor(R.color.black));
            row.addView(lineTv);

            container.addView(row);

            // real divider view between rows
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

    private void refreshUI() {
        friendsListLayout.removeAllViews();

        if (btnAddFriend.getParent() != null) {
            ((ViewGroup) btnAddFriend.getParent()).removeView(btnAddFriend);
        }

        int inputNameIndex = rootLayout.indexOfChild(inputName);

        if (contributions.isEmpty()) {
            rootLayout.addView(btnAddFriend, inputNameIndex + 1);
        } else {
            for (Map.Entry<String, List<Double>> entry : contributions.entrySet()) {
                final String friendName = entry.getKey();
                final List<Double> list = entry.getValue();

                double total = 0.0;
                if (list != null) {
                    for (double v : list) total += v;
                }

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
                    if (!amtStr.isEmpty()) {
                        try {
                            double addVal = Double.parseDouble(amtStr);
                            List<Double> l = contributions.get(friendName);
                            if (l == null) {
                                l = new ArrayList<>();
                                contributions.put(friendName, l);
                            }
                            l.add(addVal);
                            saveFriendsData();
                            refreshUI();
                        } catch (NumberFormatException ignored) { }
                    }
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

    private void saveFriendsData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, List<Double>> entry : contributions.entrySet()) {
                JSONArray arr = new JSONArray();
                for (double v : entry.getValue()) arr.put(v);
                obj.put(entry.getKey(), arr);
            }
        } catch (Exception ignored) { }
        prefs.edit().putString(FRIENDS_KEY, obj.toString()).apply();
    }

    private void loadFriendsData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(FRIENDS_KEY, null);
        contributions.clear();
        if (json != null) {
            try {
                JSONObject obj = new JSONObject(json);
                Iterator<String> keys = obj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONArray arr = obj.getJSONArray(key);
                    List<Double> list = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(arr.getDouble(i));
                    }
                    contributions.put(key, list);
                }
            } catch (Exception ignored) { }
        }
    }
}
