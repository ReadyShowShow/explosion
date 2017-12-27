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

package com.jian.explosion.animation;

import android.graphics.Point;
import android.graphics.Rect;

import java.util.Random;

/**
 * 说明：爆破粒子，每个移动与渐变的小块
 * 作者：Jian
 * 时间：2017/12/26.
 */
class ParticleModel {
    // 默认小球宽高
    static final int PART_WH = 8;
    // 随机数，随机出位置和大小
    static Random random = new Random();
    //center x of circle
    float cx;
    //center y of circle
    float cy;
    // 半径
    float radius;
    // 颜色
    int color;
    // 透明度
    float alpha;
    // 整体边界
    Rect mBound;

    ParticleModel(int color, Rect bound, Point point) {
        int row = point.y; //行是高
        int column = point.x; //列是宽

        this.mBound = bound;
        this.color = color;
        this.alpha = 1f;
        this.radius = PART_WH;
        this.cx = bound.left + PART_WH * column;
        this.cy = bound.top + PART_WH * row;
    }

    // 每一步动画都得重新计算出自己的状态值
    void advance(float factor) {
        cx = cx + factor * random.nextInt(mBound.width()) * (random.nextFloat() - 0.5f);
        cy = cy + factor * random.nextInt(mBound.height() / 2);

        radius = radius - factor * random.nextInt(2);

        alpha = (1f - factor) * (1 + random.nextFloat());
    }
}
