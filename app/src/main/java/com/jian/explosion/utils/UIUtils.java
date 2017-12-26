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

package com.jian.explosion.utils;

import android.content.res.Resources;

/**
 * 说明：
 * 作者：杨健
 * 时间：2017/12/26.
 */

public class UIUtils {

    public static int dp2px(double dpi) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dpi + 0.5f);
    }

    public static int statusBarHeignth() {
        return dp2px(25);
    }
}
