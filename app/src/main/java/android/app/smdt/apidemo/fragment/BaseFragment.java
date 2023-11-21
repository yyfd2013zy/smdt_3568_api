/**
 * File: BaseFragment.java
 * Author: Xu Linrui <lrxu@smdt.com.cn>
 * Created on 20 July 2021
 **/
package android.app.smdt.apidemo.fragment;

import android.app.Activity;
import android.app.smdt.SmdtManagerNew;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

    public final static String TAG = "os_api";

    protected View view;
    public Context mContext;
    public Resources mRes;
    public SmdtManagerNew smdtManagerNew;

    private boolean isFirstVisible = true;
    private boolean isFirstInvisible = true;
    private boolean isPrepare = false;
    private boolean isPause = false;

    public void DEBUG(String msg) {
        DEBUG_TAG(TAG, msg);
    }

    public void DEBUG_TAG(String tag,String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mRes = getResources();
        smdtManagerNew = SmdtManagerNew.getInstance(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(getLayoutResource(), container, false);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isFirstVisible) {
            initPrepare();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            if(isPause) {
                onUserResume();
                isPause = false;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            onUserPause();
            isPause = true;
        }
    }

    /**
     * setUserVisibleHint是在onCreateView之前调用的
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (!isFirstVisible) {
                if (isPrepare) {
                    onUserResume();
                } else {
                    initPrepare();
                }
            } else {
                //第一次交给onActivityCreated
            }
        } else {
            if (!isFirstInvisible) {
                onUserPause();
            } else {
                isFirstInvisible = false;
            }
        }
    }

    public synchronized void initPrepare() {
        isFirstVisible = false;
        if (getUserVisibleHint()) {
            onInit();
            isPrepare = true;
        }
    }

    /**
     * 获取布局文件
     *
     * @return 布局文件
     */
    public abstract int getLayoutResource();

    /**
     * 初始化工作
     */
    public abstract void onInit();

    /**
     * fragment可见（切换回来或者onResume）
     */
    public abstract void onUserResume();

    /**
     * fragment不可见（切换掉或者onPause）
     */
    public void onUserPause(){
        hideInputKeyboard(mContext);
    }

    /**
     * 隐藏软键盘
     * @param context
     */
    public static void hideInputKeyboard(final Context context) {
        final Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InputMethodManager mInputKeyBoard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (activity.getCurrentFocus() != null) {
                    mInputKeyBoard.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    activity.getCurrentFocus().clearFocus();
                }
            }
        });
    }

    /**
     * 隐藏软键盘
     * @param context
     */
    public static void hideInputKeyboard(final Context context, View view) {
        final Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InputMethodManager mInputKeyBoard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (activity.getCurrentFocus() != null) {
                    mInputKeyBoard.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    view.clearFocus();
                }
            }
        });
    }

}
