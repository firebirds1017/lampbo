package com.fengsheng.lnycjy.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * 源自：http://m.blog.csdn.net/article/details?id=51501875
 * 使用方法：
 * public class MainActivity extends AppCompatActivity {
 *
 * @Override protected void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * FrameLayout content = new FrameLayout(this);
 * content.setOnClickListener(null);
 * ImageView bg = new ImageView(this);
 * bg.setImageResource(R.drawable.fg);
 * bg.setScaleType(ImageView.ScaleType.FIT_XY);
 * content.addView(bg);
 * final AnimationLoading loading = new AnimationLoading(this);
 * content.addView(loading);
 * setContentView(content);
 * <p>
 * new Handler().postDelayed(new Runnable() {
 * @Override public void run() {
 * //3s后停止加载动画
 * loading.stopLoading();
 * }
 * },3000);
 * }
 * }
 * Created by guowe on 2017/1/25.
 */

public class AnimationLoading extends View {
    private float mBigCircleRaduis = 90;//大圆的半径
    private float mSubCircleRadius = 20;//小圆的半径
    private PointF mBigCenterPoint;//大圆的圆心坐标
    private Paint mBgPaint;//绘制背景的画笔
    private Paint mFgPaint;//绘制前景色的画笔
    private AnimatorTemplet mTemplet;//动画模板
    float mBigCircleRotateAngle;//大圆旋转的角度
    float mDiagonalDist;//屏幕对角线一半的距离
    float mBgStrokeCircleRadius;//用于作为绘制背景空心圆的半径
    //6个小圆的颜色
    private int[] colors = new int[]{Color.RED, Color.DKGRAY, Color.YELLOW, Color.BLUE, Color.LTGRAY, Color.GREEN};

    public AnimationLoading(Context context) {
        this(context, null);
    }

    public AnimationLoading(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //确定大圆的圆心坐标
        mBigCenterPoint.x = w / 2f;
        mBigCenterPoint.y = h / 2f;
        //屏幕对角线的一半
        mDiagonalDist = (float) (Math.sqrt(w * w + h * h) / 2);
    }

    private void init() {
        mBigCenterPoint = new PointF();
        mFgPaint = new Paint();
        mFgPaint.setAntiAlias(true);
        mBgPaint = new Paint(mFgPaint);
        mBgPaint.setColor(Color.WHITE);
        mBgPaint.setStyle(Paint.Style.STROKE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null == mTemplet) {
            //开启旋转动画
            mTemplet = new RotateState();
        }
        //传递Canvas对象
        mTemplet.drawState(canvas);
    }

    /**
     * 绘制圆
     *
     * @param canvas
     */
    private void drawCircle(Canvas canvas) {
        //获取每个小圆间隔的角度
        float rotateAngle = (float) (2 * Math.PI / colors.length);
        for (int i = 0; i < colors.length; i++) {
            //每个小圆的实际角度
            double angle = rotateAngle * i + mBigCircleRotateAngle;  //这里加上大圆旋转的角度是为了带动小圆一起旋转
            //计算每个小圆的圆心坐标
            float cx = (float) (mBigCircleRaduis * Math.cos(angle)) + mBigCenterPoint.x;
            float cy = (float) (mBigCircleRaduis * Math.sin(angle)) + mBigCenterPoint.y;
            //绘制6个小圆
            mFgPaint.setColor(colors[i]);
            canvas.drawCircle(cx, cy, mSubCircleRadius, mFgPaint);
        }
    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {
        if (mBgStrokeCircleRadius > 0f) {
            //不断扩散的空心圆,空心圆的半径为屏幕对角线的一半,空心圆的线宽则从线宽一半到0
            float strokeWidth = mDiagonalDist - mBgStrokeCircleRadius;//线宽从对角线的1/2 ~ 0
            mBgPaint.setStrokeWidth(strokeWidth);
            float radius = mBgStrokeCircleRadius + strokeWidth / 2;//半径从对角线的1/4 ~ 1/2
            canvas.drawCircle(mBigCenterPoint.x, mBigCenterPoint.y, radius, mBgPaint);
        } else {
            //绘制白色背景
            canvas.drawColor(Color.WHITE);

        }
    }

    private abstract class AnimatorTemplet {
        abstract void drawState(Canvas canvas);

    }

    /**
     * 绘制旋转动画
     */
    private class RotateState extends AnimatorTemplet {
        ValueAnimator mValueAnimator;

        public RotateState() {
            //旋转的过程,就是不断的获取大圆的角度,从0-2π
            mValueAnimator = ValueAnimator.ofFloat(0, (float) Math.PI * 2);
            mValueAnimator.setInterpolator(new LinearInterpolator());//匀速插值器
            mValueAnimator.setDuration(1200);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //获取大圆旋转的角度
                    mBigCircleRotateAngle = (float) animation.getAnimatedValue();
                    //重绘
                    invalidate();
                }
            });
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);//无限循环
            mValueAnimator.start();
        }

        /**
         * 停止旋转动画,在数据加载完毕后供外部调用
         */
        public void stopRotate() {
            mValueAnimator.cancel();
        }

        @Override
        void drawState(Canvas canvas) {
            drawBackground(canvas);
            drawCircle(canvas);
        }
    }

    /**
     * 绘制聚合动画
     */
    private class MergingState extends AnimatorTemplet {

        public MergingState() {
            //聚合的过程,就是不断的改变大圆的半径,从mBigCircleRaduis~0
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mBigCircleRaduis, 0);
            valueAnimator.setInterpolator(new OvershootInterpolator(10f));//弹性插值器
            valueAnimator.setDuration(600);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //获取大圆变化的半径
                    mBigCircleRaduis = (float) animation.getAnimatedValue();
                    //重绘
                    invalidate();
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //聚合执行完后进入下一个扩散动画
                    mTemplet = new SpreadState();

                }
            });
            valueAnimator.start();
        }

        @Override
        void drawState(Canvas canvas) {
            drawBackground(canvas);
            drawCircle(canvas);
        }
    }

    /**
     * 绘制扩散动画
     */
    private class SpreadState extends AnimatorTemplet {

        public SpreadState() {
            //扩散的过程,就是不断的改变背景画绘制空心圆的半径,从0~mDiagonalDist
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mDiagonalDist);
            valueAnimator.setDuration(600);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    //获取大圆变化的半径
                    mBgStrokeCircleRadius = (float) animation.getAnimatedValue();
                    //重绘
                    invalidate();
                }
            });

            valueAnimator.start();
        }

        @Override
        void drawState(Canvas canvas) {
            drawBackground(canvas);
        }
    }

    /**
     * 停止加载动画
     */
    public void stopLoading() {
        if (null != mTemplet && mTemplet instanceof RotateState) {
            ((RotateState) mTemplet).stopRotate();
            //开启下一个聚合动画
            post(new Runnable() {
                @Override
                public void run() {
                    mTemplet = new MergingState();
                }
            });
        }
    }
}
