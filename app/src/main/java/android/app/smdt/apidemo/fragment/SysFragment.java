/**
 * File: SysFragment.java
 * Author: Xu Linrui <lrxu@smdt.com.cn>
 * Created on 1 July 2021
 **/
package android.app.smdt.apidemo.fragment;

import android.app.AlertDialog;
import android.app.smdt.SmdtManagerNew;
import android.app.smdt.apidemo.R;
import android.app.smdt.apidemo.ui.MySeekBar;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.smdt.util.ErrorCode.RET_API_ERR_NG;
import static android.app.smdt.util.ErrorCode.RET_API_ERR_PARA;
import static android.app.smdt.util.ErrorCode.RET_API_OK;
import static android.app.smdt.util.MethodUtil.SERVICENAME;
import static android.app.smdt.util.TimerCode.ERROR_CONFIG;
import static android.app.smdt.util.VariableUtil.ADB_NETWORK;
import static android.app.smdt.util.VariableUtil.ADB_USB;
import static android.app.smdt.util.VariableUtil.BLACK_LIST;
import static android.app.smdt.util.VariableUtil.INSTALL_LIST;
import static android.app.smdt.util.VariableUtil.LOG_ANDROID;
import static android.app.smdt.util.VariableUtil.LOG_APP;
import static android.app.smdt.util.VariableUtil.LOG_CONFIG;
import static android.app.smdt.util.VariableUtil.LOG_KERNEL;
import static android.app.smdt.util.VariableUtil.LOG_MCU;
import static android.app.smdt.util.VariableUtil.LOG_MEDIA;
import static android.app.smdt.util.VariableUtil.LOG_MISC;
import static android.app.smdt.util.VariableUtil.LOG_PROP;
import static android.app.smdt.util.VariableUtil.LOG_RADIO;
import static android.app.smdt.util.VariableUtil.OTG_DEVICE;
import static android.app.smdt.util.VariableUtil.OTG_HOST;
import static android.app.smdt.util.VariableUtil.UNINSTALL_LIST;
import static android.app.smdt.util.VariableUtil.WHITE_LIST;

public class SysFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener, RadioGroup.OnCheckedChangeListener {

    private final String TAG = "SysFragment";

