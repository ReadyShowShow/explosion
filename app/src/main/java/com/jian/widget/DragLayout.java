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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * 一个左滑控件
 */
public class DragLayout extends ViewGroup {
    //为了处理单击事件的冲突
    private int mScaleTouchSlop;
    //计算滑动速度用
    private int mMaxVelocity;
    //多点触摸只算第一根手指的速度
    private int mPointerId;
    //自己的高度
    private int mHeight;
    //右侧菜单宽度总和(最大滑动距离)
    private int mRightMenuWidths;

    //滑动判定临界值（右侧菜单宽度的40%） 手指抬起时，超过了展开，没超过收起menu
    private int mLimit;
    //2016 11 13 add ，存储contentView(第一个View)
    private View mContentView;

    //以前item的滑动动画靠它做，现在用属性动画做
    //private Scroller mScroller;
    //上一次的xy
    private PointF mLastP = new PointF();
    //2016 10 22 add , 仿QQ，侧滑菜单展开时，点击除侧滑菜单之外的区域，关闭侧滑菜单。
    //增加一个布尔值变量，dispatch函数里，每次down时，为true，move时判断，如果是滑动动作，设为false。
    //在Intercept函数的up时，判断这个变量，如果仍为true 说明是点击事件，则关闭菜单。
    private boolean isUnMoved = true;

    //2016 11 03 add,判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
    //up-down的坐标，判断是否是滑动，如果是，则屏蔽一切点击事件
    private PointF mFirstP = new PointF();
    private boolean isUserSwiped;

    //防止多只手指一起滑我的flag 在每次down里判断， touch事件结束清空
    private static boolean isTouching;

    //滑动速度变量
    private VelocityTracker mVelocityTracker;

    /**
     * 右滑删除功能的开关,默认开
     */
    private boolean isSwipeEnable;

    /**
     * 20160929add 左滑右滑的开关,默认左滑打开菜单
     */
    private boolean isLeftSwipe;
    private static final int SPEED_IN_UNITS_TIME = 1000;
    private static final int ANIMATION_DURATION = 200;

    private ExpandChangeListener expandChangeListener;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public boolean isSwipeEnable() {
        return isSwipeEnable;
    }

    public void setSwipeEnable(boolean swipeEnable) {
        isSwipeEnable = swipeEnable;
    }

    public boolean isLeftSwipeEnable() {
        return isLeftSwipe;
    }

    public void setLeftSwipeEnable(boolean leftSwipeEnable) {
        isLeftSwipe = leftSwipeEnable;
    }

