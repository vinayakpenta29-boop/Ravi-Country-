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
import androidx.fragment.app.Fragment;

import com.example.tripexpensecalculator.R;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FriendsFragment extends Fragment {
    private EditText inputName;
    private Button btnAddFriend;
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
        friendsListLayout = root.findViewById(R.id.friendsListLayout);
        rootLayout = (ViewGroup) root;

        btnAddFriend.setOnClickListener(v -> addFriend());

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

    // Dynamically manages the ADD FRIEND btn location and friend cards
    private void refreshUI() {
        friendsListLayout.removeAllViews();

        // Remove ADD FRIEND from old spot if needed
        if (btnAddFriend.getParent() != null) ((ViewGroup)btnAddFriend.getParent()).removeView(btnAddFriend);

        if (contributions.isEmpty()) {
            // Show after the name field (i.e., at the top)
            int inputNameIndex = rootLayout.indexOfChild(inputName);
            rootLayout.addView(btnAddFriend, inputNameIndex + 1);
        } else {
            // Show all friends as cards
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                final String friendName = entry.getKey();

                // Outer card (curved background) for this friend
                LinearLayout cardBox = new LinearLayout(getContext());
                cardBox.setOrientation(LinearLayout.VERTICAL);
                cardBox.setBackgroundResource(R.drawable.curved_box);
                cardBox.setPadding(16, 20, 16, 20);

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 24);
                cardBox.setLayoutParams(cardParams);

                // Row 1: Name && Amount
                LinearLayout row1 = new LinearLayout(getContext());
                row1.setOrientation(LinearLayout.HORIZONTAL);

                TextView nameView = new TextView(getContext());
                nameView.setText(friendName);
                nameView.setTextSize(18);
                nameView.setTextColor(getResources().getColor(R.color.input_text));
                nameView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                nameView.setLayoutParams(leftParams);

                TextView amtView = new TextView(getContext());
                amtView.setText("₹" + String.format("%.2f", entry.getValue()));
                amtView.setTextSize(18);
                amtView.setTextColor(getResources().getColor(R.color.input_text));
                amtView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

                row1.addView(nameView);
                row1.addView(amtView);

                // Row 2: Enter Amount + Small Add Amount
                LinearLayout row2 = new LinearLayout(getContext());
                row2.setOrientation(LinearLayout.HORIZONTAL);
                row2.setPadding(0, 16, 0, 0);

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
                addAmtBtn.setText("ADD AMOUNT");
                addAmtBtn.setTextColor(getResources().getColor(R.color.input_text));
                addAmtBtn.setTextSize(14);
                addAmtBtn.setBackgroundResource(R.drawable.curved_orange_button);
                addAmtBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btnParams.setMargins(24, 0, 0, 0);
                addAmtBtn.setLayoutParams(btnParams);
                addAmtBtn.setPadding(22, 10, 22, 10);

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

                row2.addView(inputAmt);
                row2.addView(addAmtBtn);

                cardBox.addView(row1);
                cardBox.addView(row2);

                friendsListLayout.addView(cardBox);
            }

            // Show ADD FRIEND button below the last friend card
            if (btnAddFriend.getParent() != null) ((ViewGroup)btnAddFriend.getParent()).removeView(btnAddFriend);
            rootLayout.addView(btnAddFriend);
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
