/**
 * File: NetFragment.java
 * Author: Xu Linrui <lrxu@smdt.com.cn>
 * Created on 1 July 2021
 **/
package android.app.smdt.apidemo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.smdt.NetworkInfoData;
import android.app.smdt.apidemo.R;
import android.app.smdt.apidemo.ui.StaticIpDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.smdt.apidemo.ui.MethodUtil.PROP_ETH1_ENABLE;
import static android.app.smdt.apidemo.ui.MethodUtil.getSystemProperties;
import static android.app.smdt.util.ErrorCode.RET_API_ERR_NG;
import static android.app.smdt.util.VariableUtil.CONNECT_TYPE_ETHERNET;
import static android.app.smdt.util.VariableUtil.CONNECT_TYPE_MOBILE;
import static android.app.smdt.util.VariableUtil.CONNECT_TYPE_WIFI;
import static android.app.smdt.util.VariableUtil.MODE_DHCP;
import static android.app.smdt.util.VariableUtil.MODE_STATIC;
import static android.app.smdt.util.VariableUtil.SECURITY_WPA;
import static android.app.smdt.util.VariableUtil.TYPE_ETH0;
import static android.app.smdt.util.VariableUtil.TYPE_ETH1;
import static android.app.smdt.util.VariableUtil.TYPE_MOBILE;
import static android.app.smdt.util.VariableUtil.TYPE_WLAN;

public class NetFragment extends BaseFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener {

