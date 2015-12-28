package com.pitchedapps.primenumbercalculator;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * Created by 7681 on 2015-12-27.
 */

public class CalculatorSharedPreferences {
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public CalculatorSharedPreferences(Context context) {
//        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();
    }

    /**
     * Converts the provided ArrayList<String>
     * into a JSONArray and saves it as a single
     * string in the apps shared preferences
     * @param String key Preference key for SharedPreferences
     * @param array ArrayList<String> containing the list items
     */
    public void saveList(String key, ArrayList<Long> list) {
        JSONArray jList = new JSONArray(list);
        editor.remove(key);
        editor.putString(key, jList.toString());
        editor.commit();
        Log.d("Prime", "List saved!");
    }

    /**
     * Loads a JSONArray from shared preferences
     * and converts it to an ArrayList<String>
     * @param String key Preference key for SharedPreferences
     * @return ArrayList<String> containing the saved values from the JSONArray
     */
    public ArrayList<Long> getList(String key) {
        ArrayList<Long> list = new ArrayList<Long>();
        String jArrayString = prefs.getString(key, "NOPREFSAVED");
        if (jArrayString.matches("NOPREFSAVED")) return getDefaultArray();
        else {
            try {
                JSONArray jArray = new JSONArray(jArrayString);
                for (int i = 0; i < jArray.length(); i++) {
                    list.add(jArray.getLong(i));
                }
                Log.d("Prime", "List loaded.");
                return list;
            } catch (JSONException e) {
                return getDefaultArray();
            }
        }
    }

    // Get a default array in the event that there is no array
    // saved or a JSONException occurred
    private ArrayList<Long> getDefaultArray() {
        Log.d("Prime", "ArrayList not found; creating new one.");
        ArrayList<Long> array = new ArrayList<Long>();
        return array;
    }
}