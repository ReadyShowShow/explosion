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

package com.jian.activity.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.jian.R;
import com.jian.explosion.animation.ExplosionField;
import com.jian.utils.ToastUtil;
import com.jian.widget.DragRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WordAdapter extends DragRecyclerView.Adapter<WordAdapter.WordListViewHolder> {
    protected Context mContext;

    // 为了能够在WordAdapter回收时,WordListViewHolder被及时回收,固不可用static
    // 为了能让WordListViewHolder访问到,固出现了类的双向引用
    public WordListViewHolder selectedViewHolder;
    // 由于ViewHolder和ViewModel不是强相关,导致
    public WordItemViewModel selectedViewModel;

    protected List<WordItemViewModel> dataSet = new ArrayList<>();

    public WordAdapter(Context context) {
        mContext = context;
    }

    @Override
    public WordListViewHolder onCreateViewHolder(DragRecyclerView parent, int viewType) {
        return new WordListViewHolder(mContext, parent, R.layout.item_shorthand_word_list_main_view,
                R.layout.item_word_extra_part);
    }

    @Override
    public void onBindViewHolder(WordListViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.bind(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public List<WordItemViewModel> getDataSet() {
        return dataSet;
    }

    public void setDataSet(List<WordItemViewModel> list) {
        this.dataSet = list;
    }

    public class WordListViewHolder extends DragRecyclerView.ViewHolder {
        @BindView(R.id.item_shorthand_word)
        TextView wordTv;
        @BindView(R.id.item_shorthand_phrase)
        TextView phraseTv;
        @BindView(R.id.item_shorthand_phrase_detail)
        View phraseDetailV;
        @BindView(R.id.item_shorthand_difficult)
        View difficultV;

        Context context;

        WordItemViewModel model;

        public WordListViewHolder(Context context, DragRecyclerView parent, @LayoutRes int mainViewId, @LayoutRes int rightDragViewId) {
            super(parent, mainViewId, rightDragViewId);
            ButterKnife.bind(this, itemView);
            this.context = context;
        }

        public void bind(WordItemViewModel md) { // NOPMD
            this.model = md;

            wordTv.setText(model.getEnglish());
            phraseTv.setText(model.getChinese());
            resetEnglishWordTextStyle(model.isSelected());
            phraseTv.setVisibility(model.isShowPhrase() ? View.VISIBLE : View.INVISIBLE);

            resetClickListener();
        }

        private void resetClickListener() {
            setOnMainViewClickOutListener(v -> {
                if (selectedViewModel != null && selectedViewModel != model) {
                    selectedViewModel.setSelected(false);
                    selectedViewModel.setShowPhrase(false);
                }

                selectedViewModel = model;
                if (selectedViewHolder != null && selectedViewHolder != WordListViewHolder.this) {
                    selectedViewHolder.disSelected();
                }
                selectedViewHolder = WordListViewHolder.this;

                if (!model.isSelected()) {
                    model.setSelected(true);
                    resetEnglishWordTextStyle(model.isSelected());
                    model.setShowPhrase(true);
                }

                model.setShowPhrase(!model.isShowPhrase());
                phraseTv.setVisibility(model.isShowPhrase() ? View.VISIBLE : View.GONE);

            });
            phraseDetailV.setOnClickListener(v -> {
                ToastUtil.toast(context, "选中单词：" + model.getEnglish());
                itemView.close(true);
            });
            difficultV.setOnClickListener(v -> {
                ToastUtil.toast(context, "移走：" + model.getEnglish());
                phraseTv.setVisibility(View.VISIBLE);
                itemView.setEnabled(false);
                itemView.setAnimation(new TranslateAnimation(0, 100, 0, 0));
                new ExplosionField(context).explode(itemView, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dataSet.remove(model);
                        notifyDataSetChanged();
                    }
                });
            });
        }

        private void resetEnglishWordTextStyle(boolean isSelected) {
            wordTv.setTextColor(context.getResources().getColor(isSelected ? R.color.c3_1 : R.color.c3_6));
        }

        public void disSelected() {
            model.setSelected(false);
            resetEnglishWordTextStyle(model.isSelected());
            model.setShowPhrase(false);
            phraseTv.setVisibility(View.GONE);
        }
    }
}

