/*
 * Copyright (C) 2018 Jian Yang
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

package com.jian.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.lang.ref.WeakReference;

/**
 * 弹出Toast时,能立即改变内容,而不需要等待上个提示内容的消失
 */
public final class ToastUtil {
    private static WeakReference<Toast> toastR;

    private ToastUtil() {
    }

    public static void toast(Context context, String text) {
        if (TextUtils.isEmpty(text) || context == null) {
            return;
        }
        if (toastR == null || toastR.get() == null) {
            Toast toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
            toastR = new WeakReference<>(toast);
        }
        Toast toast = toastR.get();
        toast.setText(text);
        toast.show();
    }
}
