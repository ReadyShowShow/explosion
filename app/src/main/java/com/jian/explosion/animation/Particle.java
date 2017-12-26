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
class Particle {
    static final int PART_WH = 8; //默认小球宽高

    //原本的值（不可变）
//    float originCX;
//    float originCY;
//    float originRadius;

    //实际的值（可变）
    float cx; //center x of circle
    float cy; //center y of circle
    float radius;

    int color;
    float alpha;

    static Random random = new Random();

    Rect mBound;

    static Particle generateParticle(int color, Rect bound, Point point) {
        int row = point.y; //行是高
        int column = point.x; //列是宽

        Particle particle = new Particle();
        particle.mBound = bound;
        particle.color = color;
        particle.alpha = 1f;

        particle.radius = PART_WH;
        particle.cx = bound.left + PART_WH * column;
        particle.cy = bound.top + PART_WH * row;

        return particle;
    }

    void advance(float factor) {
        cx = cx + factor * random.nextInt(mBound.width()) * (random.nextFloat() - 0.5f);
        cy = cy + factor * random.nextInt(mBound.height() / 2);

        radius = radius - factor * random.nextInt(2);

        alpha = (1f - factor) * (1 + random.nextFloat());
    }
}
