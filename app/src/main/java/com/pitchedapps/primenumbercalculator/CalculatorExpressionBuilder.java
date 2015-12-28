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

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;

public class CalculatorExpressionBuilder extends SpannableStringBuilder {

    private boolean mIsEdited;

    public CalculatorExpressionBuilder(
            CharSequence text, boolean isEdited) {
        super(text);

        mIsEdited = isEdited;
    }

    @Override
    public SpannableStringBuilder replace(int start, int end, CharSequence tb, int tbstart,
            int tbend) {
//        if (start != length() || end != length()) {
//            mIsEdited = true;
//            Log.d("Prime", "mIsEdited = true");
//            return super.replace(start, end, tb, tbstart, tbend);
//        }
        Log.d("Prime", "Starting SpannableStringBuilder");
        String appendExpr = tb.subSequence(tbstart, tbend).toString();
        if (appendExpr.length() == 1) {
//            final String expr = mTokenizer.getNormalizedExpression(toString());
            switch (appendExpr.charAt(0)) {
                case '0':
                    // don't allow leading operator
                    if (start == 0) {
                        appendExpr = "";
                        break;
                    }
                default:
                    break;
            }
        }

        // since this is the first edit replace the entire string
        if (!mIsEdited && appendExpr.length() > 0) {
            start = 0;
            Log.d("Prime", "starting new value");
            mIsEdited = true;
        }

        return super.replace(start, end, appendExpr, 0, appendExpr.length());
    }
}
