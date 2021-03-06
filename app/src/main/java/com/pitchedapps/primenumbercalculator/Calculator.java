/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pitchedapps.primenumbercalculator;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroupOverlay;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pitchedapps.library.everything.BasicFunctions;
import com.pitchedapps.library.everything.cardlist.LoERecyclerView;
import com.pitchedapps.primenumbercalculator.CalculatorEditText.OnTextSizeChangeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.sufficientlysecure.donations.google.util.IabHelper;
import org.sufficientlysecure.donations.google.util.IabResult;
import org.sufficientlysecure.donations.google.util.Inventory;

import java.io.IOException;
import java.util.ArrayList;

public class Calculator extends FragmentActivity
        implements OnTextSizeChangeListener, OnLongClickListener {

    private static final String NAME = Calculator.class.getName();
    private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";
    public boolean mIsPremium = false;
    private boolean onTheme = false;
    private boolean subSetting = false;
    private String[] mGoogleCatalog;
    public static ArrayList<Long> list = new ArrayList<Long>();
    private int x = 0;
    private int y = 0;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    IabHelper mHelper;

    /**
     * Google
     */
    private static final String GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjL6lrdziw1KllwK2u7r8/zUlLZZxcg2a1/1RrP73yAzpl3yIkh3gdCPpvjwKb1MqB6sRVp8ziwpsGwFFaX4ECvDXz16rZFgz55V43MyFBHDXO4dnCUvuA58kY8YcZF+I1zyk4sJmqnXU+qmx+KeUpUR37+flM2/shAEUiaWW4wXam36VRezGtPcgQaPGop2AhbNNemOoi6QtynCO0Kj8t2RTUcAcHDqGgMy7iZnj6hIbIeawiok5EIQAfrNt0WnCDlgzoYrT6uudGaUobaZh1vnOIV2r6w8hLekVLjng5dFHYaDdwQAU+YfeE7oFr9mAlKhxiTdVsdoDpKLd9HLrqwIDAQAB";
    private static final String[] GOOGLE_CATALOG_FREE = new String[]{"prime.donation.1",
            "prime.donation.2", "prime.donation.3", "prime.donation.5", "prime.donation.10"};
    private static final String[] GOOGLE_CATALOG_PRO = new String[]{"prime.donation.consumable.1",
            "prime.donation.consumable.2", "prime.donation.consumable.3", "prime.donation.consumable.5", "prime.donation.consumable.10"};

    /**
     * PayPal
     */
    private static final String PAYPAL_USER = "pitchedapps@gmail.com";
    private static final String PAYPAL_CURRENCY_CODE = "CAD";

    // instance state keys
    private static final String KEY_CURRENT_STATE = NAME + "_currentState";
    private static final String KEY_CURRENT_EXPRESSION = NAME + "_currentExpression";

    // for theming
    int themeDisplay;
    int themeDisplayText;
    int themeDisplayInput;
    int themeDisplayResult;
    int themeClearAccent;
    int themeNumpad;
    int themeNumpadText;
    int themeAdvancedNumpad;
    int themeAdvancedNumpadText;


    private enum CalculatorState {
        INPUT, EVALUATE, RESULT
    }

    private final TextWatcher mInputTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            setState(CalculatorState.INPUT);
        }
    };

    private final OnKeyListener mInputOnKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_NUMPAD_ENTER:
                case KeyEvent.KEYCODE_ENTER:
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        try {
                            onEquals();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    // ignore all other actions
                    return true;
            }
            return false;
        }
    };

    private final Editable.Factory mInputEditableFactory = new Editable.Factory() {
        @Override
        public Editable newEditable(CharSequence source) {
            final boolean isEdited = mCurrentState == CalculatorState.INPUT;
            return new CalculatorExpressionBuilder(source, isEdited);
        }
    };

    private CalculatorState mCurrentState;

    private View mDisplayView;
    private CalculatorEditText mInputEditText;
    private CalculatorEditText mResultEditText;
    private ViewPager mPadViewPager;
    private View mDeleteButton;
    private View mClearButton;
    private TextView mHelpVersionName;

    private Animator mCurrentAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        prefs = getSharedPreferences("prime",
                MODE_PRIVATE);
        editor = getSharedPreferences("prime",
                MODE_PRIVATE).edit();

        mDisplayView = findViewById(R.id.display);
        mInputEditText = (CalculatorEditText) findViewById(R.id.input);
        mResultEditText = (CalculatorEditText) findViewById(R.id.result);
        mPadViewPager = (ViewPager) findViewById(R.id.pad_pager);
        mDeleteButton = findViewById(R.id.del);
        mClearButton = findViewById(R.id.clr);
        mHelpVersionName = (TextView) findViewById(R.id.help_version_number);

        savedInstanceState = savedInstanceState == null ? Bundle.EMPTY : savedInstanceState;
        setState(CalculatorState.values()[
                savedInstanceState.getInt(KEY_CURRENT_STATE, CalculatorState.INPUT.ordinal())]);

        PackageInfo appInfo = null;
        try {
            appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        assert appInfo != null;
        mHelpVersionName.setText("v" + appInfo.versionName);

        mInputEditText.setText(savedInstanceState.getString(KEY_CURRENT_EXPRESSION, ""));
        mInputEditText.setEditableFactory(mInputEditableFactory);
        mInputEditText.addTextChangedListener(mInputTextWatcher);
        mInputEditText.setOnKeyListener(mInputOnKeyListener);
        mInputEditText.setOnTextSizeChangeListener(this);
        mDeleteButton.setOnLongClickListener(this);

        themeEngine();

        //Setup donations
        final IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

                if (inventory != null) {
                    if (inventory.hasPurchase("prime.donation.1") ||
                            inventory.hasPurchase("prime.donation.2") ||
                            inventory.hasPurchase("prime.donation.3") ||
                            inventory.hasPurchase("prime.donation.5") ||
                            inventory.hasPurchase("prime.donation.10")) {
                        Log.d("PNC: ", "IAP inventory contains a donation");

                        mIsPremium = true;
                    }
                }
                if (isPremium()) {
                    mGoogleCatalog = GOOGLE_CATALOG_PRO;
                }
            }
        };

        if (isStoreVersion()) {
            mHelper = new IabHelper(Calculator.this, GOOGLE_PUBKEY);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result)
                {
                    if (!result.isSuccess()) {
                        Log.d("PNC: ", "In-app Billing setup failed: " + result);
                    } else {
                        mHelper.queryInventoryAsync(false, mGotInventoryListener);
                    }

                }
            }) ;
        }

        initiateCredits();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        // If there's an animation in progress, end it immediately to ensure the state is
        // up-to-date before it is serialized.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.end();
        }

        super.onSaveInstanceState(outState);

        outState.putInt(KEY_CURRENT_STATE, mCurrentState.ordinal());
        outState.putString(KEY_CURRENT_EXPRESSION,
                mInputEditText.getText().toString());
    }

    private void setState(CalculatorState state) { //keep this
        if (mCurrentState != state) {
            mCurrentState = state;

            if (state == CalculatorState.RESULT) {
                mDeleteButton.setVisibility(View.GONE);
                mClearButton.setVisibility(View.VISIBLE);
            } else {
                mDeleteButton.setVisibility(View.VISIBLE);
                mClearButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        backToSettings();
        if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first pad (or the pad is not paged),
            // allow the system to handle the Back button.
            super.onBackPressed();
        } else if (subSetting == false) { // only run if current view is not a subSetting
            // Otherwise, select the previous pad.
            mPadViewPager.setCurrentItem(mPadViewPager.getCurrentItem() - 1);
        }

        subSetting = false; //make boolean back to original
    }

    public void onBackPressed(View v) {
        onBackPressed();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();

        // If there's an animation in progress, end it immediately to ensure the state is
        // up-to-date before the pending user interaction is handled.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.end();
        }

        if (mPadViewPager.getCurrentItem() != 0) {

            addOnTouchListener(findViewById(R.id.advanced_themes));
            addOnTouchListener(findViewById(R.id.advanced_help));
            addOnTouchListener(findViewById(R.id.advanced_donate));
            addOnTouchListener(findViewById(R.id.advanced_credits));
            addOnTouchListenerText(findViewById(R.id.back_help));
            addOnTouchListenerText(findViewById(R.id.back_donate));
            addOnTouchListenerText(findViewById(R.id.back_credits));

        }

        if (mPadViewPager == null || mPadViewPager.getCurrentItem() == 0) {
            backToSettings();
            subSetting = false; //make boolean back to original
        }
    }

    public void onButtonClick(View view) throws IOException, ClassNotFoundException {
        BasicFunctions basic = new BasicFunctions(this);
        switch (view.getId()) {
            case R.id.eq:
                onEquals();
                break;
            case R.id.del:
                onDelete();
                break;
            case R.id.clr:
                onClear();
                break;
            case R.id.advanced_themes:
                onTheme();
                break;
            case R.id.advanced_clear_list:
                ArrayList<Long> empty = new ArrayList<>();
                saveList("Prime", empty);
                Toast.makeText(getApplicationContext(),"Stored prime number list cleared!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.advanced_help:
                afterAdvancedPad(findViewById(R.id.help));
                break;
            case R.id.advanced_contact_me:
                basic.email();
                break;
            case R.id.advanced_rate_app:
                basic.rate();
                break;
            case R.id.advanced_share_app:
                basic.share();
                break;
            case R.id.advanced_about_dev:
                Toast.makeText(getApplicationContext(),"WIP", Toast.LENGTH_SHORT).show();
                break;
            case R.id.advanced_other_apps:
                Intent devPlay = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dev_play_store)));
                startActivity(devPlay);
                break;
            case R.id.advanced_google_plus:
                Intent gPlus = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dev_gplus_link)));
                startActivity(gPlus);
                break;
            case R.id.advanced_source:
                Intent github = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_github)));
                startActivity(github);
                break;
            case R.id.advanced_credits:
                onCredits();
                break;
            case R.id.advanced_donate:
                onDonate();
                break;
            default:
                mInputEditText.append(((Button) view).getText());
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if (view.getId() == R.id.del) {
            onClear();
            return true;
        }
        return false;
    }

    @Override
    public void onTextSizeChanged(final TextView textView, float oldSize) {
        if (mCurrentState != CalculatorState.INPUT) {
            // Only animate text changes that occur from user input.
            return;
        }

        // Calculate the values needed to perform the scale and translation animations,
        // maintaining the same apparent baseline for the displayed text.
        final float textScale = oldSize / textView.getTextSize();
        final float translationX = (1.0f - textScale) *
                (textView.getWidth() / 2.0f - textView.getPaddingEnd());
        final float translationY = (1.0f - textScale) *
                (textView.getHeight() / 2.0f - textView.getPaddingBottom());

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(textView, View.SCALE_X, textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, View.SCALE_Y, textScale, 1.0f),
                ObjectAnimator.ofFloat(textView, View.TRANSLATION_X, translationX, 0.0f),
                ObjectAnimator.ofFloat(textView, View.TRANSLATION_Y, translationY, 0.0f));
        animatorSet.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    private void onEquals() throws IOException, ClassNotFoundException {
        if (mCurrentState == CalculatorState.INPUT) {
            setState(CalculatorState.EVALUATE);

            list = getList("prime");

            onResult(CalculatorPrimeNumber.primeNumberCalculator(Long.parseLong(mInputEditText.getText().toString())));

            saveList("prime", list);

        }
    }

    private void onDelete() {
        // Delete works like backspace; remove the last character from the expression.
        final Editable inputText = mInputEditText.getEditableText();
        final int inputLength = inputText.length();
        if (inputLength > 0) {
            inputText.delete(inputLength - 1, inputLength);
        }
    }

    private void reveal(View sourceView, AnimatorListener listener) {
        final ViewGroupOverlay groupOverlay =
                (ViewGroupOverlay) getWindow().getDecorView().getOverlay();

        final Rect displayRect = new Rect();
        mDisplayView.getGlobalVisibleRect(displayRect);

        // Make reveal cover the display and status bar.
        final View revealView = new View(this);
        revealView.setBottom(displayRect.bottom);
        revealView.setLeft(displayRect.left);
        revealView.setRight(displayRect.right);
        revealView.setBackgroundColor(themeClearAccent);
        groupOverlay.add(revealView);

        final int[] clearLocation = new int[2];
        sourceView.getLocationInWindow(clearLocation);
        clearLocation[0] += sourceView.getWidth() / 2;
        clearLocation[1] += sourceView.getHeight() / 2;

        final int revealCenterX = clearLocation[0] - revealView.getLeft();
        final int revealCenterY = clearLocation[1] - revealView.getTop();

        final double x1_2 = Math.pow(revealView.getLeft() - revealCenterX, 2);
        final double x2_2 = Math.pow(revealView.getRight() - revealCenterX, 2);
        final double y_2 = Math.pow(revealView.getTop() - revealCenterY, 2);
        final float revealRadius = (float) Math.max(Math.sqrt(x1_2 + y_2), Math.sqrt(x2_2 + y_2));

        final Animator revealAnimator =
                ViewAnimationUtils.createCircularReveal(revealView,
                        revealCenterX, revealCenterY, 0.0f, revealRadius);
        revealAnimator.setDuration(
                getResources().getInteger(android.R.integer.config_longAnimTime));

        final Animator alphaAnimator = ObjectAnimator.ofFloat(revealView, View.ALPHA, 0.0f);
        alphaAnimator.setDuration(
                getResources().getInteger(android.R.integer.config_mediumAnimTime));
        alphaAnimator.addListener(listener);

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(revealAnimator).before(alphaAnimator);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                groupOverlay.remove(revealView);
                mCurrentAnimator = null;
            }
        });

        mCurrentAnimator = animatorSet;
        animatorSet.start();
    }

    private void onClear() {
        if (TextUtils.isEmpty(mInputEditText.getText())) { //TODO check if necessary or not
            return;
        }

        final View sourceView = mClearButton.getVisibility() == View.VISIBLE
                ? mClearButton : mDeleteButton;
        reveal(sourceView, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mInputEditText.getEditableText().clear();
            }
        });
    }

    private void onResult(final String result) {
        // Calculate the values needed to perform the scale and translation animations,
        // accounting for how the scale will affect the final position of the text.
        final float resultScale =
                mInputEditText.getVariableTextSize(result) / mResultEditText.getTextSize();
        final float resultTranslationX = (1.0f - resultScale) *
                (mResultEditText.getWidth() / 2.0f - mResultEditText.getPaddingEnd());
        final float resultTranslationY = (1.0f - resultScale) * //TODO delete unnecessary lines for animation
                (mResultEditText.getHeight() / 2.0f - mResultEditText.getPaddingBottom()) +
                (mInputEditText.getBottom() - mResultEditText.getBottom()) +
                (mResultEditText.getPaddingBottom() - mInputEditText.getPaddingBottom());
        final float inputTranslationY = -mInputEditText.getBottom();

        // Use a value animator to fade to the final text color over the course of the animation.
        final int resultTextColor = mResultEditText.getCurrentTextColor();
        final int inputEditText = mInputEditText.getCurrentTextColor();
        final ValueAnimator textColorAnimator =
                ValueAnimator.ofObject(new ArgbEvaluator(), resultTextColor, inputEditText);
        textColorAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mResultEditText.setTextColor((int) valueAnimator.getAnimatedValue());
            }
        });

        final AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                textColorAnimator,
                ObjectAnimator.ofFloat(mResultEditText, View.SCALE_X, resultScale),
                ObjectAnimator.ofFloat(mResultEditText, View.SCALE_Y, resultScale),
                ObjectAnimator.ofFloat(mResultEditText, View.TRANSLATION_X, resultTranslationX),
                ObjectAnimator.ofFloat(mResultEditText, View.TRANSLATION_Y, resultTranslationY),
                ObjectAnimator.ofFloat(mInputEditText, View.TRANSLATION_Y, inputTranslationY));
        animatorSet.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mResultEditText.setText(result);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Reset all of the values modified during the animation.
                mResultEditText.setTextColor(resultTextColor);
                mResultEditText.setScaleX(1.0f);
                mResultEditText.setScaleY(1.0f);
                mResultEditText.setTranslationX(0.0f);
                mResultEditText.setTranslationY(0.0f);
                mInputEditText.setTranslationY(0.0f);

                // Finally update the input to use the current result.
                mInputEditText.setText(result); //TODO figure out how to reset after equal sign without changing input text
                mResultEditText.getEditableText().clear();
                setState(CalculatorState.RESULT);

                mCurrentAnimator = null;
            }

        });

        mCurrentAnimator = animatorSet;
        animatorSet.start();
    }

