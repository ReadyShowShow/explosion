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
`-- architecture.md
```

## 庖丁解牛
  下面开始每个类的详细分析
  本着从简到繁、由表及里的原则，详细讲解每个类
### MainActivity.java
> MainActivity.java是测试动效的界面，该Activity内部有7个测试按钮。该类做的事情非常单纯，就是给每个View分别绑定click点击事件，让View在点击时能触发爆炸破碎动画。
### ParticleModel.java
>  ParticleModel.java是包含一个粒子的所有信息的model。advance方法根据值动画返回的进度计算出粒子的位置和颜色等信息
### ExplosionAnimation.java
> ExlosionAnimation.java是动画类，是一个值动画，在值动画每次产生一个值的时候，就计算出整个爆炸破碎动效内的全部粒子的状态。这些状态交由使用的View在渲染时进行显示。
### ExplosionField.java
>  ExplosionField.java是真实执行上面ExplosionAnimator。ExplosionField会创建一个View并依附在Activity的根View上。
#### 动画执行时为什么要创建一个新View（ExplosionField
 其实上面的动画类ExplosionAnimator已经实现了核心功能，直接在原View上使用该动画应该是没问题的。为什么还要引入一个ExplosionField类呢？动画的执行为什么不能直接在原本的View上执行呢？偏偏要在一个看似多余的ExplosionField对象上执行呢。
 
 这里就得从Android下View绘制原理来解释了：Android下的View都有一个Bound，在View进行measure和layout的时候，已经确定了View的大小和位置，如果要在这个View上进行动画的话，就会出现动画只能在view大小范围内进行展现。当然了，也不是说在原来View上一定不能实现这一动效，就是相当复杂，要在动画执行过程中，不断改变原View的大小和View的属性等信息，相当复杂。

在性能还行的前提下，要优先代码的整洁度，尽量避免为了优化的性能，而舍弃整洁清爽的代码。一般来说，过度的优化，并没有给用户带来太多体验上的提升，反而给项目带来了巨大的维护难度。

### UIUtils.java
> UIUtils是关于UI的工具类，没啥可说的
## 结束

源码在这里👉<https://github.com/ReadyShowShow/explosion>

如果有优化的建议与意见，欢迎大家提[Issues](https://github.com/ReadyShowShow/explosion/issues)或者邮箱<ReadyShowShow@gmail.com>
