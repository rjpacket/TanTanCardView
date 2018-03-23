### 一、效果图展示

### 二、分析
  这是一个触摸，内容子View跟随滑动的效果，重写onTouchEvent很好实现。每一张卡片都是层叠的，容易想到实现FrameLayout。
    
### 三、代码实现
  先不管，直接实现：
    
```
public abstract class TanTanBaseView<T> extends FrameLayout {

    public TanTanBaseView(Context context) {
        this(context, null);
    }

    public TanTanBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }
}
```
  为什么写成抽象的呢？因为考虑到Model我们不知道到底什么样的，卡片的布局我们也不知道，那么我们抽象一个方法，不去管Model和布局，方便复用。
    
```
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        height = width = MeasureSpec.getSize(widthMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            measureChild(view, widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
        centerX = width / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            view.layout(SPACE * i, SPACE * i, width - SPACE * (cardCount - i), height - SPACE * (cardCount - i));
        }
    }
```
  onMeasure里面需要measureChild()。重点看下onLayout()方法，这里我们需要做出层叠的效果，那么就不能一个View覆盖另一个View，需要留一点SPACE空间出来。
    
```
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mDatas.size() == 0){
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        float nowX = event.getX();
        float nowY = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = nowX;
                downY = nowY;
                break;
            case MotionEvent.ACTION_MOVE:
                moveCard(downX, downY, nowX, nowY);
                downX = nowX;
                downY = nowY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                int left = moveView.getLeft();
                if (Math.abs(left) > centerX) {
                    if(left > 0) {
                        animatorMoveView(moveView.getLeft(), moveView.getTop(), width * 2, 0);
                    }else{
                        animatorMoveView(moveView.getLeft(), moveView.getTop(), - width * 2, 0);
                    }
                    isRemove = true;
                } else {
                    animatorMoveView(moveView.getLeft(), moveView.getTop(), SPACE * 2, SPACE * 2);
                }
                break;
        }
        return true;
    }
```
  onTouchEvent()里面，MOVE里面有个移动卡片的方法：
```
    /**
     * 移动最顶上的View
     *
     * @param downX
     * @param downY
     * @param nowX
     * @param nowY
     */
    private void moveCard(float downX, float downY, float nowX, float nowY) {
        int dx = (int) (nowX - downX);
        int dy = (int) (nowY - downY);
        moveView.layout(
                moveView.getLeft() + dx,
                moveView.getTop() + dy,
                moveView.getRight() + dx,
                moveView.getBottom() + dy
        );
        int left = moveView.getLeft();
        if(Math.abs(left) > centerX) {
            if(left > 0){
                left = centerX;
            }else{
                left = -centerX;
            }

        }
        moveView.setRotation((float) (left * 1.0 / centerX * ANGLE));
    }
```
  当产生dx和dy的位移时，将这个位移体现到MoveView的位置上，下面是旋转，我们设定好一个最大允许旋转角度ANGLE，然后根据x轴位移去计算应该旋转多少角度。
    
  回到onTouchEvent()，在UP和CANCEL的时候，判断一下左右的偏移，如果超过一半width（也就是centerX）的时候，一个属性动画滑出去，否则复位。
```
    /**
     * 开启动画
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     */
    private void animatorMoveView(int startX, int startY, int endX, int endY) {
        prePoint = new Point(startX, startY);
        Point endLoc = new Point(endX, endY);
        ValueAnimator animator = ValueAnimator.ofObject(new LocationEvaluator(), prePoint, endLoc);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Point curPoint = (Point) animation.getAnimatedValue();
                moveCard(prePoint.x, prePoint.y, curPoint.x, curPoint.y);
                prePoint = curPoint;
            }
        });
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(isRemove) {
                    removeView(moveView);
                    cacheViews.add(moveView);
                    cardViews.removeFirst();
                    mDatas.removeFirst();
                    removeOtherView();
                    resetData();
                    isRemove = false;
                }
                moveView.setRotation(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
```
  这个动画是自定义的一个属性动画，不断的返回起止位置间的一系列Point：
```
public class LocationEvaluator implements TypeEvaluator<Point> {
    @Override
    public Point evaluate(float fraction, Point sp, Point ep) {
        Point p = new Point();
        p.x = (int) (sp.x + (ep.x - sp.x) * fraction);
        p.y = (int) (sp.y + (ep.y - sp.y) * fraction);
        return p;
    }
}
```
  监听动画的addUpdateListener() 方法里面，去调用移动卡片的方法，就会产生一个动画的效果，卡片的飞入飞出。
    
  动画结束的时候，我们需要移除这个最上层的View，但是我们可以把这个View缓存起来，然后把底下的两张卡片View也移除掉，同样缓存起来，调用resetData()方法，重修初始化数据：
```
    /**
     * 重置数据
     */
    private void resetData() {
        removeAllViews();
        int size = mDatas.size();
        int count = Math.min(cardCount, size);
        for (int i = 0; i < count; i++) {
            View view;
            if(cacheViews.size() > 0){
                view = getView(mDatas.get(i), cacheViews.remove(0));
            }else{
                view = getView(mDatas.get(i), null);
            }
            cardViews.add(0, view);
            ViewGroup parent = (ViewGroup) view.getParent();
            if(parent != null){
                parent.removeView(view);
            }
            addView(view, 0);
            if(i == 0){
                moveView = view;
            }
        }
    }
```
  这里需要注意getView()方法，仿照ListView的Adapter写法，抽象的暴漏给子View去更新。
    
  我这里写了一个例子：
```
public class TTView extends TanTanBaseView<String> {
    public TTView(Context context) {
        super(context);
    }

    public TTView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getView(String item, View convertView) {
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_tan_tan_view, null);
        }
        TextView tvNum = (TextView) convertView.findViewById(R.id.num);
        tvNum.setText(item);
        return convertView;
    }
}
```

  简单的继承一下，重写getView()方法就好了。