//    for advanced options

    public void onTheme() {

        findViewById(R.id.pad_advanced).setVisibility(View.INVISIBLE);
        findViewById(R.id.pad_advanced).startAnimation(fadeOutAnimation());

        CalculatorThemesFragment themesFragment = CalculatorThemesFragment.newInstance(x, y);

//        View view = CalculatorThemesFragment.getView();
//        enterReveal(view);

        getFragmentManager().executePendingTransactions();
        getFragmentManager().beginTransaction()
                .replace(R.id.root_layout, themesFragment)
                .addToBackStack("theme")
                .commit();
        onTheme = true;
    }

    public void onDonate() {
        CalculatorDonationsFragment donationsFragment;
        if (isStoreVersion()) {
            donationsFragment = CalculatorDonationsFragment.newInstance(BuildConfig.DEBUG, true, GOOGLE_PUBKEY, mGoogleCatalog,
                    getResources().getStringArray(R.array.donation_google_catalog_values), true, PAYPAL_USER,
                    PAYPAL_CURRENCY_CODE, getString(R.string.donation_paypal_item));
        } else {
            donationsFragment = CalculatorDonationsFragment.newInstance(BuildConfig.DEBUG, false, null, null, null, true, PAYPAL_USER,
                    PAYPAL_CURRENCY_CODE, getString(R.string.donation_paypal_item));
        }

        afterAdvancedPad(findViewById(R.id.donations_fragment));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.pad_advanced, donationsFragment)
                .commit();
    }

    public void onCredits() {
        afterAdvancedPad(findViewById(R.id.advanced_credits_layout));
    }

    public void initiateCredits() {
        final LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        ((RecyclerView) findViewById(R.id.rv)).setLayoutManager(llm);

        findViewById(R.id.advanced_credits_layout).setVisibility(View.INVISIBLE);
        LoERecyclerView lrv = new LoERecyclerView(this);
        lrv.initialize(findViewById(R.id.rv));
        lrv.addLibCard("AOSP Calculator", "The official AOSP Calculator.", "android", "https://github.com/android/platform_packages_apps_calculator");
        lrv.addLibCard("Android Donations Lib", "Android Donations Lib supports donations by Google Play Store, Flattr, PayPal, and Bitcoin.", "Sufficiently Secure", "https://github.com/SufficientlySecure/donations");
        lrv.addLibCard("Circular Image View", "A fast circular ImageView perfect for profile images.", "hdodenhof", "https://github.com/hdodenhof/CircleImageView");
        lrv.addLibCard("The Library of Everything", "Bits and pieces to make adding small features easier.", "asdfasdfvful", "https://github.com/asdfasdfvful/The-Library-of-Everything");
        lrv.cardTheme(ColorUtils.setAlphaComponent(themeAdvancedNumpadText, 255), themeAdvancedNumpadText, themeAdvancedNumpadText, themeAdvancedNumpadText, "sans-serif-light");
        //TODO remove button theme from line above as new method is created
        lrv.finalize();
    }

