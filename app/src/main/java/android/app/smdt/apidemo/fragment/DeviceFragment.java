/**
 * File: DeviceFragment.java
 * Author: Xu Linrui <lrxu@smdt.com.cn>
 * Created on 21 June 2021
 **/
package android.app.smdt.apidemo.fragment;

import android.app.smdt.apidemo.R;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;

public class DeviceFragment extends BaseFragment {

    //data
    private String boardStr;
    private String serialNumber;
    private String androidVer;
    private String hardwareVer;
    private String kernelVer;
    private String mcuVer;
    private String securityVer;
    private int faceSupport;
    private String npuVer;
    private String totalMemoryStr;
    private String availMemoryStr;
    private String totalStorageStr;
    private String availStorageStr;
    private String appUsedStr;
    private String modelStr;
    private String companyStr;
    private String softwareVer;
    private String webViewVer;
    private String imeiStr;
    private String iccidStr;
    private String imsiStr;
    //view
    private TextView boardType, serial, androidVersion, hardwareVersion, kernelVersion, mcuVersion, securityPatch, faceDetect, npuVersion,
            model, company, softwareVersion, webViewVersion,
            totalMemory, availMemory, totalStorage, availStorage, cpuTemperature, cpuFrequency, cpuUsage, appUsed, devTemperature;
    //Thread
    private boolean cputemstart = false;
    private CpuTemThread mCpuTemThread;
    private boolean cpufrestart = false;
    private CpuFreThread mCpuFreThread;
    private boolean cpustart = false;
    private CpuThread mCpuThread;
    private boolean devtemstart = false;
    private DevTemThread mDevTemThread;
    //Handler
    private static final int REFRESH_DATA = 0x00;
    private static final int REFRESH_CPU_TEMP = 0x01;
    private static final int REFRESH_CPU_FRE = 0x02;
    private static final int REFRESH_CPU = 0x03;
    private static final int REFRESH_DEV = 0x04;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == REFRESH_DATA) {
                readData();
            } else if (msg.what == REFRESH_CPU_TEMP) {
                Bundle bundle = msg.getData();
                String cputem = bundle.getString("cputem");
                cpuTemperature.setText(cputem);
            } else if (msg.what == REFRESH_CPU_FRE) {
                Bundle bundle = msg.getData();
                String cpufre = bundle.getString("cpufre");
                cpuFrequency.setText(cpufre);
            } else if (msg.what == REFRESH_CPU) {
                Bundle bundle = msg.getData();
                String cpuuge = bundle.getString("cpuuge");
                cpuUsage.setText(cpuuge);
            } else if (msg.what == REFRESH_DEV) {
                Bundle bundle = msg.getData();
                String devtem = bundle.getString("devtem");
                devTemperature.setText(devtem);
            }
        }
    };

    @Override
    public int getLayoutResource() {
        return R.layout.device_fragment;
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
        cputemstart = true;
        mCpuTemThread = new CpuTemThread();
        mCpuTemThread.start();
        cpufrestart = true;
        mCpuFreThread = new CpuFreThread();
        mCpuFreThread.start();
        cpustart = true;
        mCpuThread = new CpuThread();
        mCpuThread.start();
        devtemstart = true;
        mDevTemThread = new DevTemThread();
        mDevTemThread.start();
    }

    @Override
    public void onUserPause() {
        super.onUserPause();
        stopAndClean();
    }

    private void initView() {
        boardType = view.findViewById(R.id.board_type);
        serial = view.findViewById(R.id.serial);
        androidVersion = view.findViewById(R.id.android_version);
        hardwareVersion = view.findViewById(R.id.hardware_version);
        kernelVersion = view.findViewById(R.id.kernel_version);
        mcuVersion = view.findViewById(R.id.mcu_version);
        securityPatch = view.findViewById(R.id.security_patch);
        faceDetect = view.findViewById(R.id.face_detect);
        npuVersion = view.findViewById(R.id.npu_version);
        totalMemory = view.findViewById(R.id.total_memory);
        availMemory = view.findViewById(R.id.avail_memory);
        totalStorage = view.findViewById(R.id.total_storage);
        availStorage = view.findViewById(R.id.avail_storage);
        cpuTemperature = view.findViewById(R.id.cpu_temperature);
        cpuFrequency = view.findViewById(R.id.cpu_frequency);
        cpuUsage = view.findViewById(R.id.cpu_usage);
        appUsed = view.findViewById(R.id.app_used);
        devTemperature = view.findViewById(R.id.dev_temperature);
        webViewVersion = view.findViewById(R.id.web_view_version);
        model = view.findViewById(R.id.model);
        company = view.findViewById(R.id.company);
        softwareVersion = view.findViewById(R.id.software_version);
    }

    private void readData() {
        boardType.setText(boardStr);
        serial.setText(serialNumber);
        androidVersion.setText(androidVer);
        hardwareVersion.setText(hardwareVer);
        kernelVersion.setText(kernelVer);
        mcuVersion.setText(mcuVer);
        securityPatch.setText(securityVer);
        if (faceSupport == 1) {
            faceDetect.setText(mRes.getString(R.string.support));
        } else {
            faceDetect.setText(mRes.getString(R.string.no_support));
        }
        npuVersion.setText(npuVer);
        webViewVersion.setText(webViewVer);
        totalMemory.setText(totalMemoryStr);
        availMemory.setText(availMemoryStr);
        totalStorage.setText(totalStorageStr);
        availStorage.setText(availStorageStr);
        appUsed.setText(appUsedStr + "MB");
        model.setText(modelStr);
        company.setText(companyStr);
        softwareVersion.setText(softwareVer);
    }

    private void stopAndClean() {
        cputemstart = false;
        if (mCpuTemThread != null) {
            mCpuTemThread.interrupt();
            mCpuTemThread = null;
        }
        cpufrestart = false;
        if (mCpuFreThread != null) {
            mCpuFreThread.interrupt();
            mCpuFreThread = null;
        }
        cpustart = false;
        if (mCpuThread != null) {
            mCpuThread.interrupt();
            mCpuThread = null;
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            boardStr = smdtManagerNew.info_getBoardType();
            serialNumber = smdtManagerNew.info_getSerialNumber();
            androidVer = smdtManagerNew.info_getAndroidVersion();
            hardwareVer = smdtManagerNew.info_getHardwareVersion();
            kernelVer = smdtManagerNew.info_getKernelVersion();
            mcuVer = smdtManagerNew.info_getMCUVersion();
            securityVer = smdtManagerNew.info_getSecurityVersion();
            faceSupport = smdtManagerNew.info_getFaceDetectSupport();
            npuVer = smdtManagerNew.info_getNPUVersion();
            totalMemoryStr = smdtManagerNew.info_getTotalMemory();
            availMemoryStr = smdtManagerNew.info_getAvailMemory();
            totalStorageStr = smdtManagerNew.info_getTotalStorage();
            availStorageStr = smdtManagerNew.info_getAvailStorage();
            appUsedStr = smdtManagerNew.info_getAppUsedMemory(android.os.Process.myPid());
            modelStr = smdtManagerNew.info_getModel();
            companyStr = smdtManagerNew.info_getFactoryCompany();
            softwareVer = smdtManagerNew.info_getSoftwareVersion();
            webViewVer = smdtManagerNew.info_getWebViewVersion();

            mHandler.sendEmptyMessage(REFRESH_DATA);
        }
    }

    private class CpuTemThread extends Thread {
        @Override
        public void run() {
            String cputem;
            while (cputemstart) {
                cputem = smdtManagerNew.info_getCpuTemperature();
                if (TextUtils.isEmpty(cputem)) {
                    cputem = "";
                    cputemstart = false;
                } else {
                    cputem = cputem + "\u2103";
                }
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("cputem", cputem);
                message.setData(bundle);
                message.what = REFRESH_CPU_TEMP;
                mHandler.sendMessage(message);

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CpuFreThread extends Thread {
        @Override
        public void run() {
            String cpufre;
            while (cpufrestart) {
                cpufre = smdtManagerNew.info_getCpuFrequency();
                if (TextUtils.isEmpty(cpufre)) {
                    cpufre = "";
                    cpufrestart = false;
                }
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("cpufre", cpufre);
                message.setData(bundle);
                message.what = REFRESH_CPU_FRE;
                mHandler.sendMessage(message);

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CpuThread extends Thread {
        @Override
        public void run() {
            String cpuuge;
            while (cpustart) {
                cpuuge = smdtManagerNew.info_getCpuUsage();
                if (TextUtils.isEmpty(cpuuge)) {
                    cpuuge = "";
                    cputemstart = false;
                } else {
                    cpuuge = cpuuge + "\u2105";
                }
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("cpuuge", cpuuge);
                message.setData(bundle);
                message.what = REFRESH_CPU;
                mHandler.sendMessage(message);

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class DevTemThread extends Thread {
        @Override
        public void run() {
            String devtemStr;
            while (devtemstart) {
                float devtem = smdtManagerNew.info_getDeviceTemperature();
                if (devtem < 0) {
                    devtemStr = "";
                    devtemstart = false;
                } else {
                    devtemStr = devtem + "\u2103";
                }
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("devtem", devtemStr);
                message.setData(bundle);
                message.what = REFRESH_DEV;
                mHandler.sendMessage(message);

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
