/**
 *   File: MainActivity.java
 *   Author: Xu Linrui <lrxu@smdt.com.cn>
 *   Created on 17 June 2021
 **/

package android.app.smdt.apidemo;

import android.app.smdt.apidemo.fragment.CustomFragment;
import android.app.smdt.apidemo.fragment.DeviceFragment;
import android.app.smdt.apidemo.fragment.DisplayFragment;
import android.app.smdt.apidemo.fragment.HardwareFragment;
import android.app.smdt.apidemo.fragment.MainFragment;
import android.app.smdt.apidemo.fragment.NetFragment;
import android.app.smdt.apidemo.fragment.SysFragment;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    //view
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Fragment mainFragment;
    private Fragment deviceFragment;
    private Fragment displayFragment;
    private Fragment netFragment;
    private Fragment sysFragment;
    private Fragment hardwareFragment;
    private Fragment customFragment;
    private String[] tabTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewAndRes();
        initFragment();
    }

    private void initViewAndRes(){
        tabTitle = getResources().getStringArray(R.array.tab_title);
        tabLayout = (TabLayout) findViewById(R.id.toolbar_tab);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
    }

    private void initFragment(){
        List<Fragment> fragments = new ArrayList<>();
        mainFragment = new MainFragment();
        fragments.add(mainFragment);
        deviceFragment = new DeviceFragment();
        fragments.add(deviceFragment);
        displayFragment = new DisplayFragment();
        fragments.add(displayFragment);
        netFragment = new NetFragment();
        fragments.add(netFragment);
        sysFragment = new SysFragment();
        fragments.add(sysFragment);
        hardwareFragment = new HardwareFragment();
        fragments.add(hardwareFragment);
        customFragment = new CustomFragment();
        fragments.add(customFragment);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments, tabTitle);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1); //预加载页面数量,最小1
        tabLayout.setupWithViewPager(viewPager);
    }

}