//  fade animations
    public Animation fadeInAnimation() {
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInAnimation.setStartOffset(200);
        fadeInAnimation.setDuration(300);
        return fadeInAnimation;
    }

    public Animation fadeOutAnimation() {
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOutAnimation.setDuration(200);
        return fadeOutAnimation;
    }

//    onTouchListeners

    void addOnTouchListener(View view) {
        Button button = (Button) view;
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                x = (int) e.getX() + v.getLeft();
                y = (int) e.getY() + v.getTop();
                return false;
            }
        });
    }

    void addOnTouchListenerText(View view) {
        TextView text = (TextView) view;
        text.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                x = (int) e.getX() + v.getLeft();
                y = (int) e.getY() + v.getTop();
                return false;
            }
        });
    }

//    advanced pad transitions

    void afterAdvancedPad(View view) {
        enterReveal(view);
        findViewById(R.id.pad_advanced).setVisibility(View.INVISIBLE);
        findViewById(R.id.pad_advanced).startAnimation(fadeOutAnimation());
    }

    void backToAdvancedPad(View view) {
        exitReveal(view);
        findViewById(R.id.pad_advanced).setVisibility(View.VISIBLE);
        findViewById(R.id.pad_advanced).startAnimation(fadeInAnimation());
    }

//    reveal animations

    void enterReveal(View view) {
        // previously invisible view
        final View myView = view;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, x, y, 0, finalRadius).setDuration(600);

        // make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);
        anim.start();
    }

    void exitReveal(View view) {
        // previously visible view
        final View myView = view;

        // get the initial radius for the clipping circle
        int initialRadius = Math.max(myView.getWidth(), myView.getHeight());

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, x, y, initialRadius, 0).setDuration(600);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.start();
    }

