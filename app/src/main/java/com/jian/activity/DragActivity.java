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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.LinearLayout;

import com.jian.R;
import com.jian.activity.adapter.WordAdapter;
import com.jian.activity.adapter.WordItemViewModel;
import com.jian.widget.DragRecyclerView;
import com.jian.widget.RecycleViewDivider;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class DragActivity extends AppCompatActivity {
    @BindView(R.id.activity_word_list)
    DragRecyclerView wordList;

    List<WordItemViewModel> mockData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag);
        mockData();
        showListView();
    }

    private void mockData() {
        mockData = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mockData.add(new WordItemViewModel("word" + i, "chinese" + i, i % 4 == 0, i % 3 == 1, i % 2 == 0));
        }
    }

    private void showListView() {
        int orientation = LinearLayout.VERTICAL;
        wordList.setLayoutManager(new LinearLayoutManager(this, orientation, false));
        wordList.addItemDecoration(new RecycleViewDivider(this, orientation,
                R.drawable.shape_horizontal_divider,
                getResources().getDimensionPixelSize(R.dimen.global_margin_horizontal),
                getResources().getDimensionPixelSize(R.dimen.global_margin_horizontal)
        ));
        WordAdapter adapter = new WordAdapter(this);
        wordList.setAdapter(adapter);
        adapter.setDataSet(mockData, true);

    }
}
