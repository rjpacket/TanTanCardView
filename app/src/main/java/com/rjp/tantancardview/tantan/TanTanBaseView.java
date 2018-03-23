package com.rjp.tantancardview.tantan;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.LinkedList;
import java.util.List;

/**
 * author : Gimpo create on 2018/3/22 12:28
 * email  : jimbo922@163.com
 */

public abstract class TanTanBaseView<T> extends FrameLayout {

    private int width;
    private int height;
    private int cardCount = 3;
    private final int SPACE = 20;
    private final int ANGLE = 30;
    public Context mContext;
    private LinkedList<T> mDatas = new LinkedList<>();
    private LinkedList<View> cardViews = new LinkedList<>();
    private LinkedList<View> cacheViews = new LinkedList<>();
    private float downX;
    private float downY;
    private int centerX;
    private View moveView;
    private Point prePoint = new Point();
    private boolean isRemove = false;

    public TanTanBaseView(Context context) {
        this(context, null);
    }

    public TanTanBaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mContext = context;
        setClipChildren(false);

//        for (int i = 0; i < 3; i++) {
//            View view = LayoutInflater.from(mContext).inflate(R.layout.item_tan_tan_view, null);
//            TextView tvNum = (TextView) view.findViewById(R.id.num);
//            tvNum.setText(String.valueOf(i * 10000));
//            cardViews.add(view);
//            addView(view);
//            if (i == 2) {
//                moveView = view;
//            }
//        }
    }

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

    /**
     * 移除其他的view
     */
    private void removeOtherView() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(childCount);
            cacheViews.add(view);
        }
    }

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

    /**
     * 添加数据
     * @param datas
     */
    public void addData(List<T> datas){
        mDatas.addAll(datas);
        resetData();
    }

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

    protected abstract View getView(T item, View convertView);
}