//    for list retrieval

    public void saveList(String key, ArrayList<Long> list) {
        JSONArray jList = new JSONArray(list);
        editor.remove(key);
        editor.putString(key, jList.toString());
        editor.commit();
        Log.d("PNC: ", "List saved!");
    }

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
                Log.d("PNC: ", "List loaded.");
                return list;
            } catch (JSONException e) {
                return getDefaultArray();
            }
        }
    }

    // Get a default array in the event that there is no array
    // saved or a JSONException occurred
    private ArrayList<Long> getDefaultArray() {
        Log.d("PNC: ", "ArrayList not found; creating new one.");
        ArrayList<Long> array = new ArrayList<Long>();
        return array;
    }

    //checks if app is sideloaded or installed from a market
    public boolean isStoreVersion() {
        String installer = getPackageManager().getInstallerPackageName(getPackageName());
        try {
            if (installer.equals("com.google.android.feedback") || installer.equals("com.android.vending")) {
                return true;
            }
        } catch (Throwable e) {
        }

        return true;
    }

    public void backToSettings() {
        subSetting = true;
        if (onTheme) {

            findViewById(R.id.pad_advanced).setVisibility(View.VISIBLE);
            findViewById(R.id.pad_advanced).startAnimation(fadeInAnimation());
//            getFragmentManager().popBackStack("theme", R.id.root_layout);
            getFragmentManager().popBackStackImmediate(); //appears to working as opposed to the line above

            onTheme = false;
        } else if (findViewById(R.id.help).getVisibility() == View.VISIBLE){
            backToAdvancedPad(findViewById(R.id.help));
        } else if (findViewById(R.id.advanced_credits_layout).getVisibility() == View.VISIBLE){
            backToAdvancedPad(findViewById(R.id.advanced_credits_layout));
        } else if (findViewById(R.id.donations_fragment).getVisibility() == View.VISIBLE){
            backToAdvancedPad(findViewById(R.id.donations_fragment));
        } else {
            subSetting = false;
        }
    }

    public void themeEngine() {

        SharedPreferences themes = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        themeDisplay = themes.getInt("theme_display", 0xFFFFFFFF);
        themeDisplayText = themes.getInt("theme_display_text", 0xFF000000);
        themeDisplayInput = ColorUtils.setAlphaComponent(themeDisplayText, 138); //8A transparency
        themeDisplayResult = ColorUtils.setAlphaComponent(themeDisplayText, 108); //6C transparency
        themeClearAccent = themes.getInt("theme_clear_accent", 0xFF00BCD4);
        themeNumpad = themes.getInt("theme_numpad", 0xFF434343);
        themeNumpadText = themes.getInt("theme_numpad_text", 0xFFFFFFFF);
        themeAdvancedNumpad = themes.getInt("theme_advanced_numpad", 0xFF1DE9B6);
        themeAdvancedNumpadText = ColorUtils.setAlphaComponent(themes.getInt("theme_advanced_numpad_text", 0xFF000000), 145); //91 transparency
        //adds black "cache" color with alpha depending on themeNumpad
        mPadViewPager.setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(this, android.R.color.black), (new ColorDrawable(themeNumpad)).getAlpha()));

        mDisplayView.setBackgroundColor(themeDisplay);
        findViewById(R.id.pad_numeric).setBackgroundColor(themeNumpad);
        findViewById(R.id.root_layout).setBackgroundColor(themeAdvancedNumpad);
        mInputEditText.setTextColor(themeDisplayInput);
        mResultEditText.setTextColor(themeDisplayResult);
        getWindow().setStatusBarColor(themeClearAccent);

        //numpad section

        ((Button) findViewById(R.id.del)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.clr)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.eq)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_0)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_1)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_2)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_3)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_4)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_5)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_6)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_7)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_8)).setTextColor(themeNumpadText);
        ((Button) findViewById(R.id.digit_9)).setTextColor(themeNumpadText);

        //settings/advancedpad section

        ((Button) findViewById(R.id.advanced_themes)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_clear_list)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_help)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_contact_me)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_rate_app)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_share_app)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_about_dev)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_other_apps)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_google_plus)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_source)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_credits)).setTextColor(themeAdvancedNumpadText);
        ((Button) findViewById(R.id.advanced_donate)).setTextColor(themeAdvancedNumpadText);

        //donations theming done in donations fragment

        //help section

        ((TextView) findViewById(R.id.help_text)).setTextColor(themeAdvancedNumpadText);
        ((TextView) findViewById(R.id.help_version_number)).setTextColor(themeAdvancedNumpadText);
        ((TextView) findViewById(R.id.back_help)).setTextColor(themeAdvancedNumpadText);

        //credit back, rest are themed in the built in theme engine in the Library of Everything
        ((TextView) findViewById(R.id.back_credits)).setTextColor(themeAdvancedNumpadText);

    }

    public boolean isPremium() {
        return mIsPremium;
    }

}
