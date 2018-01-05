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

package com.jian.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 说明：RecycleView的分割线
 * 作者：杨健
 * 时间：2017/8/6.
 */
public class RecycleViewDivider extends RecyclerView.ItemDecoration {
    private int marginStart;
    private int marginEnd;

    private Drawable mDivider;
    //分割线高度，默认为1px
    private int mDividerSize = 2;
    //列表的方向：LinearLayoutManager.VERTICAL或LinearLayoutManager.HORIZONTAL
    private int mOrientation;
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    /**
     * 自定义分割线
     *
     * @param drawableId 分割线图片
     */
    public RecycleViewDivider(Context context, int orientation,
                              @DrawableRes int drawableId, int marginStart, int marginEnd) {
        if (orientation != LinearLayout.VERTICAL && orientation != LinearLayout.HORIZONTAL) {
            throw new IllegalArgumentException("请输入正确的参数！");
        }
        mOrientation = orientation;
        mDivider = ContextCompat.getDrawable(context, drawableId);
        mDividerSize = mDivider.getIntrinsicHeight();
        this.marginStart = marginStart;
        this.marginEnd = marginEnd;
    }

    //获取分割线尺寸
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mOrientation == LinearLayout.VERTICAL) {
            outRect.set(0, 0, 0, mDividerSize);
        } else {
            outRect.set(0, 0, mDividerSize, 0);
        }
    }

    //绘制分割线
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            mDivider.setBounds(getDividerBounds(view));
            mDivider.draw(c);
        }
    }

    private Rect getDividerBounds(View view) {
        if (mOrientation == LinearLayout.VERTICAL) {
            int left = view.getLeft() + marginStart;
            int top = view.getBottom();
            int right = view.getRight() - marginEnd;
            int bottom = top + mDividerSize;
            return new Rect(left, top, right, bottom);
        } else {
            int left = view.getRight();
            int top = view.getTop() + marginStart;
            int right = left + mDividerSize;
            int bottom = view.getBottom() - marginEnd;
            return new Rect(left, top, right, bottom);
        }
    }
}
