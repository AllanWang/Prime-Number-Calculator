/*
 * Copyright (C) 2011 Sergey Margaritov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pitchedapps.primenumbercalculator;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class CalculatorThemesFragment extends PreferenceFragment {

    SharedPreferences themes;
    // TODO check this
    // http://stackoverflow.com/questions/26819429/cannot-start-this-animator-on-a-detached-view-reveal-effect

    private static final String GETX = "0";
    private static final String GETY = "0";
    private int x = 0;
    private int y = 0;


    public static CalculatorThemesFragment newInstance(int x, int y) {
        CalculatorThemesFragment themesFragment = new CalculatorThemesFragment();
        Bundle args = new Bundle();

        args.putInt(GETX, x);
        args.putInt(GETY, y);

        themesFragment.setArguments(args);
        return themesFragment;
    }

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.themes);
        getActivity().setTheme(R.style.ThemesSection);


        x = getArguments().getInt(GETX);
        y = getArguments().getInt(GETY);

        ((ColorPickerPreference) findPreference("theme_display")).setTitle("hello");
//        ((ColorPickerPreference) findPreference("color2")).setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
//
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                preference.setSummary(ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue))));
//                return true;
//            }
//
//        });
//        ((ColorPickerPreference) findPreference("color2")).setAlphaSliderEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout v = (LinearLayout) super.onCreateView(inflater, container, savedInstanceState);

        Button btn = new Button(getActivity().getApplicationContext());
        btn.setText("Reboot Now");

        btn.setTextColor(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt("theme_advanced_numpad_text", 0xFF000000));
        btn.setBackgroundColor(PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()).getInt("theme_advanced_numpad", 0xFF1DE9B6));
//        btn.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));

        return v;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = super.onCreateView(inflater, container, savedInstanceState);
//        final View rootLayout = view;
//
//        if (savedInstanceState == null) {
//            rootLayout.setVisibility(View.INVISIBLE);
//            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//                @Override
//                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                    ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
//                    if(viewTreeObserver.isAlive())
//                    Log.d("PNC: ", "viewTree is alive");
//                    {
//                        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                            @Override
//                            public void onGlobalLayout() {
//                                enterReveal(rootLayout);
//                            }
//                        });
//                    }
//                }
//            });
//        }
//
//        return view;
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.pad_advanced_background_color_2));
        getView().setClickable(true);
    }

//    @Override
//    public Animator onCreateAnimator (int transit, boolean enter, int nextAnim) {
//        Animator anim;
//        View view = getView();
//        if (enter) {
//            anim = enterReveal(view);
//        } else {
//            anim = exitReveal(view);
//        };
//
//        return anim;
//    }


    Animator enterReveal(View view) {
        // previously invisible view
        final View myView = view;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, x, y, 0, finalRadius).setDuration(600);

        return anim;
    }

    Animator exitReveal(View view) {
        // previously visible view
        final View myView = view;

        // get the initial radius for the clipping circle
        int initialRadius = Math.max(myView.getWidth(), myView.getHeight());

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, x, y, initialRadius, 0).setDuration(600);

        return anim;
    }
}