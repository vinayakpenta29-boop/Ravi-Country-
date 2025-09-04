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

    private EditText inputName, inputContribution;
    private Button btnAddFriend;
    private LinearLayout friendsListLayout;

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
        inputContribution = root.findViewById(R.id.inputContribution);
        btnAddFriend = root.findViewById(R.id.btnAddFriend);
        friendsListLayout = root.findViewById(R.id.friendsListLayout);

        btnAddFriend.setOnClickListener(v -> addFriend());

        loadFriendsData();
        refreshFriendsList();
        return root;
    }

    private void addFriend() {
        String name = inputName.getText().toString().trim();
        String contributionStr = inputContribution.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(getContext(), "Friend's name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(contributionStr)) {
            Toast.makeText(getContext(), "Contribution cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        double contribution;
        try {
            contribution = Double.parseDouble(contributionStr);
            if (contribution < 0) {
                Toast.makeText(getContext(), "Contribution cannot be negative.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid contribution amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        contributions.put(name, contribution);

        inputName.setText("");
        inputContribution.setText("");
        saveFriendsData();
        refreshFriendsList();
        Toast.makeText(getContext(), "Friend added/updated.", Toast.LENGTH_SHORT).show();
    }

    private void refreshFriendsList() {
        friendsListLayout.removeAllViews();
        for (Map.Entry<String, Double> entry : contributions.entrySet()) {
            final String friendName = entry.getKey();

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 0, 0, 16);

            // Curved box for friend name
            TextView nameBox = new TextView(getContext());
            nameBox.setText(friendName);
            nameBox.setTextSize(18);
            nameBox.setTextColor(getResources().getColor(R.color.input_text));
            nameBox.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            nameBox.setPadding(28, 12, 28, 12);
            nameBox.setBackgroundResource(R.drawable.curved_box);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.9f);
            nameParams.setMargins(0, 0, 8, 0);
            nameBox.setLayoutParams(nameParams);
            row.addView(nameBox);

            // Amount box showing current friend's contribution
            TextView amtBox = new TextView(getContext());
            amtBox.setText("₹" + String.format("%.2f", entry.getValue()));
            amtBox.setTextSize(16);
            amtBox.setTextColor(getResources().getColor(R.color.input_text));
            amtBox.setPadding(18, 8, 18, 8);
            amtBox.setBackgroundResource(R.drawable.curved_box);
            LinearLayout.LayoutParams amtParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f);
            amtParams.setMargins(0, 0, 8, 0);
            amtBox.setLayoutParams(amtParams);
            row.addView(amtBox);

            // Amount Entry (input box)
            EditText inputAmt = new EditText(getContext());
            inputAmt.setHint("Enter Amount");
            inputAmt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            inputAmt.setBackgroundResource(R.drawable.curved_box);
            inputAmt.setTextColor(getResources().getColor(R.color.input_text));
            inputAmt.setHintTextColor(getResources().getColor(R.color.grey_hint));
            inputAmt.setTextSize(16);
            inputAmt.setPadding(12, 8, 12, 8);
            LinearLayout.LayoutParams inputAmtParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            inputAmtParams.setMargins(0, 0, 8, 0);
            inputAmt.setLayoutParams(inputAmtParams);
            row.addView(inputAmt);

            // Add Amount Button
            Button addAmtBtn = new Button(getContext());
            addAmtBtn.setText("Add Amount");
            addAmtBtn.setTextColor(getResources().getColor(R.color.input_text));
            addAmtBtn.setTextSize(16);
            addAmtBtn.setBackgroundResource(R.drawable.curved_orange_button);
            addAmtBtn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            addAmtBtn.setLayoutParams(btnParams);
            addAmtBtn.setPadding(20, 8, 20, 8);
            addAmtBtn.setOnClickListener(v -> {
                String amtStr = inputAmt.getText().toString().trim();
                if (!amtStr.isEmpty()) {
                    try {
                        double addVal = Double.parseDouble(amtStr);
                        contributions.put(friendName, entry.getValue() + addVal);
                        saveFriendsData();
                        refreshFriendsList();
                    } catch (NumberFormatException ignored) { }
                }
            });
            row.addView(addAmtBtn);

            friendsListLayout.addView(row);
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