    private IntentFilter mIntentFilter;
    private SmdtReceiver mSmdtReceiver;
    //data
    private String ntpServer;
    private String inputMethod;
    private String timeZone;
    private String timeFormat;
    private String launcher;
    private String bootApp;
    private String national;
    private float fontSize;
    private int debugLevel;
    private String daemonsActivity;
    private int btEnable;
    private int location;
    private int airPlaneEnable;
    private int hwStackEnable;
    private int keyboardEnable;
    private int keyReportEnable;
    private int touchReportEnable;
    private int networkTimeSyncEnable;
    private int watchDogEnable;
    private int developmentEnable;
    private int systemLogEnable;
    private int adbEnable;
    private int adbNetEnable;
    private int OTGEnable;
    private int pointerEnbale;
    private int floatBallEnbale;
    private int volueMuteEnbale;
    private int autoinstallEnbale;
    private int controlEnbale;
    private int updateExEnable;
    /*private int gpioDirection;
    private int gpioValue;
    private String[] gpio_List;
    private int gpio;*/
    private int volume_output,volume_input;
    private int volume_max, volume_min;
    private int volume_value;
    private boolean drag_volume = false;
    private int volume_gain_max, volume_gain_min;
    private int volume_gain_value;
    private boolean drag_volume_gain = false;
    private boolean get_log_single = false;
    private int timerEnable, timerOnEnable, timerOffEnable;
    private String bootTime, shutdownTime, onTime, offTime;
    private String[] inputMethodList;
    private int[] controlValues;
    //view
    //button
    private Button set_watchdog_feed, set_power_off, set_reboot, recovery, get_processlog, set_gpio, set_reboot_mcu;
    private TextView set_watchdog_feed_tv, set_power_off_tv, set_reboot_tv, recovery_tv, get_processlog_tv, set_reboot_mcu_tv;
    //button+editText
    private Button set_time, set_system_boot_app, get_api_error, add_white_list, del_white_list, add_black_list, del_black_list,
            do_update, set_daemons, do_silent_install, do_silent_uninstall, copy_file, set_backup, get_backup,
            get_anr, add_auto_install, del_auto_install;
    private EditText set_time_year_et, set_time_month_et, set_time_day_et, set_time_hour_et, set_time_min_et,
            set_system_boot_app_et, get_api_error_et, add_white_list_et, del_white_list_et, add_black_list_et, del_black_list_et,
            do_update_et, set_daemons_et, set_daemons_time_et, do_silent_install_et, do_silent_uninstall_et,
            copy_file_et1, copy_file_et2, set_backup_et, get_backup_et, get_anr_et, add_auto_install_path_et, add_auto_install_pkg_et,
            del_auto_install_path_et, del_auto_install_pkg_et;
    private TextView set_time_tv, set_system_boot_app_tv, get_api_error_tv,
            add_white_list_tv, del_white_list_tv, add_black_list_tv, del_black_list_tv,
            do_update_tv, set_daemons_tv, do_silent_install_tv, do_silent_uninstall_tv,
            copy_file_tv, set_backup_tv, get_backup_tv, get_backup_path, get_anr_tv,
            add_auto_install_tv, del_auto_install_tv;
    //button+spinner
    private Button set_national_language, set_def_inputmethod, set_ntp_server, set_timezone, set_timeformat, set_launcher;
    private Spinner set_national_language_spinner, set_def_inputmethod_spinner, set_ntp_server_spinner, set_timezone_spinner, set_timeformat_spinner, set_launcher_spinner;
    private TextView set_national_language_tv, set_def_inputmethod_tv, set_ntp_server_tv, set_timezone_tv, set_timeformat_tv, set_launcher_tv;
    //button+dialog
    private Button set_system_fontsize, set_api_debug, set_audio_output, set_audio_input, get_auto_install;
    private TextView set_system_fontsize_tv, set_api_debug_tv, set_audio_output_tv, set_audio_input_tv, get_auto_install_tv;
    private AlertDialog fontsizeDialog, apidebugDialog, audioOutputDialog,audioInputDialog, autoinstallDialog;
    //switch
    private Switch set_bluetooth, set_location, set_airplane, set_hwstack, set_softkeyboard, set_keyreport, set_touchreport,
            set_network_time_sync, set_watchdog, set_development, set_adb, set_adb_net, set_otg,
            set_pointer_switch, set_float_ball_switch, set_volume_mute_switch, set_auto_install_switch, set_update_ex_switch;
    //switch+editText
    private Switch set_timer, set_timer_on, set_timer_off;
    private EditText set_timer_on_time_et, set_timer_off_time_et,
            set_timer_off_year_et, set_timer_off_month_et, set_timer_off_day_et, set_timer_off_hour_et, set_timer_off_min_et,
            set_timer_on_year_et, set_timer_on_month_et, set_timer_on_day_et, set_timer_on_hour_et, set_timer_on_min_et;
    //button+radio+dialog
    private Button get_white_list, get_black_list;
    private RadioButton install_white, uninstall_white, install_black, uninstall_black;
    private TextView get_white_list_tv;
    private TextView get_black_list_tv;
    private AlertDialog appListDialog;
    //spinner+radio+switch
    /*private Spinner set_gpio_spinner;
    private RadioGroup set_gpio_direction_group;
    private RadioButton gpio_in, gpio_out;
    private ToggleButton set_gpio_value;
    private TextView set_gpio_tv;*/
    //spinner+switch
    private Spinner set_control_spinner, get_systemlog_spinner;
    private Switch set_control, get_systemlog;
    //seekBar
    private MySeekBar set_volume, set_out_volume;
    private TextView set_volume_tv, set_out_volume_tv;
    //Handler
    private static final int REFRESH_DATA = 0x00;
    private static final int REFRESH_INSTALL_APP = 0x01;
    private static final int REFRESH_UNINSTALL_APP = 0x02;
    private static final int REFRESH_COPY_PROGRESS = 0x03;
    private static final int REFRESH_COPY_FINISH = 0x04;
    private static final int REFRESH_BACKUP_PROGRESS = 0x05;
    private static final int REFRESH_BACKUP_FINISH = 0x06;
    private static final int REFRESH_RECOVERY_PROGRESS = 0x07;
    private static final int REFRESH_RECOVERY_FINISH = 0x08;
    private static final int REFRESH_BACKUP_PATH = 0x09;
    private static final int REFRESH_LOGCAT = 0x10;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_DATA) {
                //readData();
            } else if (msg.what == REFRESH_INSTALL_APP) {
                Bundle bundle = msg.getData();
                int returnCode = bundle.getInt("returnCode");
                do_silent_install_tv.setText(returnCode + "");
            } else if (msg.what == REFRESH_UNINSTALL_APP) {
                Bundle bundle = msg.getData();
                int returnCode = bundle.getInt("returnCode");
                do_silent_uninstall_tv.setText(returnCode + "");
            } else if (msg.what == REFRESH_COPY_PROGRESS) {
                Bundle bundle = msg.getData();
                int progress = bundle.getInt("progress");
                copy_file_tv.setText(progress + "");
            } else if (msg.what == REFRESH_COPY_FINISH) {
                Bundle bundle = msg.getData();
                int returnCode = bundle.getInt("returnCode");
                copy_file_tv.setText(returnCode + "");
            } else if (msg.what == REFRESH_BACKUP_PROGRESS) {
                Bundle bundle = msg.getData();
                int progress = bundle.getInt("progress");
                set_backup_tv.setText(progress + "");
            } else if (msg.what == REFRESH_BACKUP_FINISH) {
                Bundle bundle = msg.getData();
                int returnCode = bundle.getInt("returnCode");
                set_backup_tv.setText(returnCode + "");
            } else if (msg.what == REFRESH_RECOVERY_PROGRESS) {
                Bundle bundle = msg.getData();
                int progress = bundle.getInt("progress");
                get_backup_tv.setText(progress + "");
            } else if (msg.what == REFRESH_RECOVERY_FINISH) {
                Bundle bundle = msg.getData();
                int returnCode = bundle.getInt("returnCode");
                get_backup_tv.setText(returnCode + "");
            } else if (msg.what == REFRESH_BACKUP_PATH) {
                Bundle bundle = msg.getData();
                String path = bundle.getString("path");
                get_backup_path.setText(path + "");
            } else if (msg.what == REFRESH_LOGCAT) {
                if (get_processlog_tv != null && get_log_single) {
                    Bundle bundle = msg.getData();
                    String log = bundle.getString("log");
                    get_processlog_tv.append(log + "\n");
                }
            }
        }
    };

    /**
     * BroadcastReceiver 广播
     */
    private class SmdtReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                //刷新蓝牙状态变化
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (blueState == BluetoothAdapter.STATE_ON) {
                    set_bluetooth.setChecked(true);
                } else if (blueState == BluetoothAdapter.STATE_OFF) {
                    set_bluetooth.setChecked(false);
                }
            }
        }
    }

    @Override
    public int getLayoutResource() {
        return R.layout.sys_fragment;
    }

    @Override
    public void onInit() {
        initView();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        controlValues = mRes.getIntArray(R.array.control_value);
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
        if (mSmdtReceiver != null) {
            mContext.unregisterReceiver(mSmdtReceiver);
            mSmdtReceiver = null;
        }
        get_log_single = false;
        smdtManagerNew.sys_getProcessLogcat(null);
    }

    private void initView() {
        set_watchdog_feed = view.findViewById(R.id.sys_set_watchdog_feed);
        set_watchdog_feed.setOnClickListener(this);
        set_watchdog_feed_tv = view.findViewById(R.id.sys_set_watchdog_feed_tv);
        set_power_off = view.findViewById(R.id.sys_set_power_off);
        set_power_off.setOnClickListener(this);
        set_power_off_tv = view.findViewById(R.id.sys_set_power_off_tv);
        set_reboot = view.findViewById(R.id.sys_set_reboot);
        set_reboot.setOnClickListener(this);
        set_reboot_tv = view.findViewById(R.id.sys_set_reboot_tv);
        set_reboot_mcu = view.findViewById(R.id.sys_set_reboot_mcu);
        set_reboot_mcu.setOnClickListener(this);
        set_reboot_mcu_tv = view.findViewById(R.id.sys_set_reboot_mcu_tv);
        recovery = view.findViewById(R.id.sys_recovery);
        recovery.setOnClickListener(this);
        recovery_tv = view.findViewById(R.id.sys_recovery_tv);
        get_processlog = view.findViewById(R.id.sys_get_processlog);
        get_processlog.setOnClickListener(this);
        set_gpio = view.findViewById(R.id.sys_set_gpio);
        set_gpio.setOnClickListener(this);

        set_time = view.findViewById(R.id.sys_set_time);
        set_time.setOnClickListener(this);
        set_time_year_et = view.findViewById(R.id.sys_set_time_year_et);
        set_time_month_et = view.findViewById(R.id.sys_set_time_month_et);
        set_time_day_et = view.findViewById(R.id.sys_set_time_day_et);
        set_time_hour_et = view.findViewById(R.id.sys_set_time_hour_et);
        set_time_min_et = view.findViewById(R.id.sys_set_time_min_et);
        set_time_tv = view.findViewById(R.id.sys_set_time_tv);
        set_system_boot_app = view.findViewById(R.id.sys_set_system_boot_app);
        set_system_boot_app.setOnClickListener(this);
        set_system_boot_app_et = view.findViewById(R.id.sys_set_system_boot_app_et);
        set_system_boot_app_tv = view.findViewById(R.id.sys_set_system_boot_app_tv);
        get_api_error = view.findViewById(R.id.sys_get_api_error);
        get_api_error.setOnClickListener(this);
        get_api_error_et = view.findViewById(R.id.sys_get_api_error_et);
        get_api_error_tv = view.findViewById(R.id.sys_get_api_error_tv);
        add_white_list = view.findViewById(R.id.sys_add_white_list);
        add_white_list.setOnClickListener(this);
        add_white_list_et = view.findViewById(R.id.sys_add_white_list_et);
        add_white_list_tv = view.findViewById(R.id.sys_add_white_list_tv);
        del_white_list = view.findViewById(R.id.sys_del_white_list);
        del_white_list.setOnClickListener(this);
        del_white_list_et = view.findViewById(R.id.sys_del_white_list_et);
        del_white_list_tv = view.findViewById(R.id.sys_del_white_list_tv);
        add_black_list = view.findViewById(R.id.sys_add_black_list);
        add_black_list.setOnClickListener(this);
        add_black_list_et = view.findViewById(R.id.sys_add_black_list_et);
        add_black_list_tv = view.findViewById(R.id.sys_add_black_list_tv);
        del_black_list = view.findViewById(R.id.sys_del_black_list);
        del_black_list.setOnClickListener(this);
        del_black_list_et = view.findViewById(R.id.sys_del_black_list_et);
        del_black_list_tv = view.findViewById(R.id.sys_del_black_list_tv);
        do_update = view.findViewById(R.id.sys_do_update);
        do_update.setOnClickListener(this);
        do_update_et = view.findViewById(R.id.sys_do_update_et);
        do_update_tv = view.findViewById(R.id.sys_do_update_tv);
        set_daemons = view.findViewById(R.id.sys_set_daemons);
        set_daemons.setOnClickListener(this);
        set_daemons_et = view.findViewById(R.id.sys_set_daemons_et);
        set_daemons_time_et = view.findViewById(R.id.sys_set_daemons_time_et);
        set_daemons_tv = view.findViewById(R.id.sys_set_daemons_tv);
        do_silent_install = view.findViewById(R.id.sys_do_silent_install);
        do_silent_install.setOnClickListener(this);
        do_silent_install_et = view.findViewById(R.id.sys_do_silent_install_et);
        do_silent_install_tv = view.findViewById(R.id.sys_do_silent_install_tv);
        do_silent_uninstall = view.findViewById(R.id.sys_do_silent_uninstall);
        do_silent_uninstall.setOnClickListener(this);
        do_silent_uninstall_et = view.findViewById(R.id.sys_do_silent_uninstall_et);
        do_silent_uninstall_tv = view.findViewById(R.id.sys_do_silent_uninstall_tv);
        copy_file = view.findViewById(R.id.sys_copy_file);
        copy_file.setOnClickListener(this);
        copy_file_et1 = view.findViewById(R.id.sys_copy_file_et1);
        copy_file_et2 = view.findViewById(R.id.sys_copy_file_et2);
        copy_file_tv = view.findViewById(R.id.sys_copy_file_tv);
        set_backup = view.findViewById(R.id.sys_set_backup);
        set_backup.setOnClickListener(this);
        set_backup_et = view.findViewById(R.id.sys_set_backup_et);
        set_backup_tv = view.findViewById(R.id.sys_set_backup_tv);
        get_backup = view.findViewById(R.id.sys_get_backup);
        get_backup.setOnClickListener(this);
        get_backup_et = view.findViewById(R.id.sys_get_backup_et);
        get_backup_tv = view.findViewById(R.id.sys_get_backup_tv);
        get_backup_path = view.findViewById(R.id.sys_get_backup_path);
        get_anr = view.findViewById(R.id.sys_get_anr);
        get_anr.setOnClickListener(this);
        get_anr_et = view.findViewById(R.id.sys_get_anr_et);
        get_anr_tv = view.findViewById(R.id.sys_get_anr_tv);
        add_auto_install = view.findViewById(R.id.sys_add_auto_install);
        add_auto_install.setOnClickListener(this);
        add_auto_install_path_et = view.findViewById(R.id.sys_add_auto_install_path_et);
        add_auto_install_pkg_et = view.findViewById(R.id.sys_add_auto_install_pkg_et);
        add_auto_install_tv = view.findViewById(R.id.sys_add_auto_install_tv);
        del_auto_install = view.findViewById(R.id.sys_del_auto_install);
        del_auto_install.setOnClickListener(this);
        del_auto_install_path_et = view.findViewById(R.id.sys_del_auto_install_path_et);
        del_auto_install_pkg_et = view.findViewById(R.id.sys_del_auto_install_pkg_et);
        del_auto_install_tv = view.findViewById(R.id.sys_del_auto_install_tv);

        set_system_fontsize = view.findViewById(R.id.sys_set_system_fontsize);
        set_system_fontsize.setOnClickListener(this);
        set_system_fontsize_tv = view.findViewById(R.id.sys_set_system_fontsize_tv);
        set_api_debug = view.findViewById(R.id.sys_set_api_debug);
        set_api_debug.setOnClickListener(this);
        set_api_debug_tv = view.findViewById(R.id.sys_set_api_debug_tv);
        set_audio_output = view.findViewById(R.id.sys_set_audio_output);
        set_audio_output.setOnClickListener(this);
        set_audio_output_tv = view.findViewById(R.id.sys_set_audio_output_tv);
        set_audio_input = view.findViewById(R.id.sys_set_audio_input);
        set_audio_input.setOnClickListener(this);
        set_audio_input_tv = view.findViewById(R.id.sys_set_audio_input_tv);
        get_auto_install = view.findViewById(R.id.sys_get_auto_install);
        get_auto_install.setOnClickListener(this);
        get_auto_install_tv = view.findViewById(R.id.sys_get_auto_install_tv);

        set_bluetooth = view.findViewById(R.id.sys_set_bluetooth_switch);
        set_location = view.findViewById(R.id.sys_set_location_switch);
        set_airplane = view.findViewById(R.id.sys_set_airplane_switch);
        set_hwstack = view.findViewById(R.id.sys_set_hwstack_switch);
        set_softkeyboard = view.findViewById(R.id.sys_set_softkeyboard_switch);
        set_keyreport = view.findViewById(R.id.sys_set_keyreport_switch);
        set_touchreport = view.findViewById(R.id.sys_set_touchreport_switch);
        set_network_time_sync = view.findViewById(R.id.sys_set_network_time_sync_switch);
        set_watchdog = view.findViewById(R.id.sys_set_watchdog_switch);
        set_development = view.findViewById(R.id.sys_set_development_switch);
        set_adb = view.findViewById(R.id.sys_set_adb_switch);
        set_adb_net = view.findViewById(R.id.sys_set_adb_net_switch);
        set_otg = view.findViewById(R.id.sys_set_otg_switch);
        set_pointer_switch = view.findViewById(R.id.sys_set_pointer_switch);
        set_float_ball_switch = view.findViewById(R.id.sys_set_float_ball_switch);
        set_volume_mute_switch = view.findViewById(R.id.sys_set_volume_mute_switch);
        set_auto_install_switch = view.findViewById(R.id.sys_set_auto_install_switch);
        set_update_ex_switch = view.findViewById(R.id.sys_set_update_ex_switch);

        set_timer = view.findViewById(R.id.sys_set_timer);
        set_timer_on_time_et = view.findViewById(R.id.sys_set_timer_on_time_et);
        set_timer_off_time_et = view.findViewById(R.id.sys_set_timer_off_time_et);
        set_timer_off = view.findViewById(R.id.sys_set_timer_off);
        set_timer_off_year_et = view.findViewById(R.id.sys_set_timer_off_year_et);
        set_timer_off_month_et = view.findViewById(R.id.sys_set_timer_off_month_et);
        set_timer_off_day_et = view.findViewById(R.id.sys_set_timer_off_day_et);
        set_timer_off_hour_et = view.findViewById(R.id.sys_set_timer_off_hour_et);
        set_timer_off_min_et = view.findViewById(R.id.sys_set_timer_off_min_et);
        set_timer_on = view.findViewById(R.id.sys_set_timer_on);
        set_timer_on_year_et = view.findViewById(R.id.sys_set_timer_on_year_et);
        set_timer_on_month_et = view.findViewById(R.id.sys_set_timer_on_month_et);
        set_timer_on_day_et = view.findViewById(R.id.sys_set_timer_on_day_et);
        set_timer_on_hour_et = view.findViewById(R.id.sys_set_timer_on_hour_et);
        set_timer_on_min_et = view.findViewById(R.id.sys_set_timer_on_min_et);

        get_white_list = view.findViewById(R.id.sys_get_white_list);
        get_white_list.setOnClickListener(this);
        get_black_list = view.findViewById(R.id.sys_get_black_list);
        get_black_list.setOnClickListener(this);
        install_white = view.findViewById(R.id.install_white);
        uninstall_white = view.findViewById(R.id.uninstall_white);
        install_black = view.findViewById(R.id.install_black);
        uninstall_black = view.findViewById(R.id.uninstall_black);
        get_white_list_tv = view.findViewById(R.id.sys_get_white_list_tv);
        get_black_list_tv = view.findViewById(R.id.sys_get_black_list_tv);

        /*set_gpio_spinner = view.findViewById(R.id.sys_set_gpio_spinner);
        set_gpio_spinner.setOnItemSelectedListener(this);
        gpio_List = mRes.getStringArray(R.array.gpio_array);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, gpio_List);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_gpio_spinner.setAdapter(mAdapter);
        set_gpio_direction_group = view.findViewById(R.id.sys_set_gpio_direction_group);
        set_gpio_direction_group.setOnCheckedChangeListener(this);
        gpio_in = view.findViewById(R.id.gpio_in);
        gpio_out = view.findViewById(R.id.gpio_out);
        set_gpio_value = view.findViewById(R.id.sys_set_gpio_value);
        set_gpio_tv = view.findViewById(R.id.sys_set_gpio_tv);*/

        set_control_spinner = view.findViewById(R.id.sys_set_control_spinner);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.control));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_control_spinner.setAdapter(mAdapter);
        set_control_spinner.setSelection(2);
        set_control = view.findViewById(R.id.sys_set_control_switch);

        get_systemlog_spinner = view.findViewById(R.id.sys_get_systemlog_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.log));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        get_systemlog_spinner.setAdapter(mAdapter);
        get_systemlog_spinner.setSelection(0);
        get_systemlog = view.findViewById(R.id.sys_get_systemlog_switch);

        set_volume = (MySeekBar) view.findViewById(R.id.sys_set_volume_seekbar);
        set_volume_tv = view.findViewById(R.id.sys_set_volume_tv);
        initSeekBarVolume();
        set_out_volume = (MySeekBar) view.findViewById(R.id.sys_set_out_volume_seekbar);
        set_out_volume_tv = view.findViewById(R.id.sys_set_out_volume_tv);
        initSeekBarVolumeGain();

        set_national_language = view.findViewById(R.id.sys_set_national_language);
        set_national_language.setOnClickListener(this);
        set_national_language_spinner = view.findViewById(R.id.sys_set_national_language_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.national_language));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_national_language_spinner.setAdapter(mAdapter);
        set_national_language_tv = view.findViewById(R.id.sys_set_national_language_tv);

        set_def_inputmethod = view.findViewById(R.id.sys_set_def_inputmethod);
        set_def_inputmethod.setOnClickListener(this);
        set_def_inputmethod_spinner = view.findViewById(R.id.sys_set_def_inputmethod_spinner);
        set_def_inputmethod_tv = view.findViewById(R.id.sys_set_def_inputmethod_tv);

        set_ntp_server = view.findViewById(R.id.sys_set_ntp_server);
        set_ntp_server.setOnClickListener(this);
        set_ntp_server_spinner = view.findViewById(R.id.sys_set_ntp_server_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.ntp_server));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_ntp_server_spinner.setAdapter(mAdapter);
        set_ntp_server_tv = view.findViewById(R.id.sys_set_ntp_server_tv);

        set_timezone = view.findViewById(R.id.sys_set_timezone);
        set_timezone.setOnClickListener(this);
        set_timezone_spinner = view.findViewById(R.id.sys_set_timezone_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.time_zone));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_timezone_spinner.setAdapter(mAdapter);
        set_timezone_tv = view.findViewById(R.id.sys_set_timezone_tv);

        set_timeformat = view.findViewById(R.id.sys_set_timeformat);
        set_timeformat.setOnClickListener(this);
        set_timeformat_spinner = view.findViewById(R.id.sys_set_timeformat_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.time_format));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_timeformat_spinner.setAdapter(mAdapter);
        set_timeformat_tv = view.findViewById(R.id.sys_set_timeformat_tv);

        set_launcher = view.findViewById(R.id.sys_set_launcher);
        set_launcher.setOnClickListener(this);
        set_launcher_spinner = view.findViewById(R.id.sys_set_launcher_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.launcher));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_launcher_spinner.setAdapter(mAdapter);
        set_launcher_tv = view.findViewById(R.id.sys_set_launcher_tv);

    }

    private void readData() {
        unInitListener();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        set_time_year_et.setText(calendar.get(Calendar.YEAR) + "");
        set_time_month_et.setText(calendar.get(Calendar.MONTH) + 1 + "");
        set_time_day_et.setText(calendar.get(Calendar.DAY_OF_MONTH) + "");
        set_time_hour_et.setText(calendar.get(Calendar.HOUR_OF_DAY) + "");
        set_time_min_et.setText(calendar.get(Calendar.MINUTE) + "");

        set_ntp_server_tv.setText(ntpServer);
        set_timezone_tv.setText(timeZone);
        set_timeformat_tv.setText(timeFormat);
        set_launcher_tv.setText(launcher);
        set_system_boot_app_tv.setText(bootApp);
        set_national_language_tv.setText(national);
        set_system_fontsize_tv.setText(fontSize + "");
        set_api_debug_tv.setText(debugLevel + "");
        set_daemons_tv.setText(daemonsActivity + "");

        set_bluetooth.setChecked(btEnable == 1);
        set_location.setChecked(location != 0);
        if (location == 0) {
            location = 1;
        }
        set_airplane.setChecked(airPlaneEnable == 1);
        set_hwstack.setChecked(hwStackEnable == 1);
        set_softkeyboard.setChecked(keyboardEnable == 1);
        set_keyreport.setChecked(keyReportEnable == 1);
        set_touchreport.setChecked(touchReportEnable == 1);
        set_network_time_sync.setChecked(networkTimeSyncEnable == 1);
        set_watchdog.setChecked(watchDogEnable == 1);
        set_development.setChecked(developmentEnable == 1);
        get_systemlog.setChecked(systemLogEnable == 1);
        if (get_systemlog.isChecked()) {
            smdtManagerNew.sys_getProcessLogcat(new SmdtManagerNew.LogCallback() {
                @Override
                public void onSingleLine(String log) throws RemoteException {
                    if (get_log_single && !TextUtils.isEmpty(log) && get_processlog_tv != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("log", log);
                        Message message = mHandler.obtainMessage();
                        message.what = REFRESH_LOGCAT;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        set_adb.setChecked(adbEnable == 1);
        set_adb_net.setChecked(adbNetEnable == 1);
        set_otg.setChecked(OTGEnable == OTG_DEVICE);
        set_pointer_switch.setChecked(pointerEnbale == 1);
        set_float_ball_switch.setChecked(floatBallEnbale == 1);
        set_volume_mute_switch.setChecked(volueMuteEnbale == 1);
        set_auto_install_switch.setChecked(autoinstallEnbale == 1);
        if (controlEnbale < 0) {
            set_control.setChecked(true);
        } else {
            set_control.setChecked(controlEnbale == 1);
        }
        set_update_ex_switch.setChecked(updateExEnable == 1);
        /*if (gpioDirection == 0) {
            gpio_in.setChecked(true);
            set_gpio_value.setChecked(false);
        } else if (gpioValue == 1) {
            gpio_out.setChecked(true);
            set_gpio_value.setChecked(gpioValue == 1);
        }*/

        set_timer_off_year_et.setText(calendar.get(Calendar.YEAR) + "");
        set_timer_off_month_et.setText(calendar.get(Calendar.MONTH) + 1 + "");
        set_timer_off_day_et.setText(calendar.get(Calendar.DAY_OF_MONTH) + "");
        //            set_timer_off_hour_et.setText(calendar.get(Calendar.HOUR_OF_DAY) + "");
        //            set_timer_off_min_et.setText(calendar.get(Calendar.MINUTE) + "");
        set_timer_on_year_et.setText(calendar.get(Calendar.YEAR) + "");
        set_timer_on_month_et.setText(calendar.get(Calendar.MONTH) + 1 + "");
        set_timer_on_day_et.setText(calendar.get(Calendar.DAY_OF_MONTH) + "");
        //            set_timer_on_hour_et.setText(calendar.get(Calendar.HOUR_OF_DAY) + "");
        //            set_timer_on_min_et.setText(calendar.get(Calendar.MINUTE) + "");
        set_timer.setChecked(timerEnable == 1);
        if (!TextUtils.isEmpty(bootTime)) {
            set_timer_on_time_et.setText(bootTime);
        }
        if (!TextUtils.isEmpty(shutdownTime)) {
            set_timer_off_time_et.setText(shutdownTime);
        }
        set_timer_on.setChecked(timerOnEnable == 1);
        set_timer_off.setChecked(timerOffEnable == 1);
        if (!TextUtils.isEmpty(onTime)) {
            long realOnTime = Long.parseLong(onTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
            String onTimeReal = sdf.format(realOnTime);
            String[] onTimes = onTimeReal.split(":");
            if (onTimes.length == 5) {
                set_timer_on_year_et.setText(onTimes[0]);
                set_timer_on_month_et.setText(onTimes[1]);
                set_timer_on_day_et.setText(onTimes[2]);
                set_timer_on_hour_et.setText(onTimes[3]);
                set_timer_on_min_et.setText(onTimes[4]);
            }
        }
        if (!TextUtils.isEmpty(offTime)) {
            long realOffTime = Long.parseLong(offTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
            String offTimeReal = sdf.format(realOffTime);
            String[] offTimes = offTimeReal.split(":");
            if (offTimes.length == 5) {
                set_timer_off_year_et.setText(offTimes[0]);
                set_timer_off_month_et.setText(offTimes[1]);
                set_timer_off_day_et.setText(offTimes[2]);
                set_timer_off_hour_et.setText(offTimes[3]);
                set_timer_off_min_et.setText(offTimes[4]);
            }
        }

        set_volume.setProgress(volume_value - volume_min);
        set_volume_tv.setText(volume_value + "");
        set_out_volume.setProgress(volume_gain_value - volume_gain_min);
        set_out_volume_tv.setText(volume_gain_value + "");
        set_audio_output_tv.setText(volume_output + "");
        set_audio_input_tv.setText(volume_input + "");

        set_def_inputmethod_tv.setText(inputMethod);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, inputMethodList);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_def_inputmethod_spinner.setAdapter(mAdapter);

        //保存数据到本地用于验证该接口恢复时能否恢复保存的数据
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SERVICENAME, Context.MODE_PRIVATE);
        get_backup_et.setText(sharedPreferences.getString("test_backup", null));

        initListener();
        if (mSmdtReceiver == null) {
            mSmdtReceiver = new SmdtReceiver();
            mContext.registerReceiver(mSmdtReceiver, mIntentFilter);
        }

    }

    private void initListener() {
        set_bluetooth.setOnCheckedChangeListener(this);
        set_location.setOnCheckedChangeListener(this);
        set_airplane.setOnCheckedChangeListener(this);
        set_hwstack.setOnCheckedChangeListener(this);
        set_softkeyboard.setOnCheckedChangeListener(this);
        set_keyreport.setOnCheckedChangeListener(this);
        set_touchreport.setOnCheckedChangeListener(this);
        set_network_time_sync.setOnCheckedChangeListener(this);
        set_watchdog.setOnCheckedChangeListener(this);
        set_development.setOnCheckedChangeListener(this);
        get_systemlog.setOnCheckedChangeListener(this);
        set_timer.setOnCheckedChangeListener(this);
        set_timer_off.setOnCheckedChangeListener(this);
        set_timer_on.setOnCheckedChangeListener(this);
        set_adb.setOnCheckedChangeListener(this);
        set_adb_net.setOnCheckedChangeListener(this);
        set_otg.setOnCheckedChangeListener(this);
        set_pointer_switch.setOnCheckedChangeListener(this);
        set_float_ball_switch.setOnCheckedChangeListener(this);
        set_volume_mute_switch.setOnCheckedChangeListener(this);
        set_auto_install_switch.setOnCheckedChangeListener(this);
        //        set_gpio_value.setOnCheckedChangeListener(this);
        set_control.setOnCheckedChangeListener(this);
        set_update_ex_switch.setOnCheckedChangeListener(this);
        set_control_spinner.setOnItemSelectedListener(this);
        get_systemlog_spinner.setOnItemSelectedListener(this);
        set_national_language_spinner.setOnItemSelectedListener(this);
        set_def_inputmethod_spinner.setOnItemSelectedListener(this);
    }

    private void unInitListener() {
        set_bluetooth.setOnCheckedChangeListener(null);
        set_location.setOnCheckedChangeListener(null);
        set_airplane.setOnCheckedChangeListener(null);
        set_hwstack.setOnCheckedChangeListener(null);
        set_softkeyboard.setOnCheckedChangeListener(null);
        set_keyreport.setOnCheckedChangeListener(null);
        set_touchreport.setOnCheckedChangeListener(null);
        set_network_time_sync.setOnCheckedChangeListener(null);
        set_watchdog.setOnCheckedChangeListener(null);
        set_development.setOnCheckedChangeListener(null);
        get_systemlog.setOnCheckedChangeListener(null);
        set_timer.setOnCheckedChangeListener(null);
        set_timer_off.setOnCheckedChangeListener(null);
        set_timer_on.setOnCheckedChangeListener(null);
        set_adb.setOnCheckedChangeListener(null);
        set_adb_net.setOnCheckedChangeListener(null);
        set_otg.setOnCheckedChangeListener(null);
        set_pointer_switch.setOnCheckedChangeListener(null);
        set_float_ball_switch.setOnCheckedChangeListener(null);
        set_volume_mute_switch.setOnCheckedChangeListener(null);
        set_auto_install_switch.setOnCheckedChangeListener(null);
        //        set_gpio_value.setOnCheckedChangeListener(null);
        set_control.setOnCheckedChangeListener(null);
        set_update_ex_switch.setOnCheckedChangeListener(null);
        set_control_spinner.setOnItemSelectedListener(null);
        get_systemlog_spinner.setOnItemSelectedListener(null);
        set_national_language_spinner.setOnItemSelectedListener(null);
        set_def_inputmethod_spinner.setOnItemSelectedListener(null);
    }

    @Override
    public void onClick(View view) {
        hideInputKeyboard(mContext);
        switch (view.getId()) {
            case R.id.sys_set_ntp_server:
                setNtpServer();
                break;
            case R.id.sys_set_def_inputmethod:
                setDefInputMethod();
                break;
            case R.id.sys_set_timezone:
                setTimeZone();
                break;
            case R.id.sys_set_timeformat:
                setTimeFormat();
                break;
            case R.id.sys_set_time:
                setTime();
                break;
            case R.id.sys_set_launcher:
                setDefaultLauncher();
                break;
            case R.id.sys_set_system_boot_app:
                setSystemBootApp();
                break;
            case R.id.sys_set_system_fontsize:
                showFontSizeDialog();
                break;
            case R.id.sys_set_watchdog_feed:
                set_watchdog_feed_tv.setText(smdtManagerNew.sys_setWatchDogFeed() + "");
                break;
            case R.id.sys_set_power_off:
                set_power_off_tv.setText(smdtManagerNew.sys_setPowerOff() + "");
                break;
            case R.id.sys_set_reboot:
                set_reboot_tv.setText(smdtManagerNew.sys_setReboot() + "");
                break;
            case R.id.sys_set_reboot_mcu:
                set_reboot_mcu_tv.setText(smdtManagerNew.sys_setRebootByMcu() + "");
                break;
            case R.id.sys_recovery:
                recovery_tv.setText(smdtManagerNew.sys_rebootRecovery() + "");
                break;
            case R.id.sys_get_processlog:
                getLogSingleLine();
                break;
            case R.id.sys_set_api_debug:
                showApiDebugDialog();
                break;
            case R.id.sys_get_api_error:
                getApiError();
                break;
            case R.id.sys_add_white_list:
                addBlackWhiteList(WHITE_LIST);
                break;
            case R.id.sys_get_white_list:
                showAppListDialog(WHITE_LIST);
                break;
            case R.id.sys_del_white_list:
                delBlackWhiteList(WHITE_LIST);
                break;
            case R.id.sys_add_black_list:
                addBlackWhiteList(BLACK_LIST);
                break;
            case R.id.sys_get_black_list:
                showAppListDialog(BLACK_LIST);
                break;
            case R.id.sys_del_black_list:
                delBlackWhiteList(BLACK_LIST);
                break;
            case R.id.sys_set_audio_output:
                showAudioOutputDialog();
                break;
            case R.id.sys_set_audio_input:
                showAudioInputDialog();
                break;
            case R.id.sys_get_auto_install:
                showAutoInstallDialog();
                break;
            case R.id.sys_do_update:
                doUpate();
                break;
            case R.id.sys_set_national_language:
                setNationalAndLanguage();
                break;
            case R.id.sys_set_daemons:
                setDaemons();
                break;
            case R.id.sys_do_silent_install:
                doSilentInstall();
                break;
            case R.id.sys_do_silent_uninstall:
                doSilentUninstall();
                break;
            case R.id.sys_copy_file:
                copyFile();
                break;
            case R.id.sys_set_backup:
                backUpApplication();
                break;
            case R.id.sys_get_backup:
                recoveryApplication();
                break;
            case R.id.sys_get_anr:
                getAnrLog();
                break;
            case R.id.sys_add_auto_install:
                addAutoInstall();
                break;
            case R.id.sys_del_auto_install:
                delAutoInstall();
                break;
            case R.id.sys_set_gpio:
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName c = new ComponentName("android.app.smdt.smdtsettings", "android.app.smdt.smdtsettings.MainActivity");
                intent.setComponent(c);
                intent.putExtra("FUNCTION", "gpioTest");
                mContext.startActivity(intent);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        hideInputKeyboard(mContext);
        if (!compoundButton.isPressed()) {
            DEBUG("System isPressed from code or what?:" + compoundButton.isPressed());
            return;
        }
        switch (compoundButton.getId()) {
            //失败反选
            case R.id.sys_set_bluetooth_switch:
                if (smdtManagerNew.sys_setBluetooth(isChecked) != 0) {
                    set_bluetooth.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_location_switch:
                if (smdtManagerNew.sys_setLocation(isChecked ? location : 0) != 0) {
                    set_bluetooth.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_airplane_switch:
                if (smdtManagerNew.sys_setAirPlane(isChecked) != 0) {
                    set_airplane.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_hwstack_switch:
                if (smdtManagerNew.sys_setHwStack(isChecked) != 0) {
                    set_hwstack.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_softkeyboard_switch:
                if (smdtManagerNew.sys_setSoftKeyboard(isChecked) != 0) {
                    set_softkeyboard.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_keyreport_switch:
                if (smdtManagerNew.sys_setKeyReport(isChecked) != 0) {
                    set_keyreport.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_touchreport_switch:
                if (smdtManagerNew.sys_setTouchReport(isChecked) != 0) {
                    set_touchreport.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_network_time_sync_switch:
                if (smdtManagerNew.sys_setNetworkTimeSync(isChecked) != 0) {
                    set_network_time_sync.setChecked(!isChecked);
                }
                break;
            /*case R.id.sys_set_gpio_value:
                if (smdtManagerNew.sys_setGpioDirection(gpio, GPIO_OUT, isChecked ? GPIO_HIGH : GPIO_LOW) != 0) {
                    set_gpio_value.setChecked(!isChecked);
                    set_gpio_tv.setText("-1");
                } else {
                    set_gpio_tv.setText(isChecked ? "1" : "0");
                }
                break;*/
            case R.id.sys_set_watchdog_switch:
                if (smdtManagerNew.sys_setWatchDog(isChecked, 60) != 0) {
                    set_watchdog.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_development_switch:
                if (smdtManagerNew.sys_setDeveloperOptions(isChecked) != 0) {
                    set_development.setChecked(!isChecked);
                }
                break;
            case R.id.sys_get_systemlog_switch:
                setSystemLogEnable(isChecked);
                break;
            case R.id.sys_set_adb_switch:
                if (smdtManagerNew.sys_setAdbDebug(ADB_USB, isChecked) != 0) {
                    set_adb.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_adb_net_switch:
                if (smdtManagerNew.sys_setAdbDebug(ADB_NETWORK, isChecked) != 0) {
                    set_adb_net.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_otg_switch:
                if (smdtManagerNew.sys_setOTGMode(isChecked ? OTG_DEVICE : OTG_HOST) != 0) {
                    set_otg.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_pointer_switch:
                if (smdtManagerNew.sys_setPointerLoction(isChecked) != 0) {
                    set_pointer_switch.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_float_ball_switch:
                if (smdtManagerNew.sys_setFloatBall(isChecked) != 0) {
                    set_float_ball_switch.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_volume_mute_switch:
                if (smdtManagerNew.sys_setVolumeMute(isChecked) != 0) {
                    set_volume_mute_switch.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_auto_install_switch:
                if (smdtManagerNew.sys_setAutoInstallEnable(isChecked) != 0) {
                    set_auto_install_switch.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_control_switch:
                if (smdtManagerNew.sys_setControl(controlValues[set_control_spinner.getSelectedItemPosition()], isChecked) != 0) {
                    set_control.setChecked(!isChecked);
                }
                break;
            case R.id.sys_set_timer:
                int result = setTimer(isChecked);
                if (result != 0) {
                    set_timer.setChecked(!isChecked);
                    Toast.makeText(mContext, mRes.getString(R.string.error_code) + ":" + result, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sys_set_timer_off:
                result = setTimerOnOff(isChecked, false);
                if (result != 0) {
                    set_timer_off.setChecked(!isChecked);
                    Toast.makeText(mContext, mRes.getString(R.string.error_code) + ":" + result, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sys_set_timer_on:
                result = setTimerOnOff(isChecked, true);
                if (result != 0) {
                    set_timer_on.setChecked(!isChecked);
                    Toast.makeText(mContext, mRes.getString(R.string.error_code) + ":" + result, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.sys_set_update_ex_switch:
                if (smdtManagerNew.sys_setUpdateExState(isChecked) != 0) {
                    set_update_ex_switch.setChecked(!isChecked);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        hideInputKeyboard(mContext);
        switch (adapterView.getId()) {
            /*case R.id.sys_set_gpio_spinner:
                gpio = i + 1;
                setGpioStatus();
                break;*/
            case R.id.sys_set_control_spinner:
                controlEnbale = smdtManagerNew.sys_getControl(controlValues[i]);
                set_control.setOnCheckedChangeListener(null);
                if (controlEnbale < 0) {
                    set_control.setChecked(true);
                } else {
                    set_control.setChecked(controlEnbale == 1);
                }
                set_control.setOnCheckedChangeListener(this);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        hideInputKeyboard(mContext);
        switch (radioGroup.getId()) {
            /*case R.id.sys_set_gpio_direction_group:
                switch (checkedId) {
                    case R.id.gpio_in:
                        set_gpio_value.setEnabled(false);
                        if (smdtManagerNew.sys_setGpioDirection(gpio, GPIO_IN, GPIO_LOW) != 0) {
                            set_gpio_tv.setText("-1");
                        } else {
                            set_gpio_tv.setText("0");
                        }
                        break;
                    case R.id.gpio_out:
                        int gpio_value = smdtManagerNew.sys_getGpioValue(gpio);
                        set_gpio_value.setEnabled(true);
                        set_gpio_value.setChecked(gpio_value == GPIO_HIGH);
                        if (smdtManagerNew.sys_setGpioDirection(gpio, GPIO_OUT, gpio_value) != 0) {
                            set_gpio_tv.setText("-1");
                        } else {
                            set_gpio_tv.setText(gpio_value + "");
                        }
                        break;
                }
                break;*/
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            ntpServer = smdtManagerNew.sys_getNtpServer();
            inputMethod = smdtManagerNew.sys_getDefInputMethod();
            timeZone = smdtManagerNew.sys_getTimeZone();
            timeFormat = smdtManagerNew.sys_getTimeFormat();
            launcher = smdtManagerNew.sys_getDefaultLauncher();
            bootApp = smdtManagerNew.sys_getSystemBootApp();
            national = smdtManagerNew.sys_getNationallanguage();
            fontSize = smdtManagerNew.sys_getSystemFontSize();
            debugLevel = smdtManagerNew.sys_getApiDebugLevel();
            daemonsActivity = smdtManagerNew.sys_getDaemonsActivity();
            btEnable = smdtManagerNew.sys_getBluetooth();
            location = smdtManagerNew.sys_getLocation();
            airPlaneEnable = smdtManagerNew.sys_getAirPlane();
            hwStackEnable = smdtManagerNew.sys_getHwStack();
            keyboardEnable = smdtManagerNew.sys_getSoftKeyboard();
            keyReportEnable = smdtManagerNew.sys_getKeyReport();
            touchReportEnable = smdtManagerNew.sys_getTouchReport();
            networkTimeSyncEnable = smdtManagerNew.sys_getNetworkTimeSync();
            watchDogEnable = smdtManagerNew.sys_getWatchDog();
            developmentEnable = smdtManagerNew.sys_getDeveloperOptions();
            systemLogEnable = smdtManagerNew.sys_getSystemLog();
            adbEnable = smdtManagerNew.sys_getAdbDebug(ADB_USB);
            adbNetEnable = smdtManagerNew.sys_getAdbDebug(ADB_NETWORK);
            OTGEnable = smdtManagerNew.sys_getOTGMode();
            pointerEnbale = smdtManagerNew.sys_getPointerLoction();
            floatBallEnbale = smdtManagerNew.sys_getFloatBall();
            volueMuteEnbale = smdtManagerNew.sys_getVolumeMute();
            autoinstallEnbale = smdtManagerNew.sys_getAutoInstallEnable();
            controlEnbale = smdtManagerNew.sys_getControl(controlValues[set_control_spinner.getSelectedItemPosition()]);
            /*gpio = set_gpio_spinner.getSelectedItemPosition() + 1;
            gpioDirection = smdtManagerNew.sys_getGpioDirection(gpio);
            gpioValue = smdtManagerNew.sys_getGpioValue(gpio);*/
            volume_value = smdtManagerNew.sys_getVolume();
            volume_gain_value = smdtManagerNew.sys_getOutVolume(1);
            volume_output =smdtManagerNew.sys_getAudioOutput();
            volume_input =smdtManagerNew.sys_getAudioInput();
            timerEnable = smdtManagerNew.sys_getAutoPowerOnOffEnable();
            shutdownTime = smdtManagerNew.sys_getAutoPowerOnOff(0);
            bootTime = smdtManagerNew.sys_getAutoPowerOnOff(1);
            //            timerOnEnable = smdtManagerNew.sys_getAutoPowerOnOffTimeEnable(true);
            //            timerOffEnable = smdtManagerNew.sys_getAutoPowerOnOffTimeEnable(false);
            offTime = smdtManagerNew.sys_getAutoPowerOnOff(2);
            onTime = smdtManagerNew.sys_getAutoPowerOnOff(3);
            inputMethodList = smdtManagerNew.sys_getDefInputMethodList();
            updateExEnable = smdtManagerNew.sys_getUpdateExState();

            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }

    private void setNtpServer() {
        String url = set_ntp_server_spinner.getSelectedItem().toString();
        int result = smdtManagerNew.sys_setNtpServer(url);
        if (result == 0) {
            set_ntp_server_tv.setText(smdtManagerNew.sys_getNtpServer() + "");
        } else {
            set_ntp_server_tv.setText(result + "");
        }
    }

    private void setDefInputMethod() {
        String packageName = set_def_inputmethod_spinner.getSelectedItem().toString();
        int result = smdtManagerNew.sys_setDefInputMethod(packageName);
        if (result == 0) {
            set_def_inputmethod_tv.setText(smdtManagerNew.sys_getDefInputMethod() + "");
        } else {
            set_def_inputmethod_tv.setText(result + "");
        }
    }

    private void setTimeZone() {
        String timeZone = set_timezone_spinner.getSelectedItem().toString();
        int result = smdtManagerNew.sys_setTimeZone(timeZone);
        set_timezone_tv.setText(result + "");
    }

    private void setTimeFormat() {
        String timeFormat = set_timeformat_spinner.getSelectedItem().toString();
        int result = smdtManagerNew.sys_setTimeFormat(timeFormat);
        if (result == 0) {
            set_timeformat_tv.setText(smdtManagerNew.sys_getTimeFormat());
        } else {
            set_timeformat_tv.setText(result + "");
        }
    }

    private void setTime() {
        String year = set_time_year_et.getText().toString();
        String month = set_time_month_et.getText().toString();
        String day = set_time_day_et.getText().toString();
        String hour = set_time_hour_et.getText().toString();
        String minute = set_time_min_et.getText().toString();
        if (TextUtils.isEmpty(year)
                || TextUtils.isEmpty(month)
                || TextUtils.isEmpty(day)
                || TextUtils.isEmpty(hour)
                || TextUtils.isEmpty(minute)) {
            return;
        }
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, Integer.parseInt(year));
        c.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        c.set(Calendar.MINUTE, Integer.parseInt(minute));
        set_time_tv.setText(smdtManagerNew.sys_setTime(c.getTimeInMillis()) + "");
    }

    private void setDefaultLauncher() {
        String packageName = set_launcher_spinner.getSelectedItem().toString();
        int result = smdtManagerNew.sys_setDefaultLauncher(packageName);
        if (result == 0) {
            set_launcher_tv.setText(smdtManagerNew.sys_getDefaultLauncher() + "");
        } else {
            set_launcher_tv.setText(result + "");
        }
    }

    private void setSystemBootApp() {
        String packageName = set_system_boot_app_et.getText().toString();
        int result = smdtManagerNew.sys_setSystemBootApp(packageName);
        if (result == 0) {
            set_system_boot_app_tv.setText(smdtManagerNew.sys_getSystemBootApp() + "");
        } else {
            set_system_boot_app_tv.setText(result + "");
        }
    }

    private void getApiError() {
        String str = get_api_error_et.getText().toString();
        if (TextUtils.isEmpty(str)) {
            return;
        }
        int errorCode = Integer.parseInt(str);
        get_api_error_tv.setText(smdtManagerNew.sys_getErrorDescription(errorCode) + "");
    }

    private void showFontSizeDialog() {
        final String[] entries = mRes.getStringArray(R.array.entries_font_size);
        final String[] strEntryValues = mRes.getStringArray(R.array.entryvalues_font_size);
        float currentScale = smdtManagerNew.sys_getSystemFontSize();
        int index = -1;
        if (currentScale > 0) {
            index = fontSizeValueToIndex(currentScale, strEntryValues);
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setSingleChoiceItems(entries, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                float fontsize = Float.parseFloat(strEntryValues[i]);
                int result = smdtManagerNew.sys_setSystemFontSize(fontsize);
                if (result == 0) {
                    set_system_fontsize_tv.setText(smdtManagerNew.sys_getSystemFontSize() + "");
                } else {
                    set_system_fontsize_tv.setText(result + "");
                }
                fontsizeDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                fontsizeDialog.dismiss();
            }
        });

        fontsizeDialog = alertBuilder.create();
        fontsizeDialog.show();
    }

    private void showApiDebugDialog() {
        final String[] entries = mRes.getStringArray(R.array.api_level);
        int current = smdtManagerNew.sys_getApiDebugLevel();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setSingleChoiceItems(entries, current, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int result = smdtManagerNew.sys_setApiDebugLevel(i);
                if (result == 0) {
                    set_api_debug_tv.setText(smdtManagerNew.sys_getApiDebugLevel() + "");
                } else {
                    set_api_debug_tv.setText(result + "");
                }
                apidebugDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                apidebugDialog.dismiss();
            }
        });

        apidebugDialog = alertBuilder.create();
        apidebugDialog.show();
    }

    private static int fontSizeValueToIndex(float val, String[] indices) {
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal - lastVal) * .5f)) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }

    /*private void setGpioStatus() {
        int gpio_direction = smdtManagerNew.sys_getGpioDirection(gpio);
        if (gpio_direction == GPIO_IN) {
            gpio_in.setChecked(true);
        } else if (gpio_direction == GPIO_OUT) {
            gpio_out.setChecked(true);
            int gpio_value = smdtManagerNew.sys_getGpioValue(gpio);
            set_gpio_value.setChecked(gpio_value == GPIO_HIGH);
        }
    }*/

    private void addBlackWhiteList(int type) {
        //type 0黑名单 1白名单
        String packageName = (type == BLACK_LIST ? add_black_list_et.getText().toString() : add_white_list_et.getText().toString());
        int function = (type == BLACK_LIST ? (install_black.isChecked() ? INSTALL_LIST : UNINSTALL_LIST) : (install_white.isChecked() ? INSTALL_LIST : UNINSTALL_LIST));
        int result = smdtManagerNew.sys_addBlackWhiteList(packageName, type, function);
        if (type == BLACK_LIST) {
            add_black_list_tv.setText(result + "");
        } else {
            add_white_list_tv.setText(result + "");
        }
    }

    private void showAppListDialog(int type) {
        //type 0黑名单 1白名单
        int function = (type == BLACK_LIST ? (install_black.isChecked() ? INSTALL_LIST : UNINSTALL_LIST) : (install_white.isChecked() ? INSTALL_LIST : UNINSTALL_LIST));
        List<String> list = smdtManagerNew.sys_getBlackWhiteList(type, function);
        if (list == null || list.size() == 0) {
            if (type == BLACK_LIST) {
                get_black_list_tv.setText("-1");
            } else {
                get_white_list_tv.setText("-1");
            }
            return;
        }

        if (type == BLACK_LIST) {
            get_black_list_tv.setText("");
        } else {
            get_white_list_tv.setText("");
        }

        String[] applist = new String[list.size()];
        list.toArray(applist);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle((function == INSTALL_LIST ? mRes.getString(R.string.install_list) : mRes.getString(R.string.uninstall_list)) + "" +
                (type == WHITE_LIST ? mRes.getString(R.string.white_list) : mRes.getString(R.string.black_list)));
        alertBuilder.setItems(applist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获取剪贴板管理器
                ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                //创建能够存入剪贴板的ClipData对象
                ClipData mClipData = ClipData.newPlainText("copydata", applist[which]);
                //将ClipData数据复制到剪贴板：
                mClipboardManager.setPrimaryClip(mClipData);
                Toast.makeText(mContext, mRes.getString(R.string.long_click_toast), Toast.LENGTH_SHORT).show();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                appListDialog.dismiss();
            }
        });

        appListDialog = alertBuilder.create();
        appListDialog.show();
    }

    private void delBlackWhiteList(int type) {
        String packageName = (type == BLACK_LIST ? del_black_list_et.getText().toString() : del_white_list_et.getText().toString());
        int function = (type == BLACK_LIST ? (install_black.isChecked() ? INSTALL_LIST : UNINSTALL_LIST) : (install_white.isChecked() ? INSTALL_LIST : UNINSTALL_LIST));
        int result = smdtManagerNew.sys_delBlackWhiteList(packageName, type, function);
        if (type == BLACK_LIST) {
            del_black_list_tv.setText(result + "");
        } else {
            del_white_list_tv.setText(result + "");
        }


    }

    private void showAudioOutputDialog() {
        final String[] entries = mRes.getStringArray(R.array.audio_output_type_entries);
        int current = smdtManagerNew.sys_getAudioOutput();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mContext.getResources().getString(R.string.sys_set_audio_output));
        alertBuilder.setSingleChoiceItems(entries, current, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int result = smdtManagerNew.sys_setAudioOutput(i);
                if (result == 0) {
                    set_audio_output_tv.setText(smdtManagerNew.sys_getAudioOutput() + "");
                } else {
                    set_audio_output_tv.setText(result + "");
                }
                audioOutputDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                audioOutputDialog.dismiss();
            }
        });

        audioOutputDialog = alertBuilder.create();
        audioOutputDialog.show();
    }

    private void showAudioInputDialog() {
        final String[] entries = mRes.getStringArray(R.array.audio_input_type_entries);
        int current = smdtManagerNew.sys_getAudioInput();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mContext.getResources().getString(R.string.sys_set_audio_output));
        alertBuilder.setSingleChoiceItems(entries, current, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int result = smdtManagerNew.sys_setAudioInput(i);
                if (result == 0) {
                    set_audio_input_tv.setText(smdtManagerNew.sys_getAudioInput() + "");
                } else {
                    set_audio_input_tv.setText(result + "");
                }
                audioInputDialog.dismiss();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                audioInputDialog.dismiss();
            }
        });

        audioInputDialog = alertBuilder.create();
        audioInputDialog.show();
    }

    private void showAutoInstallDialog() {
        List<String> list = smdtManagerNew.sys_getAutoInstallAppList();
        if (list == null || list.size() == 0) {
            get_auto_install_tv.setText("-1");
            return;
        }
        get_auto_install_tv.setText("");

        String[] applist = new String[list.size()];
        list.toArray(applist);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mRes.getString(R.string.long_click_msg));
        alertBuilder.setItems(applist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获取剪贴板管理器
                ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                //创建能够存入剪贴板的ClipData对象
                ClipData mClipData = ClipData.newPlainText("copydata", applist[which]);
                //将ClipData数据复制到剪贴板：
                mClipboardManager.setPrimaryClip(mClipData);
                Toast.makeText(mContext, mRes.getString(R.string.long_click_toast), Toast.LENGTH_SHORT).show();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                autoinstallDialog.dismiss();
            }
        });

        autoinstallDialog = alertBuilder.create();
        autoinstallDialog.show();
    }

    private void addAutoInstall() {
        String path = add_auto_install_path_et.getText().toString();
        String packageName = add_auto_install_pkg_et.getText().toString();
        add_auto_install_tv.setText(smdtManagerNew.sys_addAutoInstallAppList(path, packageName) + "");
    }

    private void delAutoInstall() {
        String path = del_auto_install_path_et.getText().toString();
        String packageName = del_auto_install_pkg_et.getText().toString();
        del_auto_install_tv.setText(smdtManagerNew.sys_delAutoInstallAppList(path, packageName) + "");
    }

    private void doUpate() {
        String path = do_update_et.getText().toString();
        do_update_tv.setText(smdtManagerNew.sys_doUpdatePackage(0, path) + "");

    }

    private void setNationalAndLanguage() {
        String[] select = set_national_language_spinner.getSelectedItem().toString().split("_");
        String language = select[0];
        String national = select[1];
        int result = smdtManagerNew.sys_setNationallanguage(national, language);
        if (result == 0) {
            set_national_language_tv.setText(smdtManagerNew.sys_getNationallanguage() + "");
        } else {
            set_national_language_tv.setText(result + "");
        }
    }

    private void setDaemons() {
        String pkgName = set_daemons_et.getText().toString();
        String time = set_daemons_time_et.getText().toString();
        if (TextUtils.isEmpty(time)) {
            set_daemons_tv.setText(RET_API_ERR_PARA + "");
            return;
        }
        int result = smdtManagerNew.sys_setDaemonsActivity(pkgName, Long.parseLong(time), true);
        set_daemons_tv.setText(result + "");
    }

    private void doSilentInstall() {
        String path = do_silent_install_et.getText().toString();
        smdtManagerNew.sys_doSilentInstallApp(path, new SmdtManagerNew.InstallCallback() {
            @Override
            public void onInstallFinished(String packageName, int returnCode, String msg) throws RemoteException {
                Bundle bundle = new Bundle();
                bundle.putString("packageName", packageName);
                bundle.putInt("returnCode", returnCode);
                Message message = mHandler.obtainMessage();
                message.what = REFRESH_INSTALL_APP;
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
        });
    }

    private void doSilentUninstall() {
        String pkg = do_silent_uninstall_et.getText().toString();
        smdtManagerNew.sys_doSilentUninstallApp(pkg, new SmdtManagerNew.DeleteCallback() {
            @Override
            public void onDeleteFinished(String packageName, int returnCode, String msg) throws RemoteException {
                Bundle bundle = new Bundle();
                bundle.putString("packageName", packageName);
                bundle.putString("msg", msg);
                bundle.putInt("returnCode", returnCode);
                Message message = mHandler.obtainMessage();
                message.what = REFRESH_UNINSTALL_APP;
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
        });
    }

    private void copyFile() {
        String fromPath = copy_file_et1.getText().toString();
        String targetPath = copy_file_et2.getText().toString();
        smdtManagerNew.sys_copyFile(fromPath, targetPath, new SmdtManagerNew.CopyCallback() {
            @Override
            public void onCopyProgress(int progress) throws RemoteException {
                Bundle bundle = new Bundle();
                bundle.putInt("progress", progress);
                Message message = mHandler.obtainMessage();
                message.what = REFRESH_COPY_PROGRESS;
                message.setData(bundle);
                mHandler.sendMessage(message);
            }

            @Override
            public void onCopyFinished(int returnCode, String msg) throws RemoteException {
                if (returnCode != 0) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("returnCode", returnCode);
                    Message message = mHandler.obtainMessage();
                    message.what = REFRESH_COPY_FINISH;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        });
    }

    private void backUpApplication() {
        String packageName = set_backup_et.getText().toString();
        if (!TextUtils.isEmpty(packageName)) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(SERVICENAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("test_backup", set_backup_et.getText().toString());
            editor.commit();

            smdtManagerNew.sys_backupApplication(packageName, new SmdtManagerNew.BackUpCallback() {
                @Override
                public void onBackUpProgress(int progress) throws RemoteException {
                    Bundle bundle = new Bundle();
                    bundle.putInt("progress", progress);
                    Message message = mHandler.obtainMessage();
                    message.what = REFRESH_BACKUP_PROGRESS;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }

                @Override
                public void onBackUpFinished(int returnCode, String msg) throws RemoteException {
                    if (returnCode != 0) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("returnCode", returnCode);
                        Message message = mHandler.obtainMessage();
                        message.what = REFRESH_BACKUP_FINISH;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }

                @Override
                public void onBackUpPath(String path) throws RemoteException {
                    if (!TextUtils.isEmpty(path)) {
                        Bundle bundle = new Bundle();
                        bundle.putString("path", path);
                        Message message = mHandler.obtainMessage();
                        message.what = REFRESH_BACKUP_PATH;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }

            });
        }
    }

    private void recoveryApplication() {
        String packageName = get_backup_et.getText().toString();
        smdtManagerNew.sys_recoveryApplication(packageName, new SmdtManagerNew.RecoveryCallback() {
            @Override
            public void onRecoveryProgress(int progress) throws RemoteException {
                Bundle bundle = new Bundle();
                bundle.putInt("progress", progress);
                Message message = mHandler.obtainMessage();
                message.what = REFRESH_RECOVERY_PROGRESS;
                message.setData(bundle);
                mHandler.sendMessage(message);
            }

            @Override
            public void onRecoveryFinished(int returnCode, String msg) throws RemoteException {
                if (returnCode != 0) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("returnCode", returnCode);
                    Message message = mHandler.obtainMessage();
                    message.what = REFRESH_RECOVERY_FINISH;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }

            @Override
            public void onRecoveryPath(String path) throws RemoteException {
                if (!TextUtils.isEmpty(path)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("path", path);
                    Message message = mHandler.obtainMessage();
                    message.what = REFRESH_BACKUP_PATH;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }

        });
    }

    private void getAnrLog() {
        String path = get_anr_et.getText().toString();
        get_anr_tv.setText(smdtManagerNew.sys_getProcessAnrLog(path) + "");
    }

    /**
     * 初始化音量seekbar
     */
    private void initSeekBarVolume() {
        volume_min = smdtManagerNew.sys_getVolumeMaxMin("min");
        volume_max = smdtManagerNew.sys_getVolumeMaxMin("max") - volume_min;

        // 设置SeekBar监听事件
        set_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                if (drag_volume) {
                    volume_value = value + volume_min;
                    smdtManagerNew.sys_setVolume(volume_value);
                    set_volume_tv.setText(volume_value + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideInputKeyboard(mContext);
                drag_volume = true;
                set_volume_tv.setText(volume_value + "");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drag_volume = false;
                int result = smdtManagerNew.sys_setVolume(volume_value);
                if (result == 0) {
                    set_volume_tv.setText(volume_value + "");
                } else {
                    set_volume_tv.setText(result + "");
                }
            }
        });
        set_volume.setMinValue(volume_min);
        set_volume.setMax(volume_max);
    }

    /**
     * 初始化喇叭增益seekbar
     */
    private void initSeekBarVolumeGain() {
        volume_gain_min = 0;
        volume_gain_max = 99;

        // 设置SeekBar监听事件
        set_out_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                if (drag_volume_gain) {
                    volume_gain_value = value + volume_gain_min;
                    set_out_volume_tv.setText(volume_gain_value + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                hideInputKeyboard(mContext);
                drag_volume_gain = true;
                set_out_volume_tv.setText(volume_gain_value + "");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                drag_volume_gain = false;
                int result = smdtManagerNew.sys_setOutVolume(1,volume_gain_value);
                if (result == 0) {
                    set_out_volume_tv.setText(volume_gain_value + "");
                } else {
                    set_out_volume_tv.setText(result + "");
                }
            }
        });
        set_out_volume.setMinValue(volume_gain_min);
        set_out_volume.setMax(volume_gain_max);
    }

    private void setSystemLogEnable(boolean enable) {
        int[] type;
        int select = get_systemlog_spinner.getSelectedItemPosition();
        if (select == 0) {
            type = new int[]{LOG_KERNEL, LOG_ANDROID, LOG_MEDIA, LOG_RADIO, LOG_PROP, LOG_CONFIG, LOG_MISC, LOG_APP, LOG_MCU};
        } else {
            type = new int[]{select};
        }
        int result = smdtManagerNew.sys_setSystemLog(enable, type, null, 0, false);
        if (result != RET_API_OK) {
            get_systemlog.setChecked(!enable);
            if (result == RET_API_ERR_NG) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.sys_log_fail), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (!enable) {
            smdtManagerNew.sys_getProcessLogcat(null);
            get_log_single = false;
        } else {
            smdtManagerNew.sys_getProcessLogcat(new SmdtManagerNew.LogCallback() {
                @Override
                public void onSingleLine(String log) throws RemoteException {
                    if (get_log_single && !TextUtils.isEmpty(log) && get_processlog_tv != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("log", log);
                        Message message = mHandler.obtainMessage();
                        message.what = REFRESH_LOGCAT;
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private void getLogSingleLine() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_tv, null);
        get_processlog_tv = view.findViewById(R.id.message);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                get_log_single = false;
                get_processlog_tv = null;
            }
        });
        get_log_single = true;
    }

    private int setTimer(boolean enable) {
        int result = RET_API_ERR_NG;
        if (enable) {
            String onTime = set_timer_on_time_et.getText().toString();
            String offTime = set_timer_off_time_et.getText().toString();
            if (TextUtils.isEmpty(onTime) || TextUtils.isEmpty(offTime)) {
                return ERROR_CONFIG;
            }
            int on_hour, on_min, off_hour, off_min;
            if (onTime.contains(":")) {
                on_hour = Integer.parseInt(onTime.split(":")[0]);
                on_min = Integer.parseInt(onTime.split(":")[1]);
            } else {
                on_hour = Integer.parseInt(onTime);
                on_min = 0;
            }

            if (offTime.contains(":")) {
                off_hour = Integer.parseInt(offTime.split(":")[0]);
                off_min = Integer.parseInt(offTime.split(":")[1]);
            } else {
                off_hour = Integer.parseInt(offTime);
                off_min = 0;
            }

            int[] week = new int[]{1, 1, 1, 1, 1, 1, 1};
            result = smdtManagerNew.sys_setAutoPowerOnOff(true, week, on_hour, on_min, off_hour, off_min);
        } else {
            result = smdtManagerNew.sys_setAutoPowerOnOff(false, null, 0, 0, 0, 0);
        }

        return result;
    }

    private int setTimerOnOff(boolean enable, boolean on) {
        int result = RET_API_ERR_NG;
        if (enable) {
            long setTime = 0;
            if (on) {
                String onYear = set_timer_on_year_et.getText().toString();
                String onMonth = set_timer_on_month_et.getText().toString();
                String onDay = set_timer_on_day_et.getText().toString();
                String onHour = set_timer_on_hour_et.getText().toString();
                String onMinute = set_timer_on_min_et.getText().toString();
                if (TextUtils.isEmpty(onYear)
                        || TextUtils.isEmpty(onMonth)
                        || TextUtils.isEmpty(onDay)
                        || TextUtils.isEmpty(onHour)
                        || TextUtils.isEmpty(onMinute)) {
                    return ERROR_CONFIG;
                }

                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, Integer.parseInt(onYear));
                c.set(Calendar.MONTH, Integer.parseInt(onMonth) - 1);
                c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(onDay));
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(onHour));
                c.set(Calendar.MINUTE, Integer.parseInt(onMinute));
                setTime = c.getTimeInMillis();
            } else {
                String offYear = set_timer_off_year_et.getText().toString();
                String offMonth = set_timer_off_month_et.getText().toString();
                String offDay = set_timer_off_day_et.getText().toString();
                String offHour = set_timer_off_hour_et.getText().toString();
                String offMinute = set_timer_off_min_et.getText().toString();
                if (TextUtils.isEmpty(offYear)
                        || TextUtils.isEmpty(offMonth)
                        || TextUtils.isEmpty(offDay)
                        || TextUtils.isEmpty(offHour)
                        || TextUtils.isEmpty(offMinute)) {
                    return ERROR_CONFIG;
                }

                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, Integer.parseInt(offYear));
                c.set(Calendar.MONTH, Integer.parseInt(offMonth) - 1);
                c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(offDay));
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(offHour));
                c.set(Calendar.MINUTE, Integer.parseInt(offMinute));
                setTime = c.getTimeInMillis();
            }

            //            result = smdtManagerNew.sys_setAutoPowerOnOffTime(on, true, setTime);
        } else {
            //            result = smdtManagerNew.sys_setAutoPowerOnOffTime(on, false, -1);
        }

        return result;
    }

}
