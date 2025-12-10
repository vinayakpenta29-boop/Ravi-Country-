package com.example.tripexpensecalculator;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class TripManager {

    private static final String PREFS_NAME = "TripExpensePrefs";
    private static final String KEY_TRIPS_LIST = "TripsList";
    private static final String KEY_CURRENT_TRIP = "CurrentTrip";

    // ----- current trip -----

    public static String getCurrentTrip(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cur = prefs.getString(KEY_CURRENT_TRIP, null);
        if (cur == null) {
            cur = "Default Trip";
            setCurrentTrip(ctx, cur);
            addTripIfMissing(ctx, cur);
        }
        return cur;
    }

    public static void setCurrentTrip(Context ctx, String tripName) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENT_TRIP, tripName).apply();
        addTripIfMissing(ctx, tripName);
    }

    // ----- trips list -----

    public static List<String> getAllTrips(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TRIPS_LIST, "[]");
        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
        } catch (Exception ignored) { }
        return list;
    }

    public static void addTripIfMissing(Context ctx, String tripName) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TRIPS_LIST, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                if (tripName.equals(arr.getString(i))) {
                    return;
                }
            }
            arr.put(tripName);
            prefs.edit().putString(KEY_TRIPS_LIST, arr.toString()).apply();
        } catch (Exception ignored) { }
    }

    // ----- per‑trip keys for data -----

    public static String keyForFriends(String tripName) {
        return "trip_" + tripName + "_FriendsList";
    }

    public static String keyForExpenses(String tripName) {
        return "trip_" + tripName + "_ExpensesList";
    }
}
