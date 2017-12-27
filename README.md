# 💥Android爆炸破碎动画💥
  这个破碎动画，是一种类似小米系统删除应用时的爆炸破碎效果的动画。
## 效果图展示
先来看下是怎样的动效，要是感觉不是理想的学习目标，就跳过，避免浪费大家的时间。🙂

![ezgif-2-a640aae0e5.gif](http://upload-images.jianshu.io/upload_images/7802495-a0b5bb591652949b.gif?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

源码在这里👉<https://github.com/ReadyShowShow/explosion>

解析在这里👉<http://www.jianshu.com/p/11bed7dabe2c>
## 一行代码即可调用该动画
```
new ExplosionField(this).explode(view, null))
```
下面开始我们酷炫的Android动画特效正式讲解👇

---

## 先来个整体结构的把握
  整体结构非常简单明了，新老从业者都可快速看懂，容易把握学习。
```
./
|-- explosion
|   |-- MainActivity.java (测试爆炸破碎动效的主界面)
|   |-- animation(爆炸破碎动效有关的类均在这里)
|   |   |-- ExplosionAnimator.java(爆炸动画)
|   |   |-- ExplosionField.java(爆炸破碎动画所依赖的View)
|   |   `-- ParticleModel.java(每个破碎后的粒子的model，颜色、位置、大小等)
|   `-- utils
|       `-- UIUtils.java(计算状态栏高度的工具类)
`-- tree.txt
```

## 庖丁解牛
  下面开始每个类的详细分析
  本着从简到繁、由表及里的原则，详细讲解每个类
### MainActivity.java
> MainActivity.java是测试动效的界面，该Activity内部有7个测试按钮。该类做的事情非常单纯，就是给每个View分别绑定click点击事件，让View在点击时能触发爆炸破碎动画。
``` java
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
```
### ParticleModel.java
>  ParticleModel.java是包含一个粒子的所有信息的model。advance方法根据值动画返回的进度计算出粒子的位置和颜色等信息
``` java
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
```
### ExplosionAnimation.java
> ExlosionAnimation.java是动画类，是一个值动画，在值动画每次产生一个值的时候，就计算出整个爆炸破碎动效内的全部粒子的状态。这些状态交由使用的View在渲染时进行显示。

``` java
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
        setFloatValues(0.0f, 1.0f);
        setDuration(DEFAULT_DURATION);

        mPaint = new Paint();
        mContainer = view;
        mParticles = generateParticles(bitmap, bound);
    }

    // 生成粒子，按行按列生成全部粒子
    private ParticleModel[][] generateParticles(Bitmap bitmap, Rect bound) {
        int w = bound.width();
        int h = bound.height();

        // 横向粒子的个数
        int horizontalCount = w / ParticleModel.PART_WH;
        // 竖向粒子的个数
        int verticalCount = h / ParticleModel.PART_WH;

        // 粒子宽度
        int bitmapPartWidth = bitmap.getWidth() / horizontalCount;
        // 粒子高度
        int bitmapPartHeight = bitmap.getHeight() / verticalCount;

        ParticleModel[][] particles = new ParticleModel[verticalCount][horizontalCount];
        for (int row = 0; row < verticalCount; row++) {
            for (int column = 0; column < horizontalCount; column++) {
                //取得当前粒子所在位置的颜色
                int color = bitmap.getPixel(column * bitmapPartWidth, row * bitmapPartHeight);

                Point point = new Point(column, row);
                particles[row][column] = new ParticleModel(color, bound, point);
            }
        }
        return particles;
    }

    // 由view调用，在View上绘制全部的粒子
    void draw(Canvas canvas) {
        // 动画结束时停止
        if (!isStarted()) {
            return;
        }
        // 遍历粒子，并绘制在View上
        for (ParticleModel[] particle : mParticles) {
            for (ParticleModel p : particle) {
                p.advance((Float) getAnimatedValue());
                mPaint.setColor(p.color);
                // 错误的设置方式只是这样设置，透明色会显示为黑色
                // mPaint.setAlpha((int) (255 * p.alpha)); 
                // 正确的设置方式，这样透明颜色就不是黑色了
                mPaint.setAlpha((int) (Color.alpha(p.color) * p.alpha));
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
```
### ExplosionField.java
>  ExplosionField.java是真实执行上面ExplosionAnimator。ExplosionField会创建一个View并依附在Activity的根View上。
``` java
/**
 * 说明：每次爆炸时，创建一个覆盖全屏的View，这样的话，不管要爆炸的View在任何位置都能显示爆炸效果
 * 作者：Jian
 * 时间：2017/12/26.
 */
public class ExplosionField extends View {
    private static final String TAG = "ExplosionField";
    private static final Canvas mCanvas = new Canvas();
    private ExplosionAnimator animator;

    public ExplosionField(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        animator.draw(canvas);
    }

    /**
     * 执行爆破破碎动画
     */
    public void explode(final View view, final AnimatorListenerAdapter listener) {
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect); //得到view相对于整个屏幕的坐标
        rect.offset(0, -UIUtils.statusBarHeignth()); //去掉状态栏高度

        animator = new ExplosionAnimator(this, createBitmapFromView(view), rect);

        // 接口回调
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) listener.onAnimationStart(animation);
                // 延时添加到界面上
                attach2Activity((Activity) getContext());
                // 让被爆炸的View消失（爆炸的View是新创建的View，原View本身不会发生任何变化）
                view.animate().alpha(0f).setDuration(150).start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) listener.onAnimationEnd(animation);
                // 从界面中移除
                removeFromActivity((Activity) getContext());
                // 让被爆炸的View显示（爆炸的View是新创建的View，原View本身不会发生任何变化）
                view.animate().alpha(1f).setDuration(150).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (listener != null) listener.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (listener != null) listener.onAnimationRepeat(animation);
            }
        });
        animator.start();
    }

    private Bitmap createBitmapFromView(View view) {
//         为什么屏蔽以下代码段？
//         如果ImageView直接得到位图，那么当它设置背景（backgroud)时，不会读取到背景颜色
//        if (view instanceof ImageView) {
//            Drawable drawable = ((ImageView)view).getDrawable();
//            if (drawable != null && drawable instanceof BitmapDrawable) {
//                return ((BitmapDrawable) drawable).getBitmap();
//            }
//        }
        //view.clearFocus(); //不同焦点状态显示的可能不同——（azz:不同就不同有什么关系？）

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

        if (bitmap != null) {
            synchronized (mCanvas) {
                mCanvas.setBitmap(bitmap);
                view.draw(mCanvas);
                // 清除引用
                mCanvas.setBitmap(null);
            }
        }
        return bitmap;
    }

    /**
     * 将创建的ExplosionField添加到Activity上
     */
    private void attach2Activity(Activity activity) {
        ViewGroup rootView = activity.findViewById(Window.ID_ANDROID_CONTENT);

        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(this, lp);
    }

    /**
     * 将ExplosionField从Activity上移除
     */
    private void removeFromActivity(Activity activity) {
        ViewGroup rootView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        rootView.removeView(this);
    }
}

```
#### 动画执行时为什么要创建一个新View（ExplosionField
 其实上面的动画类ExplosionAnimator已经实现了核心功能，直接在原View上使用该动画应该是没问题的。为什么还要引入一个ExplosionField类呢？动画的执行为什么不能直接在原本的View上执行呢？偏偏要在一个看似多余的ExplosionField对象上执行呢。
 
 这里就得从Android下View绘制原理来解释了：Android下的View都有一个Bound，在View进行measure和layout的时候，已经确定了View的大小和位置，如果要在这个View上进行动画的话，就会出现动画只能在view大小范围内进行展现。当然了，也不是说在原来View上一定不能实现这一动效，就是相当复杂，要在动画执行过程中，不断改变原View的大小和View的属性等信息，相当复杂。

在性能还行的前提下，要优先代码的整洁度，尽量避免为了优化的性能，而舍弃整洁清爽的代码。一般来说，过度的优化，并没有给用户带来太多体验上的提升，反而给项目带来了巨大的维护难度。

### UIUtils.java
> UIUtils是关于UI的工具类，没啥可说的
```java
public class UIUtils {

    public static int dp2px(double dpi) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dpi + 0.5f);
    }

    public static int statusBarHeignth() {
        return dp2px(25);
    }
}
```
## 结束
源码👉<https://github.com/ReadyShowShow/explosion>
如果有优化的建议与意见，欢迎大家提[Issues](https://github.com/ReadyShowShow/explosion/issues)或者邮箱<ReadyShowShow@gmail.com>
