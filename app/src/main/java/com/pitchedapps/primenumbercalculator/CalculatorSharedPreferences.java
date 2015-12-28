package com.pitchedapps.primenumbercalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 7681 on 2015-12-27.
 */
public class CalculatorSharedPreferences {


    SharedPreferences sPrefs= PreferenceManager.getDefaultSharedPreferences(Calculator.context);
    SharedPreferences.Editor sEdit=sPrefs.edit();

    public void storePrime(Context context, ArrayList<Long> list) {
        for(int i=0;i<Calculator.list.size();i++)
        {
            sEdit.putLong("val" + i, Calculator.list.get(i));
        }
        sEdit.putInt("size",Calculator.list.size());
        sEdit.commit();
    }

    public ArrayList<Long> loadPrime(Context context) {
        int size = sPrefs.getInt("size",0);

        for(int j=0;j<size;j++) {
            Calculator.list.add(sPrefs.getLong("val" + j, 0));
        }
        return Calculator.list;
    }
    public void addPrime(Context context, ArrayList<Long> list) {
        
    }
}
