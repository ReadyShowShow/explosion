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

package com.jian.explosion;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.jian.explosion.animation.ExplosionField;

/**
 * 说明：测试的界面
 * 作者：Jian
 * 时间：2017/12/26.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * 加载布局文件，添加点击事件
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewsClick();
    }

    /**
     * 添加点击事件的实现
     */
    private void initViewsClick() {
        // 为单个View添加点击事件
        final View title = findViewById(R.id.title_tv);
        title.setOnClickListener(v ->
                new ExplosionField(MainActivity.this).explode(title, null));

        // 为中间3个View添加点击事件
        setSelfAndChildDisappearOnClick(findViewById(R.id.title_disappear_ll));
        // 为下面3个View添加点击事件
        setSelfAndChildDisappearAndAppearOnClick(findViewById(R.id.title_disappear_and_appear_ll));

        // 跳转到github网页的点击事件
        findViewById(R.id.github_tv).setOnClickListener((view) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri content_url = Uri.parse(getString(R.string.github));
            intent.setData(content_url);
            startActivity(intent);
        });
    }

    /**
     * 为自己以及子View添加破碎动画，动画结束后，把View消失掉
     * @param view 可能是ViewGroup的view
     */
    private void setSelfAndChildDisappearOnClick(final View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setSelfAndChildDisappearOnClick(viewGroup.getChildAt(i));
            }
        } else {
            view.setOnClickListener(v ->
                    new ExplosionField(MainActivity.this).explode(view,
                            new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    view.setVisibility(View.GONE);
                                }
                            }));
        }
    }

    /**
     * 为自己以及子View添加破碎动画，动画结束后，View自动出现
     * @param view 可能是ViewGroup的view
     */
    private void setSelfAndChildDisappearAndAppearOnClick(final View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                setSelfAndChildDisappearAndAppearOnClick(viewGroup.getChildAt(i));
            }
        } else {
            view.setOnClickListener(v ->
                    new ExplosionField(MainActivity.this).explode(view, null));
        }
    }
}
