package com.example.tripexpensecalculator.fragments;

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

import java.util.LinkedHashMap;
import java.util.Map;

public class FriendsFragment extends Fragment {

    private EditText inputName, inputContribution;
    private Button btnAddFriend;
    private LinearLayout friendsListLayout;

    // Shared data store (simplest approach)
    private static final Map<String, Double> contributions = new LinkedHashMap<>();

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

        contributions.put(name, contributions.getOrDefault(name, 0.0) + contribution);

        inputName.setText("");
        inputContribution.setText("");
        refreshFriendsList();
        Toast.makeText(getContext(), "Friend added/updated.", Toast.LENGTH_SHORT).show();
    }

    private void refreshFriendsList() {
        friendsListLayout.removeAllViews();
        for (Map.Entry<String, Double> entry : contributions.entrySet()) {
            TextView tv = new TextView(getContext());
            tv.setText(entry.getKey() + ": ₹" + String.format("%.2f", entry.getValue()));
            friendsListLayout.addView(tv);
        }
    }
}