    public void setExpandChangeListener(ExpandChangeListener expandChangeListener) {
        this.expandChangeListener = expandChangeListener;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mScaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaxVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();

        isSwipeEnable = true;
        //左滑右滑的开关,默认左滑打开菜单
        isLeftSwipe = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //令自己可点击，从而获取触摸事件
        setClickable(true);

        //由于ViewHolder的复用机制，每次这里要手动恢复初始值
        mRightMenuWidths = 0;
        mHeight = 0;
        //2016 11 09 add,适配GridLayoutManager，将以第一个子Item(即ContentItem)的宽度为控件宽度
        int contentWidth = 0;
        int childCount = getChildCount();

        //add by 2016 08 11 为了子View的高，可以matchParent(参考的FrameLayout 和LinearLayout的Horizontal)
        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        boolean isNeedMeasureChildHeight = false;

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            //令每一个子View可点击，从而获取触摸事件
            childView.setClickable(true);
            if (childView.getVisibility() != GONE) {
                //后续计划加入上滑、下滑，则将不再支持Item的margin
                measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                //measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
                mHeight = Math.max(mHeight,
                        childView.getMeasuredHeight()/* + lp.topMargin + lp.bottomMargin*/);
                if (measureMatchParentChildren && lp.height == LayoutParams.MATCH_PARENT) {
                    isNeedMeasureChildHeight = true;
                }
                //第一个布局是Left item，从第二个开始才是RightMenu
                if (i > 0) {
                    mRightMenuWidths += childView.getMeasuredWidth();
                } else {
                    mContentView = childView;
                    contentWidth = childView.getMeasuredWidth();
                }
            }
        }
        //宽度取第一个Item(Content)的宽度
        setMeasuredDimension(getPaddingLeft() + getPaddingRight() + contentWidth,
                mHeight + getPaddingTop() + getPaddingBottom());
        //滑动判断的临界值
        mLimit = mRightMenuWidths * 4 / 10;
        //Log.d(TAG, "onMeasure() called with: " + "mRightMenuWidths = [" + mRightMenuWidths);
        //如果子View的height有MatchParent属性的，设置子View高度
        if (isNeedMeasureChildHeight) {
            forceUniformHeight(childCount, widthMeasureSpec);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    /**
     * 给MatchParent的子View设置高度
     *
     * @see android.widget.LinearLayout 同名方法
     */
    private void forceUniformHeight(int count, int widthMeasureSpec) {
        // Pretend that the linear layout has an exact size. This is the measured height of
        // ourselves. The measured height should be the max height of the children, changed
        // to accommodate the heightMeasureSpec from the parent
        //以父布局高度构建一个Exactly的测量参数
        int uniformMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        for (int i = 0; i < count; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    // Temporarily force children to reuse their old measured width
                    //measureChildWithMargins 这个函数会用到宽，所以要保存一下
                    int oldWidth = lp.width;
                    lp.width = child.getMeasuredWidth();
                    // Remeasure with new dimensions
                    measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0);
                    lp.width = oldWidth;
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int left = 0 + getPaddingLeft();
        int right = 0 + getPaddingLeft();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                //第一个子View是内容 宽度设置为全屏
                if (i == 0) {
                    childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(),
                            getPaddingTop() + childView.getMeasuredHeight());
                    left = left + childView.getMeasuredWidth();
                } else {
                    if (isLeftSwipe) {
                        childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(),
                                getPaddingTop() + childView.getMeasuredHeight());
                        left = left + childView.getMeasuredWidth();
                    } else {
                        childView.layout(right - childView.getMeasuredWidth(), getPaddingTop(), right,
                                getPaddingTop() + childView.getMeasuredHeight());
                        right = right - childView.getMeasuredWidth();
                    }

                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isSwipeEnable) {
            acquireVelocityTracker(ev);
            final VelocityTracker verTracker = mVelocityTracker;
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (onDownTouch(ev)) return false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    onMoveTouch(ev);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    onReleaseTouch(ev, verTracker);
                    break;
                default:
                    break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void onReleaseTouch(MotionEvent ev, VelocityTracker verTracker) {
        //2016 11 03 add,判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
        if (Math.abs(ev.getRawX() - mFirstP.x) > mScaleTouchSlop) {
            isUserSwiped = true;
        }

        //求伪瞬时速度
        verTracker.computeCurrentVelocity(SPEED_IN_UNITS_TIME, mMaxVelocity);
        final float velocityX = verTracker.getXVelocity(mPointerId);
        //滑动速度超过阈值
        if (Math.abs(velocityX) > SPEED_IN_UNITS_TIME) {
            if (velocityX < -SPEED_IN_UNITS_TIME) {
                //左滑
                if (isLeftSwipe) {
                    //平滑展开Menu
                    smoothExpand();

                } else {
                    //平滑关闭Menu
                    close(true);
                }
            } else {
                //左滑
                if (isLeftSwipe) {
                    // 平滑关闭Menu
                    close(true);
                } else {
                    //平滑展开Menu
                    smoothExpand();
                }
            }
        } else {
            //否则就判断滑动距离
            if (Math.abs(getScrollX()) > mLimit) {
                //平滑展开Menu
                smoothExpand();
            } else {
                // 平滑关闭Menu
                close(true);
            }
        }
        //释放
        releaseVelocityTracker();
        //LogUtils.i(TAG, "onTouch A ACTION_UP ACTION_CANCEL:velocityY:" + velocityX);
        //没有手指在摸我了
        isTouching = false;
    }

    private void onMoveTouch(MotionEvent ev) {
        float gap = mLastP.x - ev.getRawX();
        //为了在水平滑动中禁止父类ListView等再竖直滑动
        //2016 09 29 修改此处，使屏蔽父布局滑动更加灵敏，
        if (Math.abs(gap) > 10 || Math.abs(getScrollX()) > 10) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        //2016 10 22 add , 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单。begin
        if (Math.abs(gap) > mScaleTouchSlop) {
            isUnMoved = false;
        }
        //2016 10 22 add , 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单。end
        //如果scroller还没有滑动结束 停止滑动动画
/*                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }*/
        //滑动使用scrollBy
        scrollBy((int) gap, 0);
        //越界修正
        if (isLeftSwipe) {
            //左滑
            if (getScrollX() < 0) {
                scrollTo(0, 0);
            }
            if (getScrollX() > mRightMenuWidths) {
                scrollTo(mRightMenuWidths, 0);
            }
        } else {
            //右滑
            if (getScrollX() < -mRightMenuWidths) {
                scrollTo(-mRightMenuWidths, 0);
            }
            if (getScrollX() > 0) {
                scrollTo(0, 0);
            }
        }

        mLastP.set(ev.getRawX(), ev.getRawY());
    }

    private boolean onDownTouch(MotionEvent ev) {
        //2016 11 03 add,判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
        isUserSwiped = false;
        //2016 10 22 add , 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单。
        isUnMoved = true;
        //如果有别的指头摸过了，那么就return false。这样后续的move..等事件也不会再来找这个View了。
        if (isTouching) {
            return true;
        } else {
            //第一个摸的指头，赶紧改变标志，宣誓主权。
            isTouching = true;
        }
        mLastP.set(ev.getRawX(), ev.getRawY());
        //2016 11 03 add,判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
        mFirstP.set(ev.getRawX(), ev.getRawY());

        //求第一个触点的id， 此时可能有多个触点，但至少一个，计算滑动速率用
        mPointerId = ev.getPointerId(0);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //禁止侧滑时，点击事件不受干扰。
        if (isSwipeEnable) {
            int action = ev.getAction();
            switch (action) {
                // fix 长按事件和侧滑的冲突。
                case MotionEvent.ACTION_MOVE:
                    //屏蔽滑动时的事件
                    if (Math.abs(ev.getRawX() - mFirstP.x) > mScaleTouchSlop) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //为了在侧滑时，屏蔽子View的点击事件
                    if (isLeftSwipe) {
                        //add by 2016 09 10 解决一个智障问题~ 居然不给点击侧滑菜单 我跪着谢罪
                        //这里判断落点在内容区域屏蔽点击，内容区域外，允许传递事件继续向下的的。。。
                        if ((getScrollX() > mScaleTouchSlop)
                                && ev.getX() < getWidth() - getScrollX()) {
                            //2016 10 22 add , 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单。
                            if (isUnMoved) {
                                close(true);
                            }
                            //true表示拦截
                            return true;
                        }
                    } else {
                        //点击范围在菜单外 屏蔽
                        if ((-getScrollX() > mScaleTouchSlop) && (ev.getX() > -getScrollX())) {
                            //2016 10 22 add , 仿QQ，侧滑菜单展开时，点击内容区域，关闭侧滑菜单。
                            if (isUnMoved) {
                                close(true);
                            }
                            return true;
                        }
                    }
                    // 判断手指起始落点，如果距离属于滑动了，就屏蔽一切点击事件。
                    if (isUserSwiped) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 平滑展开
     */
    private ValueAnimator mExpandAnim;
    private ValueAnimator mCloseAnim;

    public void smoothExpand() {
        //2016 11 13 add 侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView.setLongClickable(false);
        }

        cancelAnim();
        mExpandAnim = ValueAnimator.ofInt(getScrollX(),
                isLeftSwipe ? mRightMenuWidths : -mRightMenuWidths);
        mExpandAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scrollTo((Integer) animation.getAnimatedValue(), 0);
            }
        });
        mExpandAnim.setInterpolator(new OvershootInterpolator());
        mExpandAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (expandChangeListener != null) {
                    expandChangeListener.onExpandChange(true);
                }
            }
        });
        mExpandAnim.setDuration(ANIMATION_DURATION).start();
    }

    private void cancelAnim() {
        if (mCloseAnim != null && mCloseAnim.isRunning()) {
            mCloseAnim.cancel();
        }
        if (mExpandAnim != null && mExpandAnim.isRunning()) {
            mExpandAnim.cancel();
        }
    }

    /**
     * 平滑关闭
     */
    public void close(boolean smooth) {
        //2016 11 13 add 侧滑菜单展开，屏蔽content长按
        if (null != mContentView) {
            mContentView.setLongClickable(true);
        }
        cancelAnim();
        if (expandChangeListener != null) {
            expandChangeListener.onExpandChange(false);
        }
        if (smooth) {
            mCloseAnim = ValueAnimator.ofInt(getScrollX(), 0);
            mCloseAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scrollTo((Integer) animation.getAnimatedValue(), 0);
                }
            });
            mCloseAnim.setInterpolator(new AccelerateInterpolator());
            mCloseAnim.setDuration(ANIMATION_DURATION).start();
        } else {
            scrollTo(0, 0);
        }
    }

    /**
     * @param event 向VelocityTracker添加MotionEvent
     * @see VelocityTracker#obtain()
     * @see VelocityTracker#addMovement(MotionEvent)
     */
    private void acquireVelocityTracker(final MotionEvent event) {
        if (null == mVelocityTracker) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    /**
     * * 释放VelocityTracker
     *
     * @see VelocityTracker#clear()
     * @see VelocityTracker#recycle()
     */
    private void releaseVelocityTracker() {
        if (null != mVelocityTracker) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    //展开时，禁止长按
    @Override
    public boolean performLongClick() {
        if (Math.abs(getScrollX()) > mScaleTouchSlop) {
            return false;
        }
        return super.performLongClick();
    }

    public interface ExpandChangeListener {
        void onExpandChange(boolean expand);
    }
}