    private IntentFilter mIntentFilter;
    private SmdtReceiver mSmdtReceiver;
    //data
    private int wifiApEnable;
    private int netWorkMultiEnable;
    private int netWorkEnable;
    private String[] network_array;
    private int select_network = 0;
    private String networkType;
    private String macEth0, macEth1;
    private String macWifi;
    private int wifiRssi;
    private int modelEth0, modelEth1;
    private int modelWifi;
    private NetworkInfoData mNetworkInfoData;
    private String imeiStr;
    private String iccidStr;
    private String imsiStr;
    //view
    private TextView get_current_net, get_eth0_macaddress, get_eth1_macaddress, get_wlan_macaddress,
            get_eth0_network, get_eth1_network, get_wlan_network, get_wlan_rssi,
            get_ipaddress, get_gateway, get_netmask, get_dns1, get_dns2,
            imei, iccid, imsi;
    private LinearLayout line_eth1_network,line_eth1_mac;
    //button
    private Button set_network_protect, net_set_network_priority;
    private TextView set_network_protect_tv, net_set_network_priority_tv;
    private Dialog networkPriorityDialog;
    //button+radio+dialog
    private Button set_network;
    private RadioButton set_network_eth0, set_network_eth1, set_network_wlan;
    private Dialog netWorkDialog;
    private TextView set_network_tv;
    //button+edittext+spinner
    private Button set_wifi_connect;
    private EditText set_wifi_account_et, set_wifi_pwd_et;
    private Spinner set_wifi_spinner;
    private TextView set_wifi_connect_tv;
    //switch
    private Switch set_wifi_ap,set_network_multi_switch;
    //spinner+switch
    private Spinner set_network_enable_spinner;
    private Switch set_network_enable;
    //list
    private String[] networkPriorityList;
    //Handler
    private static final int REFRESH_DATA = 0x00;
    private static final int REFRESH_STATUS = 0x01;
    private static final int REFRESH_UI = 0x02;
    private static final int REFRESH_WIFI_RSSI = 0x03;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_DATA) {
                readData();
            } else if (msg.what == REFRESH_STATUS) {
                NetThread mNetThread = new NetThread();
                mNetThread.start();
            } else if (msg.what == REFRESH_UI) {
                updateStatus();
            } else if (msg.what == REFRESH_WIFI_RSSI) {
                get_wlan_rssi.setText("Level:" + smdtManagerNew.net_getWifiRssi(4));
            }
        }
    };
    //Receiver
    public static final String ACTION_WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLED = 13;

    /**
     * BroadcastReceiver 广播
     */
    private class SmdtReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            DEBUG("onReceive:" + action);
            if (/*WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)
                    ||*/ WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                //接收到WIFI状态改变的广播
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                if (network_array[select_network].equals("wlan0")) {
                    if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                        set_network_enable.setChecked(true);
                    } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                        set_network_enable.setChecked(false);
                    }
                }
                mHandler.sendEmptyMessage(REFRESH_STATUS);
            } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
                //接收到WIFI信号改变的广播
                mHandler.sendEmptyMessage(REFRESH_WIFI_RSSI);
            } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                //接收到网络状态改变的广播
                mHandler.sendEmptyMessage(REFRESH_STATUS);
            } else if (ACTION_WIFI_AP_STATE_CHANGED.equals(action)) {
                //刷新热点状态变化
                int state = intent.getIntExtra("wifi_state", 0);
                if (state == WIFI_AP_STATE_ENABLED) {
                    set_wifi_ap.setChecked(true);
                } else if (state == WIFI_AP_STATE_DISABLED) {
                    set_wifi_ap.setChecked(false);
                }
            }
        }
    }

    @Override
    public int getLayoutResource() {
        return R.layout.net_fragment;
    }

    @Override
    public void onInit() {
        initView();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(ACTION_WIFI_AP_STATE_CHANGED);
        onUserResume();
    }

    @Override
    public void onUserResume() {
        ReadThread mReadThread = new ReadThread();
        mReadThread.start();
        NetThread mNetThread = new NetThread();
        mNetThread.start();
    }

    @Override
    public void onUserPause() {
        super.onUserPause();
        if (mSmdtReceiver != null) {
            mContext.unregisterReceiver(mSmdtReceiver);
            mSmdtReceiver = null;
        }
    }

    private void initView() {
        line_eth1_network = view.findViewById(R.id.line_eth1_network);
        line_eth1_mac = view.findViewById(R.id.line_eth1_mac);
        boolean enableEth1 = Boolean.parseBoolean(getSystemProperties(PROP_ETH1_ENABLE, "false"));
        if (!enableEth1) {
            line_eth1_network.setVisibility(View.GONE);
            line_eth1_mac.setVisibility(View.GONE);
        }

        get_current_net = view.findViewById(R.id.net_get_current_net);
        get_eth0_macaddress = view.findViewById(R.id.net_get_eth0_macaddress);
        get_eth1_macaddress = view.findViewById(R.id.net_get_eth1_macaddress);
        get_wlan_macaddress = view.findViewById(R.id.net_get_wlan_macaddress);
        get_eth0_network = view.findViewById(R.id.net_get_eth0_network);
        get_eth1_network = view.findViewById(R.id.net_get_eth1_network);
        get_wlan_network = view.findViewById(R.id.net_get_wlan_network);
        get_wlan_rssi = view.findViewById(R.id.net_get_wlan_rssi);
        get_ipaddress = view.findViewById(R.id.net_get_ipaddress);
        get_gateway = view.findViewById(R.id.net_get_gateway);
        get_netmask = view.findViewById(R.id.net_get_netmask);
        get_dns1 = view.findViewById(R.id.net_get_dns1);
        get_dns2 = view.findViewById(R.id.net_get_dns2);
        imei = view.findViewById(R.id.imei);
        iccid = view.findViewById(R.id.iccid);
        imsi = view.findViewById(R.id.imsi);

        set_network_protect = view.findViewById(R.id.net_set_network_protect);
        set_network_protect.setOnClickListener(this);
        set_network_protect_tv = view.findViewById(R.id.net_set_network_protect_tv);
        net_set_network_priority = view.findViewById(R.id.net_set_network_priority);
        net_set_network_priority.setOnClickListener(this);
        net_set_network_priority_tv = view.findViewById(R.id.net_set_network_priority_tv);

        set_network = view.findViewById(R.id.net_set_network);
        set_network.setOnClickListener(this);
        set_network_eth0 = view.findViewById(R.id.net_set_network_eth0);
        set_network_eth1 = view.findViewById(R.id.net_set_network_eth1);
        set_network_wlan = view.findViewById(R.id.net_set_network_wlan);
        set_network_tv = view.findViewById(R.id.net_set_network_tv);

        set_wifi_connect = view.findViewById(R.id.net_set_wifi_connect);
        set_wifi_connect.setOnClickListener(this);
        set_wifi_account_et = view.findViewById(R.id.net_set_wifi_account_et);
        set_wifi_pwd_et = view.findViewById(R.id.net_set_wifi_pwd_et);
        set_wifi_spinner = view.findViewById(R.id.net_set_wifi_spinner);
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, mRes.getStringArray(R.array.wifi_connect_type));
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_wifi_spinner.setAdapter(mAdapter);
        set_wifi_spinner.setSelection(SECURITY_WPA);
        set_wifi_connect_tv = view.findViewById(R.id.net_set_wifi_connect_tv);

        set_wifi_ap = view.findViewById(R.id.net_set_wifi_ap_switch);
        set_network_multi_switch = view.findViewById(R.id.net_set_network_multi_switch);

        set_network_enable_spinner = view.findViewById(R.id.net_set_network_enable_spinner);
        network_array = mRes.getStringArray(R.array.network_type);
        mAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_item, network_array);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        set_network_enable_spinner.setAdapter(mAdapter);
        set_network_enable = view.findViewById(R.id.net_set_network_enable_switch);
    }

    private void readData() {
        unInitListener();
        set_wifi_ap.setChecked(wifiApEnable == 1);
        set_network_multi_switch.setChecked(netWorkMultiEnable == 1);
        set_network_enable.setChecked(netWorkEnable == 1);
        initListener();
        if (mSmdtReceiver == null) {
            mSmdtReceiver = new SmdtReceiver();
            mContext.registerReceiver(mSmdtReceiver, mIntentFilter);
        }
    }

    private void initListener() {
        set_wifi_ap.setOnCheckedChangeListener(this);
        set_network_multi_switch.setOnCheckedChangeListener(this);
        set_network_enable.setOnCheckedChangeListener(this);
        set_wifi_spinner.setOnItemSelectedListener(this);
        set_network_enable_spinner.setOnItemSelectedListener(this);
    }

    private void unInitListener() {
        set_wifi_ap.setOnCheckedChangeListener(null);
        set_network_multi_switch.setOnCheckedChangeListener(null);
        set_network_enable.setOnCheckedChangeListener(null);
        set_wifi_spinner.setOnItemSelectedListener(null);
        set_network_enable_spinner.setOnItemSelectedListener(null);
    }

    @Override
    public void onClick(View view) {
        hideInputKeyboard(mContext);
        switch (view.getId()) {
            case R.id.net_set_network_protect:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String packageName = "android.app.smdt.networkprotect";
                String className = "android.app.smdt.networkprotect.MainActivity";
                intent.setClassName(packageName, className);
                try {
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    set_network_protect.setText(RET_API_ERR_NG + "");
                }
                break;
            case R.id.net_set_network_priority:
                showNetWorkPriorityDialog();
                break;
            case R.id.net_set_network:
                if (set_network_eth0.isChecked()) {
                    showNetWorkDialog(TYPE_ETH0);
                } else if (set_network_eth1.isChecked()) {
                    showNetWorkDialog(TYPE_ETH1);
                } else if (set_network_wlan.isChecked()) {
                    showNetWorkDialog(TYPE_WLAN);
                }
                break;
            case R.id.net_set_wifi_connect:
                setWifiConnect();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        hideInputKeyboard(mContext);
        if (!compoundButton.isPressed()) {
            DEBUG("Net isPressed from code or what?:" + compoundButton.isPressed());
        }
        switch (compoundButton.getId()) {
            case R.id.net_set_wifi_ap_switch:
                if (smdtManagerNew.net_setWifiAp(isChecked) != 0) {
                    set_wifi_ap.setChecked(!isChecked);
                }
                break;
            case R.id.net_set_network_enable_switch:
                if (smdtManagerNew.net_setNetWork(network_array[select_network], isChecked) != 0) {
                    set_network_enable.setChecked(!isChecked);
                }
                break;
            case R.id.net_set_network_multi_switch:
                if (smdtManagerNew.net_setNetworkMultiEnable(isChecked) != 0) {
                    set_network_multi_switch.setChecked(!isChecked);
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        hideInputKeyboard(mContext);
        switch (adapterView.getId()) {
            case R.id.net_set_network_enable_spinner:
                select_network = i;
                set_network_enable.setChecked(smdtManagerNew.net_getNetWork(network_array[select_network]) == 1);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            wifiApEnable = smdtManagerNew.net_getWifiAp();
            netWorkMultiEnable = smdtManagerNew.net_getNetworkMultiEnable();
            select_network = set_network_enable_spinner.getSelectedItemPosition();
            netWorkEnable = smdtManagerNew.net_getNetWork(network_array[select_network]);
            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }

    private class NetThread extends Thread {
        @Override
        public void run() {
            networkType = smdtManagerNew.net_getCurrentNetType();
            macEth0 = smdtManagerNew.net_getMacAddress(TYPE_ETH0);
            macEth1 = smdtManagerNew.net_getMacAddress(TYPE_ETH1);
            macWifi = smdtManagerNew.net_getMacAddress(TYPE_WLAN);
            wifiRssi = smdtManagerNew.net_getWifiRssi(4);
            modelEth0 = smdtManagerNew.net_getNetWorkModel(TYPE_ETH0);
            modelEth1 = smdtManagerNew.net_getNetWorkModel(TYPE_ETH1);
            modelWifi = smdtManagerNew.net_getNetWorkModel(TYPE_WLAN);
            imeiStr = smdtManagerNew.net_getImeiNumber();
            iccidStr = smdtManagerNew.net_getIccidNumber();
            imsiStr = smdtManagerNew.net_getImsiNumber();
            if (networkType != null){
                if (networkType.equals(CONNECT_TYPE_ETHERNET)) {
                    mNetworkInfoData = smdtManagerNew.net_getNetWorkInf(TYPE_ETH0);
                    if (TextUtils.isEmpty(mNetworkInfoData.getIp())) {
                        mNetworkInfoData = smdtManagerNew.net_getNetWorkInf(TYPE_ETH1);
                    }
                } else if (networkType.equals(CONNECT_TYPE_WIFI)) {
                    mNetworkInfoData = smdtManagerNew.net_getNetWorkInf(TYPE_WLAN);
                } else if (networkType.equals(CONNECT_TYPE_MOBILE)) {
                    mNetworkInfoData = smdtManagerNew.net_getNetWorkInf(TYPE_MOBILE);
                }else {
                    mNetworkInfoData = null;
                }
            } else {
                mNetworkInfoData = null;
            }
            mHandler.sendEmptyMessage(REFRESH_UI);
        }
    }

    private void updateStatus() {
        get_current_net.setText(networkType);
        get_eth0_macaddress.setText(macEth0);
        get_eth1_macaddress.setText(macEth1);
        get_wlan_macaddress.setText(macWifi);
        get_wlan_rssi.setText("Level:" + wifiRssi);

        String eth0_type, eth1_type, wlan0_type;
        if (modelEth0 == MODE_DHCP) {
            eth0_type = mContext.getResources().getString(R.string.net_status_dhcp);
        } else if (modelEth0 == MODE_STATIC) {
            eth0_type = mContext.getResources().getString(R.string.net_status_static);
        } else {
            eth0_type = String.valueOf(modelEth0);
        }
        get_eth0_network.setText(eth0_type);
        if (modelEth1 == MODE_DHCP) {
            eth1_type = mContext.getResources().getString(R.string.net_status_dhcp);
        } else if (modelEth1 == MODE_STATIC) {
            eth1_type = mContext.getResources().getString(R.string.net_status_static);
        } else {
            eth1_type = String.valueOf(modelEth1);
        }
        get_eth1_network.setText(eth1_type);
        if (modelWifi == MODE_DHCP) {
            wlan0_type = mContext.getResources().getString(R.string.net_status_dhcp);
        } else if (modelWifi == MODE_STATIC) {
            wlan0_type = mContext.getResources().getString(R.string.net_status_static);
        } else {
            wlan0_type = String.valueOf(modelWifi);
        }
        get_wlan_network.setText(wlan0_type);
        if (mNetworkInfoData != null) {
            get_ipaddress.setText(mNetworkInfoData.getIp() + "");
            get_gateway.setText(mNetworkInfoData.getGateway() + "");
            get_netmask.setText(mNetworkInfoData.getNetmask() + "");
            get_dns1.setText(mNetworkInfoData.getDns1() + "");
            get_dns2.setText(mNetworkInfoData.getDns2() + "");
        } else {
            get_ipaddress.setText("");
            get_gateway.setText("");
            get_netmask.setText("");
            get_dns1.setText("");
            get_dns2.setText("");
        }
        imei.setText(imeiStr);
        iccid.setText(iccidStr);
        imsi.setText(imsiStr);
    }

    private void showNetWorkDialog(String type) {
        final String[] entries = mRes.getStringArray(R.array.network_array);
        int current = smdtManagerNew.net_getNetWorkModel(type);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        if (type.equals(TYPE_ETH0)) {
            alertBuilder.setTitle(mContext.getResources().getString(R.string.net_set_network_eth0));
        } else if (type.equals(TYPE_ETH1)) {
            alertBuilder.setTitle(mContext.getResources().getString(R.string.net_set_network_eth1));
        } else if (type.equals(TYPE_WLAN)) {
            alertBuilder.setTitle(mContext.getResources().getString(R.string.net_set_network_wlan));
        }
        alertBuilder.setSingleChoiceItems(entries, current, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int result = smdtManagerNew.net_setNetWorkModel(type, MODE_DHCP, null, null, null, null, null);
                            if (result == 0) {
                                netWorkDialog.dismiss();
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, "Fail! Errorcode :" + result, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();
                } else if (i == 1) {
                    StaticIpDialog mStaticIpDialog = new StaticIpDialog(mContext, smdtManagerNew, type);
                    mStaticIpDialog.setPositiveButtonListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String ipAddr = mStaticIpDialog.getIp();
                            String gateway = mStaticIpDialog.getGateway();
                            String netMask = mStaticIpDialog.getNetmask();
                            String dns1 = mStaticIpDialog.getDns1();
                            String dns2 = mStaticIpDialog.getDns2();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int result = smdtManagerNew.net_setNetWorkModel(type, MODE_STATIC, ipAddr, gateway, netMask, dns1, dns2);
                                    if (result == 0) {
                                        netWorkDialog.dismiss();
                                    } else {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(mContext, "Fail! Errorcode :" + result, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    });
                    mStaticIpDialog.show();
                    netWorkDialog.dismiss();
                }
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                netWorkDialog.dismiss();
            }
        });

        netWorkDialog = alertBuilder.create();
        netWorkDialog.show();
    }

    private void setWifiConnect() {
        String account = set_wifi_account_et.getText().toString();
        if (TextUtils.isEmpty(account)) {
            return;
        }
        String password = set_wifi_pwd_et.getText().toString();
        int type = set_wifi_spinner.getSelectedItemPosition();

        final String[] entries = mRes.getStringArray(R.array.network_array);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mContext.getResources().getString(R.string.net_set_network_wlan));
        alertBuilder.setSingleChoiceItems(entries, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int result = smdtManagerNew.net_setWifiConnect(account, password, type, MODE_DHCP, null);
                            if (result == 0) {
                                netWorkDialog.dismiss();
                            } else {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mContext, "Fail! Errorcode :" + result, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }).start();
                } else if (i == 1) {
                    StaticIpDialog mStaticIpDialog = new StaticIpDialog(mContext, smdtManagerNew, TYPE_WLAN);
                    mStaticIpDialog.setPositiveButtonListener(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NetworkInfoData info = new NetworkInfoData();
                            info.setIp(mStaticIpDialog.getIp());
                            info.setGateway(mStaticIpDialog.getGateway());
                            info.setNetmask(mStaticIpDialog.getNetmask());
                            info.setDns1(mStaticIpDialog.getDns1());
                            info.setDns2(mStaticIpDialog.getDns2());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int result = smdtManagerNew.net_setWifiConnect(account, password, type, MODE_STATIC, info);
                                    if (result == 0) {
                                        netWorkDialog.dismiss();
                                    } else {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(mContext, "Fail! Errorcode :" + result, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    });
                    mStaticIpDialog.show();
                    netWorkDialog.dismiss();
                }
            }
        });

        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                netWorkDialog.dismiss();
            }
        });

        netWorkDialog = alertBuilder.create();
        netWorkDialog.show();
    }

    private void showNetWorkPriorityDialog() {
        networkPriorityList = smdtManagerNew.net_getNetworkPriority();
        if (networkPriorityList == null || networkPriorityList.length == 0) {
            net_set_network_priority_tv.setText("-1");
            return;
        }
        net_set_network_priority_tv.setText("");

        View view = LayoutInflater.from(mContext).inflate(R.layout.network_dialog, null);
        ListView listView = view.findViewById(R.id.list);
        listView.setFadingEdgeLength(0);
        ListAdapter listAdapter = new ListAdapter(mContext);
        listView.setAdapter(listAdapter);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
        alertBuilder.setTitle(mRes.getString(R.string.net_set_network_priority_title));
        alertBuilder.setView(view);
        alertBuilder.setNegativeButton(mRes.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                networkPriorityDialog.dismiss();
            }
        });
        alertBuilder.setPositiveButton(mRes.getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int result = smdtManagerNew.net_setNetworkPriority(networkPriorityList);
                net_set_network_priority_tv.setText(""+result);
                networkPriorityDialog.dismiss();
            }
        });
        networkPriorityDialog = alertBuilder.create();

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            totalHeight = listItem.getMeasuredHeight();
        }
        Rect rectangle = new Rect();
        Window window = networkPriorityDialog.getWindow();
        WindowManager windowManager = getActivity().getWindowManager();
        //屏幕分辨率，获取屏幕宽、高用
        Display display = windowManager.getDefaultDisplay();
        //获取对话框当前的参数值
        WindowManager.LayoutParams p = window.getAttributes();
        //宽度设置为屏幕的0.8
        p.width = (int) (display.getWidth() * 0.8);
        //获取ListView的高度和当前屏幕的0.6进行比较，如果高，就自适应改变
        if (getTotalHeightofListView(listView) > display.getHeight() * 0.6) {
            //得到ListView的参数值
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            //设置ListView的高度是屏幕的一半
            params.height = (int) (display.getHeight() * 0.5);
            //设置
            listView.setLayoutParams(params);
        }
        //设置Dialog的高度
        window.setAttributes(p);

        networkPriorityDialog.show();
    }

    /**
     * 获取ListView的高度
     */
    public int getTotalHeightofListView(ListView list) {
        //ListView的适配器
        ListAdapter mAdapter = (ListAdapter) list.getAdapter();
        if (mAdapter == null) {
            return 0;
        }
        int totalHeight = 0;
        //循环适配器中的每一项
        for (int i = 0; i < mAdapter.getCount(); i++) {
            //得到每项的界面view
            View mView = mAdapter.getView(i, null, list);
            //得到一个view的大小
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            //总共ListView的高度
            totalHeight += mView.getMeasuredHeight();
        }
        return totalHeight;
    }

    public class ListAdapter extends ArrayAdapter<String[]> {
        private final LayoutInflater mInflater;
        private Context context;

        class ViewHolder {
            TextView textView;
            Button btnMoveUp;
            Button btnMoveDown;
        }

        public ListAdapter(Context context) {
            super(context, 0);
            this.context = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            addAll(networkPriorityList);
        }

        @Override
        public int getCount() {
            return networkPriorityList.length;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListAdapter.ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.network_list_content, null);
                // Creates a ViewHolder and store references to the two children
                // views
                // we want to bind data to.
                holder = new ListAdapter.ViewHolder();
                holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
                holder.btnMoveUp = (Button) convertView.findViewById(R.id.btn1);
                holder.btnMoveDown = (Button) convertView.findViewById(R.id.btn2);
                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ListAdapter.ViewHolder) convertView.getTag();
            }
            holder.textView.setText(networkPriorityList[position]);
            holder.btnMoveUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    networkPriorityList = changeListOrder(networkPriorityList, position, true);
                    notifyDataSetChanged();
                }
            });
            holder.btnMoveDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    networkPriorityList = changeListOrder(networkPriorityList, position, false);
                    notifyDataSetChanged();
                }
            });

            if (position == 0) {
                holder.btnMoveUp.setEnabled(false);
                holder.btnMoveUp.setTextColor(mRes.getColor(R.color.colorPrimaryDark));
            } else {
                holder.btnMoveUp.setEnabled(true);
                holder.btnMoveUp.setTextColor(mRes.getColor(R.color.colorAccent));
            }
            if (position == (getCount() - 1)) {
                holder.btnMoveDown.setEnabled(false);
                holder.btnMoveDown.setTextColor(mRes.getColor(R.color.colorPrimaryDark));
            } else {
                holder.btnMoveDown.setEnabled(true);
                holder.btnMoveDown.setTextColor(mRes.getColor(R.color.colorAccent));
            }

            return convertView;
        }
    }

    private String[] changeListOrder(String[] list, int currentIndex, boolean Up) {
        if (currentIndex == 0 && Up || currentIndex == list.length - 1 && !Up) {
            return list;
        }
        int changeIndex = 0;
        if (Up) {
            changeIndex = currentIndex - 1;
        } else {
            changeIndex = currentIndex + 1;
        }
        DEBUG("currentIndex:" + currentIndex + ",changeIndex:" + changeIndex);
        String currentType = list[currentIndex];
        String changeType = list[changeIndex];
        DEBUG("currentType:" + currentType + ",changeType:" + changeType);
        list[changeIndex] = currentType;
        list[currentIndex] = changeType;
        return list;
    }


}
