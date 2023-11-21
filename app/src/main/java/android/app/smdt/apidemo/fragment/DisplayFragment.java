/**
 * File: DisplayFragment.java
 * Author: Xu Linrui <lrxu@smdt.com.cn>
 * Created on 21 June 2021
 **/
package android.app.smdt.apidemo.fragment;

import android.app.AlertDialog;
import android.app.smdt.apidemo.R;
import android.app.smdt.apidemo.ui.MySeekBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static android.app.smdt.util.ErrorCode.RET_API_OK;
import static android.app.smdt.util.VariableUtil.NAVIGATION_BAR;
import static android.app.smdt.util.VariableUtil.STATUS_BAR;

public class DisplayFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private SettingsObserver mSettingsObserver;

    //data
    private String parmas;
    private String resolution;
    private String resolution2;
    private String resolution3;
    private String resolution4;
    private int hdmiInStatus;
    private int rotation;
    private int rotation2;
    private int rotation3;
    private int rotation4;
    private int density;
    private int statusEnable;
    private int navigationEnable;
    private int gestureEnable;
    private int statusDragEnable;
    private int systemUIEnable;
    private int hdmiInAudioEnable;
    private int hdmiOutEnable;
    private int mediaModeEnable;
    private int[] scaleMain;
    private int[] scaleSec;
    private int[] scaleThird;
    private int[] scaleFth;
    private int currentLCD, currentEDP;
    private int minLCD, maxLCD;
    private int minEDP, maxEDP;
    private int frequency;
    private boolean drag_light = false;
    private boolean drag_light2 = false;
    private String bootLogo;
    private String bootAnimation;
    private int backlightEnable;
    private int backlightEnable2;
    //view
    private TextView get_screen_resolution, get_screen_resolution2, get_screen_resolution3, get_screen_resolution4;
    private LinearLayout line_overscan3,line_overscan4,line_rotation3,line_rotation4;
    //button
    private Button get_hdmiin, get_params;
    private TextView get_hdmiin_tv, get_params_tv;
    //button+EditText
    private Button screenshot, set_pwm_freq, add_hide_list, del_hide_list,
            set_over_scan, set_over_scan2, set_over_scan3, set_over_scan4,
            set_animation, set_logo, set_params;
    private EditText screenshot_et, set_pwm_freq_et, add_hide_list_et, del_hide_list_et,
            over_scan_left, over_scan_right, over_scan_top, over_scan_bottom,
            over_scan_left2, over_scan_right2, over_scan_top2, over_scan_bottom2,
            over_scan_left3, over_scan_right3, over_scan_top3, over_scan_bottom3,
            over_scan_left4, over_scan_right4, over_scan_top4, over_scan_bottom4,
            set_animation_et, set_logo_et, set_params_et;
    private TextView screenshot_tv, set_pwm_freq_tv, add_hide_list_tv, del_hide_list_tv,
            set_over_scan_tv, set_over_scan_tv2, set_over_scan_tv3, set_over_scan_tv4,
            set_animation_tv, set_logo_tv, set_params_tv;
    //button+Dialog
    private Button set_rotation, set_rotation2, set_rotation3, set_rotation4, set_dpi, get_hide_list;
    private TextView set_rotation_tv, set_rotation_tv2, set_rotation_tv3, set_rotation_tv4, set_dpi_tv, get_hide_list_tv;
    private AlertDialog rotationDialog, dpiDialog, hideAppDialog;
    //switch
    private Switch set_statusbar, set_navigationbar, set_gesturebar, set_statusbar_drag, set_systemui, set_hdmiin_audio, set_hdmiout_switch, set_media_mode_switch,
            set_backlight_enable, set_backlight_enable2;
    //seekBar
    private MySeekBar set_backlight, set_backlight2;
    private TextView set_backlight_tv, set_backlight_tv2;

    //Handler
    private static final int REFRESH_DATA = 0x00;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_DATA) {
                readData();
            }
        }
    };

    //Observer
    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            if (!TextUtils.isEmpty(STATUS_BAR)) {
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(STATUS_BAR),
                        false, this);
            }
            if (!TextUtils.isEmpty(NAVIGATION_BAR)) {
                mContext.getContentResolver().registerContentObserver(
                        Settings.System.getUriFor(NAVIGATION_BAR),
                        false, this);
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            set_statusbar.setChecked(smdtManagerNew.disp_getStatusBar() == 1);
            set_navigationbar.setChecked(smdtManagerNew.disp_getNavigationBar() == 1);
        }
    }

    @Override
    public int getLayoutResource() {
        return R.layout.display_fragment;
    }

    @Override
    public void onInit() {
        initView();
        onUserResume();
    }

    @Override
    public void onUserResume() {
        ReadThread mReadThread = new ReadThread();
        mReadThread.start();
    }

    @Override
    public void onUserPause() {
        super.onUserPause();
        if (mSettingsObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mSettingsObserver);
            mSettingsObserver = null;
        }
    }

    private void initView() {
        line_overscan3 = view.findViewById(R.id.line_overscan3);
        line_overscan4 = view.findViewById(R.id.line_overscan4);
        line_rotation3 = view.findViewById(R.id.line_rotation3);
        line_rotation4 = view.findViewById(R.id.line_rotation4);

        get_screen_resolution = view.findViewById(R.id.disp_get_screen_resolution);
        get_screen_resolution2 = view.findViewById(R.id.disp_get_screen_resolution2);
        get_screen_resolution3 = view.findViewById(R.id.disp_get_screen_resolution3);
        get_screen_resolution4 = view.findViewById(R.id.disp_get_screen_resolution4);

        get_hdmiin = view.findViewById(R.id.disp_get_hdmiin);
        get_hdmiin.setOnClickListener(this);
        get_hdmiin_tv = view.findViewById(R.id.disp_get_hdmiin_tv);
        get_params = view.findViewById(R.id.disp_get_params);
        get_params.setOnClickListener(this);
        get_params_tv = view.findViewById(R.id.disp_get_params_tv);

        screenshot = view.findViewById(R.id.disp_screenshot);
        screenshot.setOnClickListener(this);
        screenshot_et = view.findViewById(R.id.disp_screenshot_et);
        screenshot_tv = view.findViewById(R.id.disp_screenshot_tv);
        set_pwm_freq = view.findViewById(R.id.disp_set_pwm_freq);
        set_pwm_freq.setOnClickListener(this);
        set_pwm_freq_et = view.findViewById(R.id.disp_set_pwm_freq_et);
        set_pwm_freq_tv = view.findViewById(R.id.disp_set_pwm_freq_tv);
        add_hide_list = view.findViewById(R.id.disp_add_hide_list);
        add_hide_list.setOnClickListener(this);
        add_hide_list_et = view.findViewById(R.id.disp_add_hide_list_et);
        add_hide_list_tv = view.findViewById(R.id.disp_add_hide_list_tv);
        del_hide_list = view.findViewById(R.id.disp_del_hide_list);
        del_hide_list.setOnClickListener(this);
        del_hide_list_et = view.findViewById(R.id.disp_del_hide_list_et);
        del_hide_list_tv = view.findViewById(R.id.disp_del_hide_list_tv);
        set_over_scan = view.findViewById(R.id.disp_set_over_scan);
        set_over_scan.setOnClickListener(this);
        over_scan_left = view.findViewById(R.id.disp_over_scan_left);
        over_scan_right = view.findViewById(R.id.disp_over_scan_right);
        over_scan_top = view.findViewById(R.id.disp_over_scan_top);
        over_scan_bottom = view.findViewById(R.id.disp_over_scan_bottom);
        set_over_scan_tv = view.findViewById(R.id.disp_set_over_scan_tv);
        set_over_scan2 = view.findViewById(R.id.disp_set_over_scan2);
        set_over_scan2.setOnClickListener(this);
        over_scan_left2 = view.findViewById(R.id.disp_over_scan_left2);
        over_scan_right2 = view.findViewById(R.id.disp_over_scan_right2);
        over_scan_top2 = view.findViewById(R.id.disp_over_scan_top2);
        over_scan_bottom2 = view.findViewById(R.id.disp_over_scan_bottom2);
        set_over_scan_tv2 = view.findViewById(R.id.disp_set_over_scan_tv2);
        set_over_scan3 = view.findViewById(R.id.disp_set_over_scan3);
        set_over_scan3.setOnClickListener(this);
        over_scan_left3 = view.findViewById(R.id.disp_over_scan_left3);
        over_scan_right3 = view.findViewById(R.id.disp_over_scan_right3);
        over_scan_top3 = view.findViewById(R.id.disp_over_scan_top3);
        over_scan_bottom3 = view.findViewById(R.id.disp_over_scan_bottom3);
        set_over_scan_tv3 = view.findViewById(R.id.disp_set_over_scan_tv3);
        set_over_scan4 = view.findViewById(R.id.disp_set_over_scan4);
        set_over_scan4.setOnClickListener(this);
        over_scan_left4 = view.findViewById(R.id.disp_over_scan_left4);
        over_scan_right4 = view.findViewById(R.id.disp_over_scan_right4);
        over_scan_top4 = view.findViewById(R.id.disp_over_scan_top4);
        over_scan_bottom4 = view.findViewById(R.id.disp_over_scan_bottom4);
        set_over_scan_tv4 = view.findViewById(R.id.disp_set_over_scan_tv4);
        set_animation = view.findViewById(R.id.disp_set_animation);
        set_animation.setOnClickListener(this);
        set_animation_et = view.findViewById(R.id.disp_set_animation_et);
        set_animation_tv = view.findViewById(R.id.disp_set_animation_tv);
        set_logo = view.findViewById(R.id.disp_set_logo);
        set_logo.setOnClickListener(this);
        set_logo_et = view.findViewById(R.id.disp_set_logo_et);
        set_logo_tv = view.findViewById(R.id.disp_set_logo_tv);
        set_params = view.findViewById(R.id.disp_set_params);
        set_params.setOnClickListener(this);
        set_params_et = view.findViewById(R.id.disp_set_params_et);
        set_params_tv = view.findViewById(R.id.disp_set_params_tv);

        set_rotation = view.findViewById(R.id.disp_set_rotation);
        set_rotation.setOnClickListener(this);
        set_rotation_tv = view.findViewById(R.id.disp_set_rotation_tv);
        set_rotation2 = view.findViewById(R.id.disp_set_rotation2);
        set_rotation2.setOnClickListener(this);
        set_rotation_tv2 = view.findViewById(R.id.disp_set_rotation_tv2);
        set_rotation3 = view.findViewById(R.id.disp_set_rotation3);
        set_rotation3.setOnClickListener(this);
        set_rotation_tv3 = view.findViewById(R.id.disp_set_rotation_tv3);
        set_rotation4 = view.findViewById(R.id.disp_set_rotation4);
        set_rotation4.setOnClickListener(this);
        set_rotation_tv4 = view.findViewById(R.id.disp_set_rotation_tv4);
        set_dpi = view.findViewById(R.id.disp_set_dpi);
        set_dpi.setOnClickListener(this);
        set_dpi_tv = view.findViewById(R.id.disp_set_dpi_tv);
        get_hide_list = view.findViewById(R.id.disp_get_hide_list);
        get_hide_list.setOnClickListener(this);
        get_hide_list_tv = view.findViewById(R.id.disp_get_hide_list_tv);

        set_statusbar = view.findViewById(R.id.disp_set_statusbar_switch);
        set_navigationbar = view.findViewById(R.id.disp_set_navigationbar_switch);
        set_gesturebar = view.findViewById(R.id.disp_set_gesturebar_switch);
        set_statusbar_drag = view.findViewById(R.id.disp_set_statusbar_drag_switch);
        set_systemui = view.findViewById(R.id.disp_set_systemui_switch);
        set_hdmiin_audio = view.findViewById(R.id.disp_set_hdmiin_audio_switch);
        set_hdmiout_switch = view.findViewById(R.id.disp_set_hdmiout_switch);
        set_media_mode_switch = view.findViewById(R.id.disp_set_media_mode_switch);
        set_backlight_enable = view.findViewById(R.id.disp_set_backlight_enable_switch);
        set_backlight_enable2 = view.findViewById(R.id.disp_set_backlight_enable_switch2);

        set_backlight = (MySeekBar) view.findViewById(R.id.disp_set_backlight_seekbar);
        set_backlight2 = (MySeekBar) view.findViewById(R.id.disp_set_backlight_seekbar2);
        set_backlight_tv = view.findViewById(R.id.disp_set_backlight_tv);
        set_backlight_tv2 = view.findViewById(R.id.disp_set_backlight_tv2);

        initSeekBarBacklight();
        initSeekBarBacklight2();
    }

    private void readData() {
        unInitListener();
        String hardware = Build.HARDWARE;
        get_params_tv.setText(parmas);
        get_screen_resolution.setText(resolution);
        get_screen_resolution2.setText(resolution2);
        if(!TextUtils.isEmpty(hardware) && hardware.contains("3588")) {
            get_screen_resolution3.setText(resolution3);
        } else {
            get_screen_resolution3.setVisibility(View.GONE);
        }
        if(!TextUtils.isEmpty(hardware) && hardware.contains("3588")) {
            get_screen_resolution4.setText(resolution4);
        } else {
            get_screen_resolution4.setVisibility(View.GONE);
        }
        get_hdmiin_tv.setText(hdmiInStatus + "");
        set_rotation_tv.setText(rotation + "");
        set_rotation_tv2.setText(rotation2 + "");
        if(rotation3 < 0) {
            line_rotation3.setVisibility(View.GONE);
        } else {
            set_rotation_tv3.setText(rotation3 + "");
        }
        if(rotation4 < 0) {
            line_rotation4.setVisibility(View.GONE);
        } else {
            set_rotation_tv4.setText(rotation4 + "");
        }
        set_dpi_tv.setText(density + "");

        set_statusbar.setChecked(statusEnable == 1);
        set_navigationbar.setChecked(navigationEnable == 1);
        set_gesturebar.setChecked(gestureEnable == 1);
        set_statusbar_drag.setChecked(statusDragEnable == 1);
        set_systemui.setChecked(systemUIEnable == 1);
        set_hdmiin_audio.setChecked(hdmiInAudioEnable == 1);
        set_hdmiout_switch.setChecked(hdmiOutEnable == 1);
        set_media_mode_switch.setChecked(mediaModeEnable == 1);
        set_backlight_enable.setChecked(backlightEnable == 1);
        set_backlight_enable2.setChecked(backlightEnable2 == 1);

        if (currentLCD >= 0) {
            set_backlight.setProgress(currentLCD - minLCD);
        }
        set_backlight_tv.setText(currentLCD + "");
        if (currentEDP >= 0) {
            set_backlight2.setProgress(currentEDP - minEDP);
        }
        set_backlight_tv2.setText(currentEDP + "");
        set_pwm_freq_tv.setText(frequency + "");

        if (scaleMain != null) {
            over_scan_left.setText(scaleMain[0] + "");
            over_scan_right.setText(scaleMain[1] + "");
            over_scan_top.setText(scaleMain[2] + "");
            over_scan_bottom.setText(scaleMain[3] + "");
        }
        if (scaleSec != null) {
            over_scan_left2.setText(scaleSec[0] + "");
            over_scan_right2.setText(scaleSec[1] + "");
            over_scan_top2.setText(scaleSec[2] + "");
            over_scan_bottom2.setText(scaleSec[3] + "");
        }
        if (scaleThird == null || scaleThird[0] < 0) {
            line_overscan3.setVisibility(View.GONE);
        } else {
            over_scan_left3.setText(scaleThird[0] + "");
            over_scan_right3.setText(scaleThird[1] + "");
            over_scan_top3.setText(scaleThird[2] + "");
            over_scan_bottom3.setText(scaleThird[3] + "");
        }
        if (scaleFth == null || scaleFth[0] < 0) {
            line_overscan4.setVisibility(View.GONE);
        } else {
            over_scan_left4.setText(scaleFth[0] + "");
            over_scan_right4.setText(scaleFth[1] + "");
            over_scan_top4.setText(scaleFth[2] + "");
            over_scan_bottom4.setText(scaleFth[3] + "");
        }

        set_animation_tv.setText(bootAnimation);
        set_logo_tv.setText(bootLogo);

        initListener();
        if (mSettingsObserver == null) {
            mSettingsObserver = new SettingsObserver(mHandler);
            mSettingsObserver.observe();
        }
    }

    private void initListener() {
        set_statusbar.setOnCheckedChangeListener(this);
        set_navigationbar.setOnCheckedChangeListener(this);
        set_gesturebar.setOnCheckedChangeListener(this);
        set_statusbar_drag.setOnCheckedChangeListener(this);
        set_systemui.setOnCheckedChangeListener(this);
        set_hdmiin_audio.setOnCheckedChangeListener(this);
        set_hdmiout_switch.setOnCheckedChangeListener(this);
        set_media_mode_switch.setOnCheckedChangeListener(this);
        set_backlight_enable.setOnCheckedChangeListener(this);
        set_backlight_enable2.setOnCheckedChangeListener(this);
    }

    private void unInitListener() {
        set_statusbar.setOnCheckedChangeListener(null);
        set_navigationbar.setOnCheckedChangeListener(null);
        set_gesturebar.setOnCheckedChangeListener(null);
        set_statusbar_drag.setOnCheckedChangeListener(null);
        set_systemui.setOnCheckedChangeListener(null);
        set_hdmiin_audio.setOnCheckedChangeListener(null);
        set_hdmiout_switch.setOnCheckedChangeListener(null);
        set_media_mode_switch.setOnCheckedChangeListener(null);
        set_backlight_enable.setOnCheckedChangeListener(null);
        set_backlight_enable2.setOnCheckedChangeListener(null);
    }

    @Override
    public void onClick(View view) {
        hideInputKeyboard(mContext);
        String[] items;
        switch (view.getId()) {
            case R.id.disp_get_params:
                get_params_tv.setText(smdtManagerNew.disp_getDispParams());
                break;
            case R.id.disp_screenshot:
                screenshot();
                break;
            case R.id.disp_get_hdmiin:
                get_hdmiin_tv.setText(smdtManagerNew.disp_getHdmiInStatus() + "");
                break;
            case R.id.disp_set_rotation:
                items = mRes.getStringArray(R.array.rotation_value);
                showRotationDialog(items, 0);
                break;
            case R.id.disp_set_rotation2:
                items = mRes.getStringArray(R.array.rotation_value);
                showRotationDialog(items, 1);
                break;
            case R.id.disp_set_rotation3:
                items = mRes.getStringArray(R.array.rotation_value);
                showRotationDialog(items, 2);
                break;
            case R.id.disp_set_rotation4:
                items = mRes.getStringArray(R.array.rotation_value);
                showRotationDialog(items, 3);
                break;
            case R.id.disp_set_dpi:
                showDpiDialog();
                break;
            case R.id.disp_set_pwm_freq:
                set_pwm_freq_tv.setText(smdtManagerNew.disp_getLcdPwmFrequency(1) + "");
                break;
            case R.id.disp_add_hide_list:
                addHideAppList();
                break;
            case R.id.disp_get_hide_list:
                showHideAppDialog();
                break;
            case R.id.disp_del_hide_list:
                delHideAppList();
                break;
            case R.id.disp_set_over_scan:
                String left = over_scan_left.getText().toString();
                String right = over_scan_right.getText().toString();
                String top = over_scan_top.getText().toString();
                String bottom = over_scan_bottom.getText().toString();
                setOverScan(0, set_over_scan_tv, left, right, top, bottom);
                break;
            case R.id.disp_set_over_scan2:
                String left2 = over_scan_left2.getText().toString();
                String right2 = over_scan_right2.getText().toString();
                String top2 = over_scan_top2.getText().toString();
                String bottom2 = over_scan_bottom2.getText().toString();
                setOverScan(1, set_over_scan_tv2, left2, right2, top2, bottom2);
                break;
            case R.id.disp_set_over_scan3:
                String left3 = over_scan_left3.getText().toString();
                String right3 = over_scan_right3.getText().toString();
                String top3 = over_scan_top3.getText().toString();
                String bottom3 = over_scan_bottom3.getText().toString();
                setOverScan(2, set_over_scan_tv3, left3, right3, top3, bottom3);
                break;
            case R.id.disp_set_over_scan4:
                String left4 = over_scan_left4.getText().toString();
                String right4 = over_scan_right4.getText().toString();
                String top4 = over_scan_top4.getText().toString();
                String bottom4 = over_scan_bottom4.getText().toString();
                setOverScan(3, set_over_scan_tv4, left4, right4, top4, bottom4);
                break;
            case R.id.disp_set_animation:
                setBootAnimation();
                break;
            case R.id.disp_set_logo:
                setBootLogo();
                break;
            case R.id.disp_set_params:
                setParams();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        hideInputKeyboard(mContext);
        if (!compoundButton.isPressed()) {
            DEBUG("Display isPressed from code or what?:" + compoundButton.isPressed());
            return;
        }

        switch (compoundButton.getId()) {
            case R.id.disp_set_statusbar_switch:
                //失败反选
                if (smdtManagerNew.disp_setStatusBar(isChecked) != 0) {
                    set_statusbar.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_navigationbar_switch:
                if (smdtManagerNew.disp_setNavigationBar(isChecked) != 0) {
                    set_navigationbar.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_gesturebar_switch:
                if (smdtManagerNew.disp_setGestureBar(isChecked) != 0) {
                    set_gesturebar.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_statusbar_drag_switch:
                if (smdtManagerNew.disp_setStatusBarDrag(isChecked) != 0) {
                    set_statusbar_drag.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_systemui_switch:
                if (smdtManagerNew.disp_setSystemUIMode(isChecked) != 0) {
                    set_systemui.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_hdmiin_audio_switch:
                if (smdtManagerNew.disp_setHdmiInAudio(isChecked) != 0) {
                    set_hdmiin_audio.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_hdmiout_switch:
                if (smdtManagerNew.disp_setHdmiOutStatus(isChecked) != 0) {
                    set_hdmiout_switch.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_media_mode_switch:
                if (smdtManagerNew.disp_setMediaMode(isChecked) != 0) {
                    set_media_mode_switch.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_backlight_enable_switch:
                //失败反选
                if (smdtManagerNew.disp_setLcdBackLightEnable(0, isChecked) != 0) {
                    set_backlight_enable.setChecked(!isChecked);
                }
                break;
            case R.id.disp_set_backlight_enable_switch2:
                //失败反选
                if (smdtManagerNew.disp_setLcdBackLightEnable(1, isChecked) != 0) {
                    set_backlight_enable2.setChecked(!isChecked);
                }
                break;
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            parmas = smdtManagerNew.disp_getDispParams();
            resolution = smdtManagerNew.disp_getScreenWidth(0) + "*" + smdtManagerNew.disp_getScreenHeight(0);
            resolution2 = smdtManagerNew.disp_getScreenWidth(1) + "*" + smdtManagerNew.disp_getScreenHeight(1);
            resolution3 = smdtManagerNew.disp_getScreenWidth(2) + "*" + smdtManagerNew.disp_getScreenHeight(2);
            resolution4 = smdtManagerNew.disp_getScreenWidth(3) + "*" + smdtManagerNew.disp_getScreenHeight(3);
            hdmiInStatus = smdtManagerNew.disp_getHdmiInStatus();
            rotation = smdtManagerNew.disp_getDisplayRotation(0);
            rotation2 = smdtManagerNew.disp_getDisplayRotation(1);
            rotation3 = smdtManagerNew.disp_getDisplayRotation(2);
            rotation4 = smdtManagerNew.disp_getDisplayRotation(3);
            density = smdtManagerNew.disp_getDisplayDensity();
            statusEnable = smdtManagerNew.disp_getStatusBar();
            navigationEnable = smdtManagerNew.disp_getNavigationBar();
            gestureEnable = smdtManagerNew.disp_getGestureBar();
            statusDragEnable = smdtManagerNew.disp_getStatusBarDrag();
            systemUIEnable = smdtManagerNew.disp_getSystemUIMode();
            hdmiInAudioEnable = smdtManagerNew.disp_getHdmiInAudio();
            hdmiOutEnable = smdtManagerNew.disp_getHdmiOutStatus();
            mediaModeEnable = smdtManagerNew.disp_getMediaMode();
            currentLCD = smdtManagerNew.disp_getLcdBackLight(0);
            currentEDP = smdtManagerNew.disp_getLcdBackLight(1);
            frequency = smdtManagerNew.disp_getLcdPwmFrequency(1);
            scaleMain = smdtManagerNew.disp_getDisplayOverScan(0);
            scaleSec = smdtManagerNew.disp_getDisplayOverScan(1);
            scaleThird = smdtManagerNew.disp_getDisplayOverScan(2);
            scaleFth = smdtManagerNew.disp_getDisplayOverScan(3);
            bootLogo = smdtManagerNew.disp_getBootLogo();
            bootAnimation = smdtManagerNew.disp_getBootAnimation();
            backlightEnable = smdtManagerNew.disp_getLcdBackLightEnable(0);
            backlightEnable2 = smdtManagerNew.disp_getLcdBackLightEnable(1);
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }

    private void screenshot() {
        String path = screenshot_et.getText().toString();
        screenshot_tv.setText(smdtManagerNew.disp_getScreenShot(path) + "");
    }

    private void showRotationDialog(String[] items, int screen_id) {
        int value = smdtManagerNew.disp_getDisplayRotation(screen_id);
        switch (value) {
            case 0:
                value = 0;
                break;
            case 90:
                value = 1;
                break;
            case 180:
                value = 2;
                break;
            case 270:
                value = 3;
                break;
            default:
                value = 0;
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        if (screen_id == 0) {
            alertBuilder.setTitle(mRes.getString(R.string.disp_set_rotation));
        } else if (screen_id == 1) {
            alertBuilder.setTitle(mRes.getString(R.string.disp_set_rotation2));
        } else if (screen_id == 2) {
            alertBuilder.setTitle(mRes.getString(R.string.disp_set_rotation3));
        } else if (screen_id == 3) {
            alertBuilder.setTitle(mRes.getString(R.string.disp_set_rotation4));
        } else {
            return;
        }
        alertBuilder.setSingleChoiceItems(items, value, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int rotation = Integer.parseInt(items[i]);
                int result = smdtManagerNew.disp_setDisplayRotation(screen_id, rotation);

                if (result == 0) {
                    if (screen_id == 0) {
                        set_rotation_tv.setText(smdtManagerNew.disp_getDisplayRotation(0) + "");
                    } else if (screen_id == 1) {
                        set_rotation_tv2.setText(smdtManagerNew.disp_getDisplayRotation(1) + "");
                    } else if (screen_id == 2) {
                        set_rotation_tv3.setText(smdtManagerNew.disp_getDisplayRotation(2) + "");
                    } else if (screen_id == 3) {
                        set_rotation_tv4.setText(smdtManagerNew.disp_getDisplayRotation(3) + "");
                    }
                } else {
                    if (screen_id == 0) {
                        set_rotation_tv.setText(result + "");
                    } else if (screen_id == 1) {
                        set_rotation_tv2.setText(result + "");
                    } else if (screen_id == 2) {
                        set_rotation_tv3.setText(result + "");
                    } else {
                        set_rotation_tv4.setText(result + "");
                    }
                }
                rotationDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rotationDialog.dismiss();
            }
        });

        rotationDialog = alertBuilder.create();
        rotationDialog.show();
    }

    private void showDpiDialog() {
        String[] items = mRes.getStringArray(R.array.dpi_value);
        int value = smdtManagerNew.disp_getDisplayDensity();
        switch (value) {
            case 120:
                value = 0;
                break;
            case 160:
                value = 1;
                break;
            case 200:
                value = 2;
                break;
            case 240:
                value = 3;
                break;
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setSingleChoiceItems(items, value, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int dpi = Integer.parseInt(items[i]);
                int result = smdtManagerNew.disp_setDisplayDensity(dpi);
                if (result == 0) {
                    set_dpi_tv.setText(smdtManagerNew.disp_getDisplayDensity() + "");
                } else {
                    set_dpi_tv.setText(result + "");
                }
                dpiDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dpiDialog.dismiss();
            }
        });

        dpiDialog = alertBuilder.create();
        dpiDialog.show();
    }

    private void initSeekBarBacklight() {
        minLCD = smdtManagerNew.disp_getLcdBackLightMaxMin(0, "min");
        maxLCD = smdtManagerNew.disp_getLcdBackLightMaxMin(0, "max") - minLCD;
        // 设置SeekBar监听事件
        set_backlight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                if (drag_light) {
                    currentLCD = value + minLCD;
                    int result = smdtManagerNew.disp_setLcdBackLight(0, currentLCD, 0, false);
                    if (result == 0) {
                        set_backlight_tv.setText(currentLCD + "");
                    } else {
                        set_backlight_tv.setText(result + "");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideInputKeyboard(mContext);
                drag_light = true;
                set_backlight_tv.setText(currentLCD + "");
/*                int result = smdtManagerNew.disp_setLcdBackLight(0, currentLCD, 0, false);
                if (result == 0) {
                    set_backlight_tv.setText(currentLCD + "");
                } else {
                    set_backlight_tv.setText(result + "");
                }*/
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drag_light = false;
                int result = smdtManagerNew.disp_setLcdBackLight(0, currentLCD, 0, true);
                if (result == 0) {
                    set_backlight_tv.setText(currentLCD + "");
                } else {
                    set_backlight_tv.setText(result + "");
                }
            }
        });

        if (minLCD >= 0 && maxLCD > 0) {
            set_backlight.setMinValue(minLCD);
            set_backlight.setMax(maxLCD);
        }

    }

    private void initSeekBarBacklight2() {
        minEDP = smdtManagerNew.disp_getLcdBackLightMaxMin(1, "min");
        maxEDP = smdtManagerNew.disp_getLcdBackLightMaxMin(1, "max") - minEDP;
        // 设置SeekBar监听事件
        set_backlight2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                if (drag_light2) {
                    currentEDP = value + minEDP;
                    set_backlight_tv2.setText(currentEDP + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideInputKeyboard(mContext);
                drag_light2 = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drag_light2 = false;
                String str = set_pwm_freq_et.getText().toString();
                if (!TextUtils.isEmpty(str)) {
                    frequency = Integer.parseInt(str);
                }
                int result = smdtManagerNew.disp_setLcdBackLight(1, currentEDP, frequency, true);
                if (result == 0) {
                    set_backlight_tv2.setText(currentEDP + "");
                } else {
                    set_backlight_tv2.setText(result + "");
                }
            }
        });

        if (minEDP >= 0 && maxEDP > 0) {
            set_backlight2.setMinValue(minEDP);
            set_backlight2.setMax(maxEDP);
        }
    }

    private void addHideAppList() {
        String packageName = add_hide_list_et.getText().toString();
        add_hide_list_tv.setText(smdtManagerNew.disp_addAppLauncherHideList(packageName) + "");
    }

    private void showHideAppDialog() {
        List<String> list = smdtManagerNew.disp_getAppLauncherHideList();
        if (list == null || list.size() == 0) {
            get_hide_list_tv.setText("-1");
            return;
        }
        get_hide_list_tv.setText("");

        String[] hidelist = new String[list.size()];
        list.toArray(hidelist);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mRes.getString(R.string.long_click_msg));
        alertBuilder.setItems(hidelist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获取剪贴板管理器
                ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                //创建能够存入剪贴板的ClipData对象
                ClipData mClipData = ClipData.newPlainText("copydata", hidelist[which]);
                //将ClipData数据复制到剪贴板：
                mClipboardManager.setPrimaryClip(mClipData);
                Toast.makeText(mContext, mRes.getString(R.string.long_click_toast), Toast.LENGTH_SHORT).show();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideAppDialog.dismiss();
            }
        });

        hideAppDialog = alertBuilder.create();
        hideAppDialog.show();


    }

    private void delHideAppList() {
        String packageName = del_hide_list_et.getText().toString();
        del_hide_list_tv.setText(smdtManagerNew.disp_delAppLauncherHideList(packageName) + "");
    }

    private void setOverScan(int screenID, TextView tv, String left, String right, String top, String bottom) {
        if (TextUtils.isEmpty(left) || TextUtils.isEmpty(right)
                || TextUtils.isEmpty(top) || TextUtils.isEmpty(bottom)) {
            tv.setText("-2");
            return;
        }
        smdtManagerNew.disp_setDisplayOverScan(screenID, "left", Integer.parseInt(left));
        smdtManagerNew.disp_setDisplayOverScan(screenID, "right", Integer.parseInt(right));
        smdtManagerNew.disp_setDisplayOverScan(screenID, "top", Integer.parseInt(top));
        int result = smdtManagerNew.disp_setDisplayOverScan(screenID, "bottom", Integer.parseInt(bottom));
        tv.setText(result + "");
    }

    private void setBootAnimation() {
        String path = set_animation_et.getText().toString();
        int result = smdtManagerNew.disp_setBootAnimation(path);
        if (result == RET_API_OK && !TextUtils.isEmpty(path)) {
            set_animation_tv.setText(smdtManagerNew.disp_getBootAnimation());
        } else {
            set_animation_tv.setText(result + "");
        }
    }

    private void setBootLogo() {
        String path = set_logo_et.getText().toString();
        int result = smdtManagerNew.disp_setBootLogo(path);
        if (result == RET_API_OK && !TextUtils.isEmpty(path)) {
            set_logo_tv.setText(smdtManagerNew.disp_getBootLogo());
        } else {
            set_logo_tv.setText(result + "");
        }
    }

    private void setParams() {
        String params = set_params_et.getText().toString();
        int result = smdtManagerNew.disp_setDispParams(params);
        set_params_tv.setText(result + "");
    }

}
