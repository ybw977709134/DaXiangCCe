/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.daxiangce123.android.util;

import android.widget.TextView;

import java.util.Formatter;

/**
 * Class containing some static utility methods.
 */
public class TextUtils {

    public static void adjustSizeText(TextView textView, String leftString, String size) {
        if (size == null) {
            return;
        }
        int iSize = Integer.valueOf(size);
        adjustSizeText(textView, leftString, iSize);
    }


    public static void adjustSizeText(TextView textView, String leftString, int size) {
        if (textView == null || leftString == null) {
            return;
        }
        StringBuilder sb = new StringBuilder(leftString);
        sb.append(leftString);
        if (size >= 10000) {
            sb.append((new Formatter()).format("%.1f万", size / 10000f).toString());
        } else {
            sb.append(size);
        }
        textView.setText(sb.toString());
    }


}
