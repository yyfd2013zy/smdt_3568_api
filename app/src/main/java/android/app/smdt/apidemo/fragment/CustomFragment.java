/**
 * File: CustomFragment.java
 * Author: Xu Linrui <lrxu@smdt.com.cn>
 * Created on 5 July 2021
 **/
package android.app.smdt.apidemo.fragment;

import android.app.AlertDialog;
import android.app.smdt.SmdtManagerNew;
import android.app.smdt.apidemo.R;
import android.app.smdt.apidemo.ui.MySeekBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CustomFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
        , AdapterView.OnItemSelectedListener {

    //data
    private int relayIoMode;
    private int relayIoEnable;
    private int mLedBrightness0, mLedBrightness1;
    private boolean drag_led = false;
    private int ledMax = 100, ledMin = 0;
    private String desktopApp;
    //view
    //button
    private Button read_wg, release_wg, clean_recent, endcall;
    private TextView read_wg_tv, release_wg_tv, clean_recent_tv, endcall_tv;
    //button+editText
    private Button get_pid, get_pid_info, kill_pid, dial, add_encryption, del_encryption, set_desktop_app;
    private EditText get_pid_et, get_pid_info_et, kill_pid_et, dial_et, add_encryption_et, add_encryption_oldpwd_et,
            add_encryption_pwd_et, del_encryption_et, del_encryption_pwd_et, set_desktop_app_et;
    private TextView get_pid_tv, get_pid_info_tv, kill_pid_tv, dial_tv, add_encryption_tv, del_encryption_tv, set_desktop_app_tv;
    //button+Dialog
    private Button get_encryption;
    private TextView get_encryption_tv;
    private AlertDialog encryDialog;
    //button+editText+spinner
    private Button send_wg_card, send_wg_card2, set_relay_mode;
    private EditText send_wg_card_et, send_wg_card2_hid_et, send_wg_card2_pid_et, set_relay_mode_et;
    private Spinner send_wg_card_spinner, send_wg_card2_spinner, set_relay_mode_spinner;
    private TextView send_wg_card_tv, send_wg_card2_tv, set_relay_mode_tv;
    //switch
    private Switch set_relay_switch;
    //seekBar
    private MySeekBar custom_ledbrightness_seekbar0, custom_ledbrightness_seekbar1;
    private TextView custom_ledbrightness_value0, custom_ledbrightness_value1;

    //Handler
    private static final int REFRESH_DATA = 0x00;
    private static final int REFRESH_WG_DATA = 0x01;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_DATA) {
                readData();
            } else if (msg.what == REFRESH_WG_DATA) {
                Bundle bundle = msg.getData();
                String data = bundle.getString("data");
                read_wg_tv.setText(data);
            }
        }
    };

    @Override
    public int getLayoutResource() {
        return R.layout.custom_fragment;
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
    }

    private void initView() {
        read_wg = view.findViewById(R.id.custom_read_wg);
        read_wg.setOnClickListener(this);
        read_wg_tv = view.findViewById(R.id.custom_read_wg_tv);
        release_wg = view.findViewById(R.id.custom_release_wg);
        release_wg.setOnClickListener(this);
        release_wg_tv = view.findViewById(R.id.custom_release_wg_tv);
        clean_recent = view.findViewById(R.id.custom_clean_recent);
        clean_recent.setOnClickListener(this);
        clean_recent_tv = view.findViewById(R.id.custom_clean_recent_tv);
        endcall = view.findViewById(R.id.custom_endcall);
        endcall.setOnClickListener(this);
        endcall_tv = view.findViewById(R.id.custom_endcall_tv);

        get_pid = view.findViewById(R.id.custom_get_pid);
        get_pid.setOnClickListener(this);
        get_pid_et = view.findViewById(R.id.custom_get_pid_et);
        get_pid_tv = view.findViewById(R.id.custom_get_pid_tv);
        get_pid_info = view.findViewById(R.id.custom_get_pid_info);
        get_pid_info.setOnClickListener(this);
        get_pid_info_et = view.findViewById(R.id.custom_get_pid_info_et);
        get_pid_info_tv = view.findViewById(R.id.custom_get_pid_info_tv);
        kill_pid = view.findViewById(R.id.custom_kill_pid);
        kill_pid.setOnClickListener(this);
        kill_pid_et = view.findViewById(R.id.custom_kill_pid_et);
        kill_pid_tv = view.findViewById(R.id.custom_kill_pid_tv);
        dial = view.findViewById(R.id.custom_dial);
        dial.setOnClickListener(this);
        dial_et = view.findViewById(R.id.custom_dial_et);
        dial_tv = view.findViewById(R.id.custom_dial_tv);
        add_encryption = view.findViewById(R.id.custom_add_encryption);
        add_encryption.setOnClickListener(this);
        add_encryption_et = view.findViewById(R.id.custom_add_encryption_et);
        add_encryption_oldpwd_et = view.findViewById(R.id.custom_add_encryption_oldpwd_et);
        add_encryption_pwd_et = view.findViewById(R.id.custom_add_encryption_pwd_et);
        add_encryption_tv = view.findViewById(R.id.custom_add_encryption_tv);
        del_encryption = view.findViewById(R.id.custom_del_encryption);
        del_encryption.setOnClickListener(this);
        del_encryption_et = view.findViewById(R.id.custom_del_encryption_et);
        del_encryption_pwd_et = view.findViewById(R.id.custom_del_encryption_pwd_et);
        del_encryption_tv = view.findViewById(R.id.custom_del_encryption_tv);
        set_desktop_app = view.findViewById(R.id.custom_set_desktop_app);
        set_desktop_app.setOnClickListener(this);
        set_desktop_app_et = view.findViewById(R.id.custom_set_desktop_app_et);
        set_desktop_app_tv = view.findViewById(R.id.custom_set_desktop_app_tv);

        get_encryption = view.findViewById(R.id.custom_get_encryption);
        get_encryption.setOnClickListener(this);
        get_encryption_tv = view.findViewById(R.id.custom_get_encryption_tv);

        send_wg_card = view.findViewById(R.id.custom_send_wg_card);
        send_wg_card.setOnClickListener(this);
        send_wg_card_et = view.findViewById(R.id.custom_send_wg_card_et);
        send_wg_card_spinner = view.findViewById(R.id.custom_send_wg_card_spinner);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.wg_array));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        send_wg_card_spinner.setAdapter(mAdapter);
        send_wg_card_tv = view.findViewById(R.id.custom_send_wg_card_tv);

        send_wg_card2 = view.findViewById(R.id.custom_send_wg_card2);
        send_wg_card2.setOnClickListener(this);
        send_wg_card2_hid_et = view.findViewById(R.id.custom_send_wg_card2_hid_et);
        send_wg_card2_pid_et = view.findViewById(R.id.custom_send_wg_card2_pid_et);
        send_wg_card2_spinner = view.findViewById(R.id.custom_send_wg_card2_spinner);
        send_wg_card2_spinner.setAdapter(mAdapter);
        send_wg_card2_tv = view.findViewById(R.id.custom_send_wg_card2_tv);

        set_relay_mode = view.findViewById(R.id.custom_set_relay_mode);
        set_relay_mode.setOnClickListener(this);
        set_relay_mode_et = view.findViewById(R.id.custom_set_relay_mode_et);
        set_relay_mode_spinner = view.findViewById(R.id.custom_set_relay_mode_spinner);
        ArrayAdapter<String> msAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.relay_array));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_relay_mode_spinner.setAdapter(msAdapter);
        set_relay_mode_tv = view.findViewById(R.id.custom_set_relay_mode_tv);

        set_relay_switch = view.findViewById(R.id.custom_set_relay_switch);

        custom_ledbrightness_seekbar0 = view.findViewById(R.id.custom_ledbrightness_seekbar0);
        custom_ledbrightness_seekbar1 = view.findViewById(R.id.custom_ledbrightness_seekbar1);
        custom_ledbrightness_value0 = view.findViewById(R.id.custom_ledbrightness_value0);
        custom_ledbrightness_value1 = view.findViewById(R.id.custom_ledbrightness_value1);
        initLedSeekbar();
    }

    private void readData() {
        unInitListener();
        if (relayIoMode >= 0 && relayIoMode <= 2) {
            set_relay_mode_spinner.setSelection(relayIoMode);
        }
        set_relay_switch.setChecked(relayIoEnable == 1);

        custom_ledbrightness_value0.setText(String.valueOf(mLedBrightness0));
        custom_ledbrightness_value1.setText(String.valueOf(mLedBrightness1));
        custom_ledbrightness_seekbar0.setProgress(mLedBrightness0 - ledMin);
        custom_ledbrightness_seekbar1.setProgress(mLedBrightness1 - ledMin);

        set_desktop_app_tv.setText(desktopApp);

        initListener();
    }

    private void initListener() {
        send_wg_card_spinner.setOnItemSelectedListener(this);
        send_wg_card2_spinner.setOnItemSelectedListener(this);
        set_relay_mode_spinner.setOnItemSelectedListener(this);
        set_relay_switch.setOnCheckedChangeListener(this);
    }

    private void unInitListener() {
        send_wg_card_spinner.setOnItemSelectedListener(null);
        send_wg_card2_spinner.setOnItemSelectedListener(null);
        set_relay_mode_spinner.setOnItemSelectedListener(null);
        set_relay_switch.setOnCheckedChangeListener(null);
    }

    @Override
    public void onClick(View view) {
        hideInputKeyboard(mContext);
        switch (view.getId()) {
            case R.id.custom_read_wg:
                readWg();
                break;
            case R.id.custom_release_wg:
                release_wg_tv.setText(smdtManagerNew.custom_releaseWiegandRead() + "");
                break;
            case R.id.custom_send_wg_card:
                sendWgCardID();
                break;
            case R.id.custom_send_wg_card2:
                sendWgCardHIDPID();
                break;
            case R.id.custom_set_relay_mode:
                setRelayMode();
                break;
            case R.id.custom_get_pid:
                getAppPid();
                break;
            case R.id.custom_get_pid_info:
                getAppPkg();
                break;
            case R.id.custom_kill_pid:
                killApp();
                break;
            case R.id.custom_clean_recent:
                clean_recent_tv.setText(smdtManagerNew.custom_cleanRecentTasks() + "");
                break;
            case R.id.custom_dial:
                dialNoActivity();
                break;
            case R.id.custom_endcall:
                endcall_tv.setText(smdtManagerNew.custom_endCall() + "");
                break;
            case R.id.custom_get_encryption:
                showEncrytionDialog();
                break;
            case R.id.custom_add_encryption:
                addEncryptionList();
                break;
            case R.id.custom_del_encryption:
                delEncryptionList();
                break;
            case R.id.custom_set_desktop_app:
                setDesktopApp();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        hideInputKeyboard(mContext);
        if (!compoundButton.isPressed()) {
            DEBUG("Custom isPressed from code or what?:" + compoundButton.isPressed());
            return;
        }
        switch (compoundButton.getId()) {
            case R.id.custom_set_relay_switch:
                //失败反选
                if (smdtManagerNew.custom_setRelayIoEnable(isChecked) != 0) {
                    set_relay_switch.setChecked(!isChecked);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        hideInputKeyboard(mContext);
        switch (adapterView.getId()) {
            case R.id.custom_send_wg_card_spinner:
                break;
            case R.id.custom_send_wg_card2_spinner:
                break;
            case R.id.custom_set_relay_mode_spinner:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            relayIoMode = smdtManagerNew.custom_getRelayIoMode();
            relayIoEnable = smdtManagerNew.custom_getRelayIoEnable();
            mLedBrightness0 = smdtManagerNew.custom_getLedBrightness(0);
            mLedBrightness1 = smdtManagerNew.custom_getLedBrightness(1);
            desktopApp = smdtManagerNew.custom_getDesktopApp();
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }

    private void sendWgCardID() {
        String idcard = send_wg_card_et.getText().toString();
        int transformat = send_wg_card_spinner.getSelectedItemPosition() + 1;
        send_wg_card_tv.setText(smdtManagerNew.custom_sendWiegandCard(idcard, transformat) + "");
    }

    private void sendWgCardHIDPID() {
        String hid = send_wg_card2_hid_et.getText().toString();
        String pid = send_wg_card2_pid_et.getText().toString();
        int transformat = send_wg_card2_spinner.getSelectedItemPosition() + 1;
        send_wg_card2_tv.setText(smdtManagerNew.custom_sendWiegandCardHIDPID(hid, pid, transformat) + "");
    }

    private void readWg() {
        smdtManagerNew.custom_readWiegandData(new SmdtManagerNew.WiegandCallback() {
            @Override
            public void onReadData(String data) throws RemoteException {
                if (!TextUtils.isEmpty(data)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("data", data);
                    Message message = mHandler.obtainMessage();
                    message.what = REFRESH_WG_DATA;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        });
    }

    private void setRelayMode() {
        String str = set_relay_mode_et.getText().toString();
        if (TextUtils.isEmpty(str)) {
            return;
        }
        int delay = Integer.parseInt(str);
        int mode = set_relay_mode_spinner.getSelectedItemPosition();

        set_relay_mode_tv.setText(smdtManagerNew.custom_setRelayIoMode(mode, delay) + "");
    }

    private void getAppPid() {
        String packageName = get_pid_et.getText().toString();
        get_pid_tv.setText(smdtManagerNew.custom_getPidProcess(packageName) + "");
    }

    private void getAppPkg() {
        String pid = get_pid_info_et.getText().toString();
        if (TextUtils.isEmpty(pid)) {
            return;
        }
        get_pid_info_tv.setText(smdtManagerNew.custom_getPidProcessInfo(Integer.parseInt(pid)) + "");
    }

    private void killApp() {
        String pid = kill_pid_et.getText().toString();
        if (TextUtils.isEmpty(pid)) {
            return;
        }

        kill_pid_tv.setText(smdtManagerNew.custom_killPidProcess(Integer.parseInt(pid)) + "");
    }

    private void dialNoActivity() {
        String number = dial_et.getText().toString();
        if (TextUtils.isEmpty(number)) {
            return;
        }
        dial_tv.setText(smdtManagerNew.custom_dial(number) + "");
    }

    private void showEncrytionDialog() {
        List<String> list = smdtManagerNew.custom_getAppliesEncryption(null);
        if (list == null || list.size() == 0) {
            get_encryption_tv.setText("-1");
            return;
        }
        get_encryption_tv.setText("");

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
                encryDialog.dismiss();
            }
        });

        encryDialog = alertBuilder.create();
        encryDialog.show();
    }

    private void addEncryptionList() {
        String packageName = add_encryption_et.getText().toString();
        String oldpwd = add_encryption_oldpwd_et.getText().toString();
        String pwd = add_encryption_pwd_et.getText().toString();
        add_encryption_tv.setText(smdtManagerNew.custom_addAppliesEncryption(packageName, oldpwd, pwd) + "");
    }

    private void delEncryptionList() {
        String packageName = del_encryption_et.getText().toString();
        String pwd = del_encryption_pwd_et.getText().toString();
        del_encryption_tv.setText(smdtManagerNew.custom_delAppliesEncryption(packageName, pwd) + "");
    }

    private void initLedSeekbar() {
        custom_ledbrightness_seekbar0.setMinValue(ledMin);
        custom_ledbrightness_seekbar0.setMax(ledMax);
        custom_ledbrightness_seekbar1.setMinValue(ledMin);
        custom_ledbrightness_seekbar1.setMax(ledMax);

        custom_ledbrightness_seekbar0.setOnSeekBarChangeListener(seekBarChangeListener);
        custom_ledbrightness_seekbar1.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (drag_led) {
                if (seekBar.getId() == R.id.custom_ledbrightness_seekbar0) {
                    mLedBrightness0 = progress;
                    custom_ledbrightness_value0.setText(String.valueOf(mLedBrightness0));
                } else {
                    mLedBrightness1 = progress;
                    custom_ledbrightness_value1.setText(String.valueOf(mLedBrightness1));
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            hideInputKeyboard(mContext);
            drag_led = true;
            if (seekBar.getId() == R.id.custom_ledbrightness_seekbar0) {
                custom_ledbrightness_value0.setText(String.valueOf(mLedBrightness0));
            } else {
                custom_ledbrightness_value1.setText(String.valueOf(mLedBrightness1));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            drag_led = false;
            if (seekBar.getId() == R.id.custom_ledbrightness_seekbar0) {
                int result = smdtManagerNew.custom_setLedBrightness(0, mLedBrightness0);
                if (result == 0) {
                    custom_ledbrightness_value0.setText(mLedBrightness0 + "");
                } else {
                    custom_ledbrightness_value0.setText(result + "");
                }
            } else {
                int result = smdtManagerNew.custom_setLedBrightness(1, mLedBrightness1);
                if (result == 0) {
                    custom_ledbrightness_value1.setText(mLedBrightness1 + "");
                } else {
                    custom_ledbrightness_value1.setText(result + "");
                }
            }
        }
    };

    private void setDesktopApp() {
        String pkg = set_desktop_app_et.getText().toString();
        int result = smdtManagerNew.custom_setDesktopApp(pkg);
        if (result == 0 && !TextUtils.isEmpty(pkg)) {
            set_desktop_app_tv.setText(smdtManagerNew.custom_getDesktopApp());
        } else {
            set_desktop_app_tv.setText(result + "");
        }
    }
}
