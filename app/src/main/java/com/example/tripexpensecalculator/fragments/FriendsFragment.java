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
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.90f);
        nameParams.setMargins(0,0,8,0);
        nameBox.setLayoutParams(nameParams);
        row.addView(nameBox);

        // Amount Entry
        EditText inputAmt = new EditText(getContext());
        inputAmt.setHint("Enter Amount");
        inputAmt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputAmt.setBackgroundResource(R.drawable.curved_box);
        inputAmt.setTextColor(getResources().getColor(R.color.input_text));
        inputAmt.setHintTextColor(getResources().getColor(R.color.grey_hint));
        inputAmt.setTextSize(16);
        inputAmt.setPadding(18, 8, 18, 8);
        LinearLayout.LayoutParams inputAmtParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        inputAmtParams.setMargins(0,0,8,0);
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
        addAmtBtn.setPadding(22,10,22,10);
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
