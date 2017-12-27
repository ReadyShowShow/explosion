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

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

/**
 * 说明：爆炸动画类，让离子移动和控制离子透明度
 * 作者：Jian
 * 时间：2017/12/26.
 */
class ExplosionAnimator extends ValueAnimator {
    private static final int DEFAULT_DURATION = 1500;
    private ParticleModel[][] mParticles;
    private Paint mPaint;
    private View mContainer;

    public ExplosionAnimator(View view, Bitmap bitmap, Rect bound) {

        mPaint = new Paint();
        mContainer = view;

        setFloatValues(0.0f, 1.0f);
        setDuration(DEFAULT_DURATION);

        mParticles = generateParticles(bitmap, bound);
    }

    private ParticleModel[][] generateParticles(Bitmap bitmap, Rect bound) {
        int w = bound.width();
        int h = bound.height();

        int partW_Count = w / ParticleModel.PART_WH; //横向个数
        int partH_Count = h / ParticleModel.PART_WH; //竖向个数

        int bitmap_part_w = bitmap.getWidth() / partW_Count;
        int bitmap_part_h = bitmap.getHeight() / partH_Count;

        ParticleModel[][] particles = new ParticleModel[partH_Count][partW_Count];
        Point point = null;
        for (int row = 0; row < partH_Count; row++) { //行
            for (int column = 0; column < partW_Count; column++) { //列
                //取得当前粒子所在位置的颜色
                int color = bitmap.getPixel(column * bitmap_part_w, row * bitmap_part_h);

                point = new Point(column, row); //x是列，y是行

                particles[row][column] = ParticleModel.generateParticle(color, bound, point);
            }
        }

        return particles;
    }

    void draw(Canvas canvas) {
        if (!isStarted()) { //动画结束时停止
            return;
        }
        for (ParticleModel[] particle : mParticles) {
            for (ParticleModel p : particle) {
                p.advance((Float) getAnimatedValue());
                mPaint.setColor(p.color);
//                mPaint.setAlpha((int) (255 * p.alpha)); //只是这样设置，透明色会显示为黑色
                mPaint.setAlpha((int) (Color.alpha(p.color) * p.alpha)); //这样透明颜色就不是黑色了
                canvas.drawCircle(p.cx, p.cy, p.radius, mPaint);
            }
        }

        mContainer.invalidate();
    }

    @Override
    public void start() {
        super.start();
        mContainer.invalidate();
    }
}
