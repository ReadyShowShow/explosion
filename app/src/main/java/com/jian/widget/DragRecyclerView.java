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
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * 说明：左滑控件的列表View
 * 作者：jian
 * 时间：2017/10/13.
 */

public class DragRecyclerView extends RecyclerView {

    public ViewHolder lastExpandHolder;

    public DragRecyclerView(Context context) {
        super(context);
    }

    public DragRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    @Deprecated
    public final void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter instanceof Adapter) {
            super.setAdapter(adapter);
        } else {
            throw new IllegalArgumentException("请使用DragRecycleView.Adapter(please use DragRecycleView.Adapter)");
        }
    }

    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
    }

    public static abstract class Adapter<VH extends ViewHolder> extends RecyclerView.Adapter {

        @Override
        public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (parent instanceof DragRecyclerView) {
                return this.onCreateViewHolder((DragRecyclerView) parent, viewType);
            } else {
                throw new IllegalArgumentException("请在xml布局文件中使用DragRecyclerView(please use DragRecyclerView in xml's file)");
            }
        }

        public abstract ViewHolder onCreateViewHolder(DragRecyclerView parent, int viewType);

        @Override
        public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            this.onBindViewHolder((VH) holder, position);
        }

        @CallSuper
        public void onBindViewHolder(VH holder, int position) {
            holder.resetToCloseState();
        }

    }

    public abstract static class ViewHolder extends RecyclerView.ViewHolder {
//        /**
//         * 没有拖拽效果
//         */
//        public final static int DRAG_FLAG_NULL = 0;
//        /**
//         * 左边可以被拽出
//         */
//        public final static int DRAG_FLAG_LEFT = 1 << 0;
//        /**
//         * 右边可以被拽出
//         */
//        public final static int DRAG_FLAG_RIGHT = 1 << 1;
//
//        @IntDef({DRAG_FLAG_NULL, DRAG_FLAG_LEFT, DRAG_FLAG_RIGHT})
//        @Retention(RetentionPolicy.SOURCE)
//        public @interface DragFlag {
//        }

        protected DragRecyclerView parent;

        public final DragLayout itemView;
        public final View mainView;
        /**
         * 右边可以被拽出的View
         */
        public final View rightDragView;

        private View.OnClickListener onMainViewClickOutListener;
        private DragLayout.ExpandChangeListener onMainViewExpandOutListener;

        public ViewHolder(DragRecyclerView parent, @LayoutRes int mainViewId, @LayoutRes int rightDragViewId) {
            this(parent,
                    mainViewId <= 0 ? null : LayoutInflater.from(parent.getContext()).inflate(mainViewId, parent, false),
                    rightDragViewId <= 0 ? null : LayoutInflater.from(parent.getContext()).inflate(rightDragViewId, parent, false));
        }

        private static DragLayout initItemView(DragRecyclerView parent, View mainView, View rightDragView) {
            ViewGroup.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            DragLayout itemView = new DragLayout(parent.getContext());
            itemView.addView(mainView);
            if (rightDragView != null) itemView.addView(rightDragView);
            itemView.setLayoutParams(params);
            return itemView;
        }

        public ViewHolder(final DragRecyclerView parent, View mainView, View rightDragView) {
            super(initItemView(parent, mainView, rightDragView));
            this.parent = parent;
            this.itemView = (DragLayout) super.itemView;
            this.mainView = mainView;
            this.rightDragView = rightDragView;

            mainViewClickListener();
            itemViewExpandListener();
        }

        private void mainViewClickListener() {
            mainView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (parent.lastExpandHolder != null)
                        parent.lastExpandHolder.itemView.close(true);
                    if (onMainViewClickOutListener != null) onMainViewClickOutListener.onClick(v);
                }
            });
        }

        private void itemViewExpandListener() {
            itemView.setExpandChangeListener(new DragLayout.ExpandChangeListener() {
                @Override
                public void onExpandChange(boolean expand) {
                    if (parent.lastExpandHolder != null && parent.lastExpandHolder != ViewHolder.this) {
                        parent.lastExpandHolder.itemView.close(true);
                    }
                    if (expand) {
                        parent.lastExpandHolder = ViewHolder.this;
                    } else {
                        parent.lastExpandHolder = null;
                    }
                    if (onMainViewExpandOutListener != null)
                        onMainViewExpandOutListener.onExpandChange(expand);
                }
            });
        }

        public void resetToCloseState() {
            itemView.close(false);
        }

        public View.OnClickListener getOnMainViewClickOutListener() {
            return onMainViewClickOutListener;
        }

        public void setOnMainViewClickOutListener(View.OnClickListener onMainViewClickOutListener) {
            this.onMainViewClickOutListener = onMainViewClickOutListener;
        }

        public DragLayout.ExpandChangeListener getOnMainViewExpandOutListener() {
            return onMainViewExpandOutListener;
        }

        public void setOnMainViewExpandOutListener(DragLayout.ExpandChangeListener onMainViewExpandOutListener) {
            this.onMainViewExpandOutListener = onMainViewExpandOutListener;
        }
    }
}
