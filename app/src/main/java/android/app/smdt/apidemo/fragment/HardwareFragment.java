/**
 * File: HardwareFragment.java
 * Author: Xu Linrui <lrxu@smdt.com.cn>
 * Created on 2 July 2021
 **/
package android.app.smdt.apidemo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.smdt.SmdtManagerNew;
import android.app.smdt.apidemo.R;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static android.app.smdt.util.ErrorCode.RET_API_ERR_NG;
import static android.app.smdt.util.ErrorCode.RET_API_OK;
import static android.app.smdt.util.VariableUtil.PARTITION_EEPROM;
import static android.app.smdt.util.VariableUtil.PARTITION_EMMC;

public class HardwareFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    //data
    private String[] uart_array;
    private boolean hex = false;
    private String uart;
    private String uartPath;
    private int usbPowerEnable;
    private String[] lightValues;
    private int lightEnable;
    //view
    //button
    private Button get_sdcard, close_uart, receive_uart, set_camera;
    private TextView get_sdcard_tv, close_uart_tv, receive_uart_tv, set_camera_tv;
    //button+editText
    private Button umount_ex, write_private, read_private;
    private EditText umount_ex_et, write_areaid_et, write_start_et, write_size_et, write_data_et, read_areaid_et, read_start_et, read_size_et;
    private TextView umount_ex_tv, write_private_tv, read_private_tv;
    //button+dialog
    private Button get_usb;
    private TextView get_usb_tv;
    private Dialog usbDialog;
    //button+spinner
    private Button open_uart;
    private Spinner open_uart_rate, open_uart_databits, open_uart_stopbits, open_uart_parity, open_uart_flow_ctrl;
    private TextView open_uart_tv;
    //spinner
    private Spinner get_uart_spinner;
    private TextView get_uart_tv;
    //spinner+switch
    private Spinner set_usbpower_spinner,set_light_spinner;
    private Switch set_usbpower,set_light;
    //button+radio
    private Button get_private;
    private RadioGroup get_private_group;
    private RadioButton emmc, eeprom0, eeprom1, eeprom2;
    private TextView get_private_tv;
    //button+radio+editText
    private Button send_uart;
    private RadioGroup send_uart_group;
    private RadioButton uart_text, uart_hex;
    private EditText send_uart_et;
    private TextView send_uart_tv;
    //Handler
    private static final int REFRESH_DATA = 0x00;
    private static final int REFRESH_UART_DATA = 0x01;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_DATA) {
                readData();
            } else if (msg.what == REFRESH_UART_DATA) {
                Bundle bundle = msg.getData();
                String data = bundle.getString("data");
                receive_uart_tv.setText(data);
            }
        }
    };

    @Override
    public int getLayoutResource() {
        return R.layout.hw_fragment;
    }

    @Override
    public void onInit() {
        initView();
        lightValues = mRes.getStringArray(R.array.three_light);
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
        closeUart();
    }

    private void initView() {
        get_sdcard = view.findViewById(R.id.hw_get_sdcard);
        get_sdcard.setOnClickListener(this);
        get_sdcard_tv = view.findViewById(R.id.hw_get_sdcard_tv);
        close_uart = view.findViewById(R.id.hw_close_uart);
        close_uart.setOnClickListener(this);
        close_uart_tv = view.findViewById(R.id.hw_close_uart_tv);
        receive_uart = view.findViewById(R.id.hw_receive_uart);
        receive_uart.setOnClickListener(this);
        receive_uart_tv = view.findViewById(R.id.hw_receive_uart_tv);
        set_camera = view.findViewById(R.id.hw_set_camera);
        set_camera.setOnClickListener(this);
        set_camera_tv = view.findViewById(R.id.hw_set_camera_tv);

        get_usb = view.findViewById(R.id.hw_get_usb);
        get_usb.setOnClickListener(this);
        get_usb_tv = view.findViewById(R.id.hw_get_usb_tv);
        umount_ex = view.findViewById(R.id.hw_umount_ex);
        umount_ex.setOnClickListener(this);
        umount_ex_et = view.findViewById(R.id.hw_umount_ex_et);
        umount_ex_tv = view.findViewById(R.id.hw_umount_ex_tv);
        write_private = view.findViewById(R.id.hw_write_private);
        write_private.setOnClickListener(this);
        write_areaid_et = view.findViewById(R.id.hw_write_private_areaid_et);
        write_start_et = view.findViewById(R.id.hw_write_private_start_et);
        write_size_et = view.findViewById(R.id.hw_write_private_size_et);
        write_data_et = view.findViewById(R.id.hw_write_private_data_et);
        write_private_tv = view.findViewById(R.id.hw_write_private_tv);
        read_private = view.findViewById(R.id.hw_read_private);
        read_private.setOnClickListener(this);
        read_areaid_et = view.findViewById(R.id.hw_read_private_areaid_et);
        read_start_et = view.findViewById(R.id.hw_read_private_start_et);
        read_size_et = view.findViewById(R.id.hw_read_private_size_et);
        read_private_tv = view.findViewById(R.id.hw_read_private_tv);

        open_uart = view.findViewById(R.id.hw_open_uart);
        open_uart.setOnClickListener(this);
        open_uart_rate = view.findViewById(R.id.hw_open_uart_rate);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.baudrates_value));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        open_uart_rate.setAdapter(mAdapter);
        open_uart_rate.setSelection(12);
        open_uart_databits = view.findViewById(R.id.hw_open_uart_databits);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.data_bits));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        open_uart_databits.setAdapter(mAdapter);
        open_uart_stopbits = view.findViewById(R.id.hw_open_uart_stopbits);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.stop_bits));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        open_uart_stopbits.setAdapter(mAdapter);
        open_uart_parity = view.findViewById(R.id.hw_open_uart_parity);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.parity));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        open_uart_parity.setAdapter(mAdapter);
        open_uart_flow_ctrl = view.findViewById(R.id.hw_open_uart_flow_ctrl);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.flow_ctrl));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        open_uart_flow_ctrl.setAdapter(mAdapter);
        open_uart_tv = view.findViewById(R.id.hw_open_uart_tv);

        get_uart_spinner = view.findViewById(R.id.hw_get_uart_spinner);
        uart_array = mRes.getStringArray(R.array.uart_array);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, uart_array);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        get_uart_spinner.setAdapter(mAdapter);
        get_uart_tv = view.findViewById(R.id.hw_get_uart_tv);

        set_usbpower_spinner = view.findViewById(R.id.hw_set_usbpower_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.usb_port));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_usbpower_spinner.setAdapter(mAdapter);
        set_usbpower = view.findViewById(R.id.hw_set_usbpower_switch);
        set_light_spinner = view.findViewById(R.id.hw_set_light_spinner);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.three_light));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_light_spinner.setAdapter(mAdapter);
        set_light = view.findViewById(R.id.hw_set_light_switch);

        get_private = view.findViewById(R.id.hw_get_private);
        get_private.setOnClickListener(this);
        get_private_group = view.findViewById(R.id.hw_get_private_group);
        get_private_group.setOnCheckedChangeListener(this);
        emmc = view.findViewById(R.id.hw_emmc);
        eeprom0 = view.findViewById(R.id.hw_eeprom0);
        eeprom1 = view.findViewById(R.id.hw_eeprom1);
        eeprom2 = view.findViewById(R.id.hw_eeprom2);
        get_private_tv = view.findViewById(R.id.hw_get_private_tv);

        send_uart = view.findViewById(R.id.hw_send_uart);
        send_uart.setOnClickListener(this);
        send_uart_group = view.findViewById(R.id.hw_send_uart_group);
        send_uart_group.setOnCheckedChangeListener(this);
        uart_text = view.findViewById(R.id.hw_text);
        uart_hex = view.findViewById(R.id.hw_hex);
        send_uart_et = view.findViewById(R.id.hw_send_uart_et);
        send_uart_tv = view.findViewById(R.id.hw_send_uart_tv);
    }

    private void readData() {
        unInitListener();
        get_uart_tv.setText(uartPath);
        set_usbpower.setChecked(usbPowerEnable == 1);
        set_light.setChecked(lightEnable == 1);
        initListener();
    }

    private void initListener() {
        open_uart_rate.setOnItemSelectedListener(this);
        open_uart_databits.setOnItemSelectedListener(this);
        open_uart_stopbits.setOnItemSelectedListener(this);
        open_uart_parity.setOnItemSelectedListener(this);
        open_uart_flow_ctrl.setOnItemSelectedListener(this);
        get_uart_spinner.setOnItemSelectedListener(this);
        set_usbpower_spinner.setOnItemSelectedListener(this);
        set_light_spinner.setOnItemSelectedListener(this);
        set_usbpower.setOnCheckedChangeListener(this);
        set_light.setOnCheckedChangeListener(this);
    }

    private void unInitListener() {
        open_uart_rate.setOnItemSelectedListener(null);
        open_uart_databits.setOnItemSelectedListener(null);
        open_uart_stopbits.setOnItemSelectedListener(null);
        open_uart_parity.setOnItemSelectedListener(null);
        open_uart_flow_ctrl.setOnItemSelectedListener(null);
        get_uart_spinner.setOnItemSelectedListener(null);
        set_usbpower_spinner.setOnItemSelectedListener(null);
        set_light_spinner.setOnItemSelectedListener(null);
        set_usbpower.setOnCheckedChangeListener(null);
        set_light.setOnCheckedChangeListener(null);
    }

    @Override
    public void onClick(View view) {
        hideInputKeyboard(mContext);
        String str;
        switch (view.getId()) {
            case R.id.hw_get_sdcard:
                get_sdcard_tv.setText(smdtManagerNew.dev_getSDcardPath());
                break;
            case R.id.hw_get_usb:
                showUsbDialog();
                break;
            case R.id.hw_umount_ex:
                str = umount_ex_et.getText().toString();
                umount_ex_tv.setText(smdtManagerNew.dev_unmountExternalStorage(str, true, false) + "");
                break;
            case R.id.hw_get_private:
                getPrivate();
                break;
            case R.id.hw_write_private:
                writePrivate();
                break;
            case R.id.hw_read_private:
                readPrivate();
                break;
            case R.id.hw_open_uart:
                openUart();
                break;
            case R.id.hw_close_uart:
                close_uart_tv.setText(closeUart() + "");
                break;
            case R.id.hw_send_uart:
                sendUart();
                break;
            case R.id.hw_receive_uart:
                //                receiveUart();
                break;
            case R.id.hw_set_camera:
                Intent intent = new Intent();
                String packageName = "android.app.smdt.smdtsettings";
                String className = "android.app.smdt.smdtsettings.service.CameraAdjustService";
                intent.setClassName(packageName, className);
                try {
                    mContext.startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    set_camera_tv.setText(RET_API_ERR_NG + "");
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        hideInputKeyboard(mContext);
        switch (adapterView.getId()) {
            case R.id.hw_get_uart_spinner:
                get_uart_tv.setText(smdtManagerNew.dev_getUartPath(uart_array[i]));
                break;
            case R.id.hw_set_usbpower_spinner:
                if (i == 0) {
                    set_usbpower.setChecked(smdtManagerNew.dev_getUsbPower(0, 1) == 1);
                } else {
                    set_usbpower.setChecked(smdtManagerNew.dev_getUsbPower(1, i) == 1);
                }
                break;
            case R.id.hw_set_light_spinner:
                set_light.setChecked(smdtManagerNew.dev_getLedState(lightValues[i]) == 1);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        hideInputKeyboard(mContext);
        if (!compoundButton.isPressed()) {
            DEBUG( "Hardware isPressed from code or what?:" + compoundButton.isPressed());
            return;
        }

        switch (compoundButton.getId()) {
            case R.id.hw_set_usbpower_switch:
                int type, usbport;
                int select = set_usbpower_spinner.getSelectedItemPosition();
                if (select == 0) {
                    //otg 1
                    type = 0;
                    usbport = 1;
                } else {
                    //host 1,2,3
                    type = 1;
                    usbport = select;
                }
                //失败反选
                if (smdtManagerNew.dev_setUsbPower(type, usbport, isChecked) != 0) {
                    set_usbpower.setChecked(!isChecked);
                }
                break;
            case R.id.hw_set_light_switch:
                //失败反选
                if (smdtManagerNew.dev_setLedLighted(lightValues[set_light_spinner.getSelectedItemPosition()], isChecked) != 0) {
                    set_light.setChecked(!isChecked);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        hideInputKeyboard(mContext);
        switch (radioGroup.getId()) {
            case R.id.hw_get_private_group:
                switch (checkedId) {
                    case R.id.hw_emmc:
                        write_areaid_et.setVisibility(View.GONE);
                        read_areaid_et.setVisibility(View.GONE);
                        break;
                    default:
                        write_areaid_et.setVisibility(View.VISIBLE);
                        read_areaid_et.setVisibility(View.VISIBLE);
                        break;
                }
                break;
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            uartPath = smdtManagerNew.dev_getUartPath(get_uart_spinner.getSelectedItem().toString());
            int select = set_usbpower_spinner.getSelectedItemPosition();
            if (select == 0) {
                usbPowerEnable = smdtManagerNew.dev_getUsbPower(0, 1);
            } else {
                usbPowerEnable = smdtManagerNew.dev_getUsbPower(1, select);
            }
            lightEnable = smdtManagerNew.dev_getLedState(lightValues[set_light_spinner.getSelectedItemPosition()]);
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }

    private void showUsbDialog() {
        List<String> list = smdtManagerNew.dev_getUdiskPath();
        if (list == null || list.size() == 0) {
            get_usb_tv.setText("-1");
            return;
        }
        get_usb_tv.setText("");

        String[] usblist = new String[list.size()];
        list.toArray(usblist);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mRes.getString(R.string.long_click_msg));
        alertBuilder.setItems(usblist, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //获取剪贴板管理器
                ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                //创建能够存入剪贴板的ClipData对象
                ClipData mClipData = ClipData.newPlainText("copydata", usblist[which]);
                //将ClipData数据复制到剪贴板：
                mClipboardManager.setPrimaryClip(mClipData);
                Toast.makeText(mContext, mRes.getString(R.string.long_click_toast), Toast.LENGTH_SHORT).show();
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                usbDialog.dismiss();
            }
        });

        usbDialog = alertBuilder.create();
        usbDialog.show();
    }

    private void getPrivate() {
        int result = -1;
        if (emmc.isChecked()) {
            result = smdtManagerNew.dev_getPublicPartitionSize(PARTITION_EMMC, 0);
        } else if (eeprom0.isChecked()) {
            result = smdtManagerNew.dev_getPublicPartitionSize(PARTITION_EEPROM, 0);
        } else if (eeprom1.isChecked()) {
            result = smdtManagerNew.dev_getPublicPartitionSize(PARTITION_EEPROM, 1);
        } else if (eeprom2.isChecked()) {
            result = smdtManagerNew.dev_getPublicPartitionSize(PARTITION_EEPROM, 2);
        }
        get_private_tv.setText(result + "");
    }

    private void writePrivate() {
        try {
            int reuslt = -1;
            String start = write_start_et.getText().toString();
            String size = write_size_et.getText().toString();
            String data = write_data_et.getText().toString();
            if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(size) && !TextUtils.isEmpty(data)) {
                if (data.length() < Integer.parseInt(size)) {
                    int num = Integer.parseInt(size) - data.length();
                    data = addBlank(data, num);
                }
                if (emmc.isChecked()) {
                    reuslt = smdtManagerNew.dev_writePublicPartition(PARTITION_EMMC, 0, 0, Integer.parseInt(start),
                            Integer.parseInt(size), data.getBytes("utf-8"));
                } else {
                    String areaId = write_areaid_et.getText().toString();
                    if (!TextUtils.isEmpty(areaId)) {
                        int deviceId = 0;
                        if (eeprom0.isChecked()) {
                            deviceId = 0;
                        } else if (eeprom1.isChecked()) {
                            deviceId = 1;
                        } else if (eeprom2.isChecked()) {
                            deviceId = 2;
                        }
                        reuslt = smdtManagerNew.dev_writePublicPartition(PARTITION_EEPROM, deviceId, Integer.parseInt(areaId), Integer.parseInt(start),
                                Integer.parseInt(size), data.getBytes());
                    }
                }
            }
            write_private_tv.setText(reuslt + "");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readPrivate() {
        byte[] data = null;
        String start = read_start_et.getText().toString();
        String size = read_size_et.getText().toString();
        if (!TextUtils.isEmpty(start) && !TextUtils.isEmpty(size)) {
            if (emmc.isChecked()) {
                data = smdtManagerNew.dev_readPublicPartition(PARTITION_EMMC, 0, 0, Integer.parseInt(start), Integer.parseInt(size));
            } else {
                String areaId = read_areaid_et.getText().toString();
                if (!TextUtils.isEmpty(areaId)) {
                    int deviceId = 0;
                    if (eeprom0.isChecked()) {
                        deviceId = 0;
                    } else if (eeprom1.isChecked()) {
                        deviceId = 1;
                    } else if (eeprom2.isChecked()) {
                        deviceId = 2;
                    }
                    data = smdtManagerNew.dev_readPublicPartition(PARTITION_EEPROM, deviceId, Integer.parseInt(areaId), Integer.parseInt(start),
                            Integer.parseInt(size));
                }
            }
        }
        if (data != null) {
            String s = new String(data);
            read_private_tv.setText(s);
        } else {
            read_private_tv.setText("-1");
        }

    }

    private String addBlank(String blank, int n) {
        String tem = "";
        for (int i = 0; i < n; i++) {
            tem = tem + " ";
        }
        return blank + tem;
    }

    private void openUart() {
        int result = -1;
        uart = get_uart_tv.getText().toString();
        int baudrate = Integer.parseInt(open_uart_rate.getSelectedItem().toString());
        int databits = Integer.parseInt(open_uart_databits.getSelectedItem().toString());
        int stopbits = Integer.parseInt(open_uart_stopbits.getSelectedItem().toString());
        int parity = Integer.parseInt(open_uart_parity.getSelectedItem().toString());
        int flow_ctrl = Integer.parseInt(open_uart_flow_ctrl.getSelectedItem().toString());
        result = smdtManagerNew.dev_openUart(uart, baudrate, databits, stopbits, parity, flow_ctrl);
        open_uart_tv.setText(result + "");
        if(result == RET_API_OK) {
            receiveUart();
        }
    }

    private int closeUart() {
        return smdtManagerNew.dev_closeUart(uart);
    }

    private void sendUart() {
        if (uart_hex.isChecked()) {
            hex = true;
        } else {
            hex = false;
        }
        String data = send_uart_et.getText().toString();
        int result = smdtManagerNew.dev_sendUart(uart, data, hex);
        send_uart_tv.setText(result + "");
    }

    private void receiveUart() {
        smdtManagerNew.dev_receiveUart(uart, new SmdtManagerNew.DataCallback() {
            @Override
            public void onDataReceive(byte[] buffer, int size) throws RemoteException {
                if (buffer != null) {
                    DEBUG( "data size :" + size);
                    String str;
                    if (hex) {
                        str = byteToHexStr(buffer, size);
                    } else {
                        str = byteToString(buffer);
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("data", str);
                    Message message = mHandler.obtainMessage();
                    message.what = REFRESH_UART_DATA;
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        });
    }

    public String byteToString(byte[] data) {
        int index = data.length;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == 0) {
                index = i;
                break;
            }
        }
        byte[] temp = new byte[index];
        Arrays.fill(temp, (byte) 0);
        System.arraycopy(data, 0, temp, 0, index);
        String str;
        try {
            str = new String(temp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
        return str;
    }

    public static String byteToHexStr(byte[] byteArray, int size) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[size * 2];
        for (int j = 0; j < size; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
