package com.example.tripexpensecalculator.fragments;

import android.content.Context;
import android.content.DialogInterface;
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
            row.setPadding(0, 0, 0, 8);

            TextView tv = new TextView(getContext());
            tv.setText(friendName + ": ₹" + String.format("%.2f", entry.getValue()));
            tv.setTextSize(16);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tv);

            Button editBtn = new Button(getContext());
            editBtn.setText("Edit");
            editBtn.setTextSize(14);
            editBtn.setOnClickListener(v -> showEditFriendDialog(friendName, entry.getValue()));
            row.addView(editBtn);

            Button deleteBtn = new Button(getContext());
            deleteBtn.setText("Delete");
            deleteBtn.setTextSize(14);
            deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Friend")
                    .setMessage("Are you sure you want to delete " + friendName + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        contributions.remove(friendName);
                        saveFriendsData();
                        refreshFriendsList();
                        Toast.makeText(getContext(), "Deleted " + friendName, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
            row.addView(deleteBtn);

            friendsListLayout.addView(row);
        }
    }

    private void showEditFriendDialog(String name, double oldValue) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit " + name + "'s Contribution");

        final EditText editInput = new EditText(requireContext());
        editInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editInput.setText(String.format("%.2f", oldValue));
        builder.setView(editInput);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String input = editInput.getText().toString().trim();
            if (!input.isEmpty()) {
                try {
                    double newValue = Double.parseDouble(input);
                    contributions.put(name, newValue);
                    saveFriendsData();
                    refreshFriendsList();
                    Toast.makeText(getContext(), "Updated " + name, Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid amount.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void saveFriendsData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, Double> entry : contributions.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) { }
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
            } catch (Exception e) { }
        }
    }
}
