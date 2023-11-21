/**
 *   File: StaticIpDialog.java
 *   Author: Xu Linrui <lrxu@smdt.com.cn>
 *   Created on 14 July 2021
 **/
package android.app.smdt.apidemo.ui;

import android.app.AlertDialog;
import android.app.smdt.NetworkInfoData;
import android.app.smdt.SmdtManagerNew;
import android.app.smdt.apidemo.R;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.regex.Pattern;

public class StaticIpDialog extends AlertDialog implements TextWatcher,DialogInterface.OnClickListener {

    public EditText et_ip_address;
    public EditText et_gateway;
    public EditText et_netmask;
    public EditText et_dns1;
    public EditText et_dns2;

    static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
    static final int BUTTON_CANCEL = DialogInterface.BUTTON_NEUTRAL;

    private final static String nullIpInfo = "0.0.0.0";

    private View mView;
    private Context mContext;
    private SmdtManagerNew mSmdtManagerNew;
    private String mType;

    public StaticIpDialog(Context context, SmdtManagerNew smdtManagerNew, String type) {
        // TODO Auto-generated constructor stub
        super(context);
        mContext = context;
        mSmdtManagerNew = smdtManagerNew;
        mType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.static_ip_dialog, null);
        setView(mView);
        setInverseBackgroundForced(true);

        et_ip_address = mView.findViewById(R.id.ipaddress);
        et_gateway = mView.findViewById(R.id.gateway);
        et_netmask = mView.findViewById(R.id.netmask);
        et_dns1 = mView.findViewById(R.id.dns1);
        et_dns2 =  mView.findViewById(R.id.dns2);
        et_ip_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkIPValue();
                checkValue();
            }
        });
        et_gateway.addTextChangedListener(this);
        et_netmask.addTextChangedListener(this);
        et_dns1.addTextChangedListener(this);
        et_dns2.addTextChangedListener(this);
        setButton(BUTTON_NEGATIVE, mContext.getString(R.string.cancel), this);
        setTitle(mContext.getString(R.string.net_status_static));

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateView();
        checkValue();
    }

    public void setPositiveButtonListener(OnClickListener listener){
        setButton(BUTTON_SUBMIT, mContext.getString(R.string.ok), listener);
    }

    public String getIp(){
        return et_ip_address.getText().toString();
    }

    public String getGateway(){
        return et_gateway.getText().toString();
    }

    public String getNetmask(){
        return et_netmask.getText().toString();
    }

    public String getDns1(){
        return et_dns1.getText().toString();
    }

    public String getDns2(){
        return et_dns2.getText().toString();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == BUTTON_CANCEL){
            dismiss();
        }
    }

    /*
     * 返回 指定的 String 是否是 有效的 IP 地址.
     */
    private boolean isValidIpAddress(String value) {
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;

        while (start < value.length()) {

            if (-1 == end) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    Log.w("EthernetIP",
                            "isValidIpAddress() : invalid 'block', block = "
                                    + block);
                    return false;
                }
            } catch (NumberFormatException e) {
                Log.w("EthernetIP", "isValidIpAddress() : e = " + e);
                return false;
            }

            numBlocks++;

            start = end + 1;
            end = value.indexOf('.', start);
        }
        return numBlocks == 4;
    }

    private void updateView(){
        NetworkInfoData mNetworkInfoData = mSmdtManagerNew.net_getNetWorkInf(mType);
        if(mNetworkInfoData != null) {
            et_ip_address.setText(mNetworkInfoData.getIp() + "");
            et_gateway.setText(mNetworkInfoData.getGateway() + "");
            et_netmask.setText(mNetworkInfoData.getNetmask() + "");
            et_dns1.setText(mNetworkInfoData.getDns1() + "");
            et_dns2.setText(mNetworkInfoData.getDns2() + "");
        }
    }

    public void checkIPValue() {
        String ipAddr = et_ip_address.getText().toString();
        String gateway = et_gateway.getText().toString();
        String netMask = et_netmask.getText().toString();
        String dns1 = et_dns1.getText().toString();
        String dns2 = et_dns2.getText().toString();
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$"); /*check subnet mask*/
        if (isValidIpAddress(ipAddr)) {
            if(TextUtils.isEmpty(gateway)) {
                et_gateway.setText(et_gateway.getHint());
            }
            if(TextUtils.isEmpty(netMask)) {
                et_netmask.setText(et_netmask.getHint());
            }
            if(TextUtils.isEmpty(dns1)) {
                et_dns1.setText(et_dns1.getHint());
            }
            if(TextUtils.isEmpty(dns2)) {
                et_dns2.setText(et_dns2.getHint());
            }
        }
    }

    public void checkValue() {
        boolean enable = false;
        String ipAddr = et_ip_address.getText().toString();
        String gateway = et_gateway.getText().toString();
        String netMask = et_netmask.getText().toString();
        String dns1 = et_dns1.getText().toString();
        String dns2 = et_dns2.getText().toString();
        Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$"); /*check subnet mask*/
        if (isValidIpAddress(ipAddr) && isValidIpAddress(gateway)
                && isValidIpAddress(dns1) && (pattern.matcher(netMask).matches())) {
            if (TextUtils.isEmpty(dns2)) { // 为空可以不考虑
                enable = true;
            } else {
                if (isValidIpAddress(dns2)) {
                    enable = true;
                } else {
                    enable = false;
                }
            }
        } else {
            enable = false;
        }
        getButton(BUTTON_SUBMIT).setEnabled(enable);
    }

    @Override
    public void afterTextChanged(Editable s) {
        checkValue();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // work done in afterTextChanged
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // work done in afterTextChanged
    }

}
