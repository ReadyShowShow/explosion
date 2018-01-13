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

package com.jian.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jian.R;


/**
 * 说明：测试的入口界面
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
        initClicks();
    }

    private void initClicks() {
        findViewById(R.id.main_explosion_btn).setOnClickListener(v -> {
            Intent i = new Intent(this, ExplosionActivity.class);
            this.startActivity(i);
        });
        findViewById(R.id.main_drag_btn).setOnClickListener(v -> {
            Intent i = new Intent(this, DragActivity.class);
            this.startActivity(i);
        });
    }
}
