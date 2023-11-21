/**
 *   File: MySeekBar.java
 *   Author: Xu Linrui <lrxu@smdt.com.cn>
 *   Created on 30 June 2021
 **/
package android.app.smdt.apidemo.ui;

import android.annotation.SuppressLint;
import android.app.smdt.apidemo.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
public class MySeekBar extends SeekBar {
    /**
     * SeekBar数值文字颜色
     */
    private int mTextColor;

    /**
     * SeekBar数值文字大小
     */
    private float mTextSize;

    /**
     * SeekBar数值文字内容
     */
    private String mText;

    /**
     * SeekBar数值文字背景
     */
    private Bitmap mBackgroundBitmap;

    /**
     * SeekBar数值文字背景宽高
     */
    private float mBgWidth, mBgHeight;

    /**
     * 画笔
     */
    private Paint mPaint;

    /**
     * SeekBar数值文字方向
     */
    private int mTextOrientation;

    /**
     * SeekBar数值文字宽度
     */
    private float mTextWidth;

    /**
     * SeekBar数值文字基线Y坐标
     */
    private float mTextBaseLineY;

    private Context mContext;
    private Canvas mCanvas;
    private boolean enable = false;

    private int min = 0;

    public MySeekBar(Context context) {
        this(context, null);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.seekbar);

        mBgWidth = mBackgroundBitmap.getWidth();
        mBgHeight = mBackgroundBitmap.getHeight();
        mTextColor = Color.BLACK;
        mTextSize = 16;

        //设置画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);

        setPadding((int) Math.ceil(mBgWidth) / 2, (int) Math.ceil(mBgHeight) + 5, (int) Math.ceil(mBgWidth) / 2, 10);

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(enable) {
            getTextLocation();
            Rect bgRect = getProgressDrawable().getBounds();
            //计算数值背景X坐标
            float bgX = bgRect.width() * getProgress() / getMax() ;
            //计算数值背景Y坐标
            float bgY = 0 ;
            //计算数值文字X坐标
            float textX = bgX + (mBgWidth - mTextWidth) / 2;
            float textY = (float) (mTextBaseLineY + bgY + (0.16 * mBgHeight / 2));

            //绘制文字和背景
            canvas.drawBitmap(mBackgroundBitmap, bgX, bgY, mPaint);
            canvas.drawText(mText, textX, textY, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                enable = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                enable = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 计算SeekBar数值文字的显示位置
     */
    private void getTextLocation() {
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        mText = getProgress() + min + "";
        //测量文字宽度
        mTextWidth = mPaint.measureText(mText);
        //计算文字基线Y坐标
        mTextBaseLineY = mBgHeight / 2 - fm.descent + (fm.descent - fm.ascent) / 2;
    }


    public void setMinValue(int min){
        this.min = min;
    }

    public int getMinValue(){
        return min;
    }

}
