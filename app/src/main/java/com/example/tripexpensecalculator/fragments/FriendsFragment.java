tic Map<String, Double> getContributions() {
        return contributions;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        inputName = root.findViewById(R.id.inputName);
        btnAddFriend = root.findViewById(R.id.btnAddFriend);
        btnDeleteFriend = root.findViewById(R.id.btnDeleteFriend);
        friendsListLayout = root.findViewById(R.id.friendsListLayout);
        rootLayout = (ViewGroup) root;

        btnAddFriend.setOnClickListener(v -> addFriend());
        btnDeleteFriend.setOnClickListener(v -> showDeleteFriendDialog());

        loadFriendsData();
        refreshUI();
        return root;
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

        contributions.put(name, 0.0);

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
        new android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Friend")
            .setItems(friendNames, (dialog, which) -> {
                String nameToDelete = friendNames[which];
                contributions.remove(nameToDelete);
                saveFriendsData();
                refreshUI();
                Toast.makeText(getContext(), nameToDelete + " deleted.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void refreshUI() {
        friendsListLayout.removeAllViews();

        // Remove ADD FRIEND and DELETE FRIEND from old spot if needed
        if (btnAddFriend.getParent() != null) ((ViewGroup) btnAddFriend.getParent()).removeView(btnAddFriend);
        if (btnDeleteFriend.getParent() != null) ((ViewGroup) btnDeleteFriend.getParent()).removeView(btnDeleteFriend);

        if (contributions.isEmpty()) {
            int inputNameIndex = rootLayout.indexOfChild(inputName);
            rootLayout.addView(btnAddFriend, inputNameIndex + 1);
            rootLayout.addView(btnDeleteFriend, inputNameIndex + 2);
        } else {
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                final String friendName = entry.getKey();

                // OUTER CARD
                LinearLayout cardBox = new LinearLayout(getContext());
                cardBox.setOrientation(LinearLayout.VERTICAL);
                cardBox.setBackgroundResource(R.drawable.curved_box_with_border);
                cardBox.setPadding(20, 28, 20, 28);

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 32);
                cardBox.setLayoutParams(cardParams);

                // TOP INNER BOX
                LinearLayout topInner = new LinearLayout(getContext());
                topInner.setOrientation(LinearLayout.HORIZONTAL);
                topInner.setBackgroundResource(R.drawable.curved_light_box);
                topInner.setPadding(24, 20, 24, 20);
                LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                topInner.setLayoutParams(topParams);

                TextView nameView = new TextView(getContext());
                nameView.setText(friendName);
                nameView.setTextSize(18);
                nameView.setTextColor(getResources().getColor(R.color.input_text));
                nameView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                nameView.setLayoutParams(leftParams);

                TextView amtView = new TextView(getContext());
                amtView.setText("₹" + String.format("%.2f", entry.getValue()));
                amtView.setTextSize(17);
                amtView.setTextColor(getResources().getColor(R.color.input_text));
                amtView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

                topInner.addView(nameView);
                topInner.addView(amtView);

                // DIVIDER
                View divider = new View(getContext());
                divider.setBackgroundColor(getResources().getColor(R.color.divider));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 2);
                dividerParams.setMargins(0,16,0,16);
                divider.setLayoutParams(dividerParams);

                // BOTTOM INNER BOX
                LinearLayout botInner = new LinearLayout(getContext());
                botInner.setOrientation(LinearLayout.HORIZONTAL);
                botInner.setBackgroundResource(R.drawable.curved_light_box);
                botInner.setPadding(24, 20, 24, 20);
                LinearLayout.LayoutParams botParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                botInner.setLayoutParams(botParams);

                EditText inputAmt = new EditText(getContext());
                inputAmt.setHint("Enter Amount");
                inputAmt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                inputAmt.setBackgroundResource(R.drawable.curved_box);
                inputAmt.setTextColor(getResources().getColor(R.color.input_text));
                inputAmt.setHintTextColor(getResources().getColor(R.color.grey_hint));
                inputAmt.setTextSize(16);
                inputAmt.setPadding(14, 10, 14, 10);
                LinearLayout.LayoutParams inputAmtParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                inputAmt.setLayoutParams(inputAmtParams);

                Button addAmtBtn = new Button(getContext());
                addAmtBtn.setText("ADD\nAMOUNT");
                addAmtBtn.setTextColor(getResources().getColor(R.color.input_text));
                addAmtBtn.setTextSize(14);
                addAmtBtn.setBackgroundResource(R.drawable.curved_orange_button);
                addAmtBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                addAmtBtn.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btnParams.setMargins(24, 0, 0, 0);
                addAmtBtn.setLayoutParams(btnParams);
                addAmtBtn.setPadding(32, 22, 32, 22);

                addAmtBtn.setOnClickListener(v -> {
                    String amtStr = inputAmt.getText().toString().trim();
                    if (!amtStr.isEmpty()) {
                        try {
                            double addVal = Double.parseDouble(amtStr);
                            contributions.put(friendName, entry.getValue() + addVal);
                            saveFriendsData();
                            refreshUI();
                        } catch (NumberFormatException ignored) { }
                    }
                });

                botInner.addView(inputAmt);
                botInner.addView(addAmtBtn);

                // BUILD CARD
                cardBox.addView(topInner);
                cardBox.addView(divider);
                cardBox.addView(botInner);

                friendsListLayout.addView(cardBox);
            }

            // Place ADD FRIEND and DELETE FRIEND buttons after the list
            if (btnAddFriend.getParent() != null) ((ViewGroup) btnAddFriend.getParent()).removeView(btnAddFriend);
            rootLayout.addView(btnAddFriend);
            if (btnDeleteFriend.getParent() != null) ((ViewGroup) btnDeleteFriend.getParent()).removeView(btnDeleteFriend);
            rootLayout.addView(btnDeleteFriend);
        }
    }

    private void saveFriendsData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
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
                    contributions.put(key, obj.getDouble(key));
                }
            } catch (Exception ignored) { }
        }
    }
}
package com.example.tripexpensecalculator.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FriendsFragment extends Fragment {
    private EditText inputName;
    private Button btnAddFriend, btnDeleteFriend;
    private LinearLayout friendsListLayout;
    private ViewGroup rootLayout;

    private static final Map<String, Double> contributions = new LinkedHashMap<>();
    private static final String PREFS_NAME = "TripExpensePrefs";
    private static final String FRIENDS_KEY = "FriendsList";

    public static Map<String, Double> getContributions() {
        return contributions;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        inputName = root.findViewById(R.id.inputName);
        btnAddFriend = root.findViewById(R.id.btnAddFriend);
        btnDeleteFriend = root.findViewById(R.id.btnDeleteFriend);
        friendsListLayout = root.findViewById(R.id.friendsListLayout);
        rootLayout = (ViewGroup) root;

        btnAddFriend.setOnClickListener(v -> addFriend());
        btnDeleteFriend.setOnClickListener(v -> showDeleteFriendDialog());

        loadFriendsData();
        refreshUI();
        return root;
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

        contributions.put(name, 0.0);

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
                    // Collect selected names
                    StringBuilder sb = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < checkedItems.length; i++) {
                        if (checkedItems[i]) {
                            if (count > 0) sb.append(", ");
                            sb.append(friendNames[i]);
                            count++;
                        }
                    }
                    if (count == 0) return; // Nothing selected

                    String namesToDelete = sb.toString();

                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Delete Friend" + (count > 1 ? "s" : ""))
                            .setMessage("Are you sure you want to delete " + namesToDelete + "?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Delete", (d, w) -> {
                                // Remove all selected friends
                                for (int i = 0; i < checkedItems.length; i++)
                                    if (checkedItems[i])
                                        contributions.remove(friendNames[i]);
                                saveFriendsData();
                                refreshUI();
                                Toast.makeText(getContext(), count + " friend(s) deleted.", Toast.LENGTH_SHORT).show();
                            })
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshUI() {
        friendsListLayout.removeAllViews();

        // Remove ADD FRIEND and DELETE FRIEND from old spot if needed
        if (btnAddFriend.getParent() != null) ((ViewGroup) btnAddFriend.getParent()).removeView(btnAddFriend);
        if (btnDeleteFriend.getParent() != null) ((ViewGroup) btnDeleteFriend.getParent()).removeView(btnDeleteFriend);

        if (contributions.isEmpty()) {
            int inputNameIndex = rootLayout.indexOfChild(inputName);
            rootLayout.addView(btnAddFriend, inputNameIndex + 1);
            rootLayout.addView(btnDeleteFriend, inputNameIndex + 2);
        } else {
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                final String friendName = entry.getKey();

                // OUTER CARD
                LinearLayout cardBox = new LinearLayout(getContext());
                cardBox.setOrientation(LinearLayout.VERTICAL);
                cardBox.setBackgroundResource(R.drawable.curved_box_with_border);
                cardBox.setPadding(20, 28, 20, 28);

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 32);
                cardBox.setLayoutParams(cardParams);

                // TOP INNER BOX
                LinearLayout topInner = new LinearLayout(getContext());
                topInner.setOrientation(LinearLayout.HORIZONTAL);
                topInner.setBackgroundResource(R.drawable.curved_light_box);
                topInner.setPadding(24, 20, 24, 20);
                LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                topInner.setLayoutParams(topParams);

                TextView nameView = new TextView(getContext());
                nameView.setText(friendName);
                nameView.setTextSize(18);
                nameView.setTextColor(getResources().getColor(R.color.input_text));
                nameView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                nameView.setLayoutParams(leftParams);

                TextView amtView = new TextView(getContext());
                amtView.setText("₹" + String.format("%.2f", entry.getValue()));
                amtView.setTextSize(17);
                amtView.setTextColor(getResources().getColor(R.color.input_text));
                amtView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

                topInner.addView(nameView);
                topInner.addView(amtView);

                // DIVIDER
                View divider = new View(getContext());
                divider.setBackgroundColor(getResources().getColor(R.color.divider));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 2);
                dividerParams.setMargins(0,16,0,16);
                divider.setLayoutParams(dividerParams);

                // BOTTOM INNER BOX
                LinearLayout botInner = new LinearLayout(getContext());
                botInner.setOrientation(LinearLayout.HORIZONTAL);
                botInner.setBackgroundResource(R.drawable.curved_light_box);
                botInner.setPadding(24, 20, 24, 20);
                LinearLayout.LayoutParams botParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                botInner.setLayoutParams(botParams);

                EditText inputAmt = new EditText(getContext());
                inputAmt.setHint("Enter Amount");
                inputAmt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                inputAmt.setBackgroundResource(R.drawable.curved_box);
                inputAmt.setTextColor(getResources().getColor(R.color.input_text));
                inputAmt.setHintTextColor(getResources().getColor(R.color.grey_hint));
                inputAmt.setTextSize(16);
                inputAmt.setPadding(14, 10, 14, 10);
                LinearLayout.LayoutParams inputAmtParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                inputAmt.setLayoutParams(inputAmtParams);

                Button addAmtBtn = new Button(getContext());
                addAmtBtn.setText("ADD\nAMOUNT");
                addAmtBtn.setTextColor(getResources().getColor(R.color.input_text));
                addAmtBtn.setTextSize(14);
                addAmtBtn.setBackgroundResource(R.drawable.curved_orange_button);
                addAmtBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                addAmtBtn.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btnParams.setMargins(24, 0, 0, 0);
                addAmtBtn.setLayoutParams(btnParams);
                addAmtBtn.setPadding(32, 22, 32, 22);

                addAmtBtn.setOnClickListener(v -> {
                    String amtStr = inputAmt.getText().toString().trim();
                    if (!amtStr.isEmpty()) {
                        try {
                            double addVal = Double.parseDouble(amtStr);
                            contributions.put(friendName, entry.getValue() + addVal);
                            saveFriendsData();
                            refreshUI();
                        } catch (NumberFormatException ignored) { }
                    }
                });

                botInner.addView(inputAmt);
                botInner.addView(addAmtBtn);

                // BUILD CARD
                cardBox.addView(topInner);
                cardBox.addView(divider);
                cardBox.addView(botInner);

                friendsListLayout.addView(cardBox);
            }

            // Place ADD FRIEND and DELETE FRIEND buttons after the list
            if (btnAddFriend.getParent() != null) ((ViewGroup) btnAddFriend.getParent()).removeView(btnAddFriend);
            rootLayout.addView(btnAddFriend);
            if (btnDeleteFriend.getParent() != null) ((ViewGroup) btnDeleteFriend.getParent()).removeView(btnDeleteFriend);
            rootLayout.addView(btnDeleteFriend);
        }
    }

    private void saveFriendsData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
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
                    contributions.put(key, obj.getDouble(key));
                }
            } catch (Exception ignored) { }
        }
    }
}
