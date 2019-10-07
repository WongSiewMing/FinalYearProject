package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/


import android.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopFragment extends Fragment {

    private TextView thisText;
    private TabLayout tabs;


    public ShopFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        //setupViewPager(viewPager);


        tabs = (TabLayout) view.findViewById(R.id.result_tabs);
        thisText = (TextView) view.findViewById(R.id.testLayout);


        tabs.addTab(tabs.newTab().setText("Campus"));
        tabs.addTab(tabs.newTab().setText("OutSide Campus"));

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));




        //tabs.setupWithViewPager(viewPager);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() == "Campus"){
                    thisText.setText("Campus");
                }else if (tab.getText() == "OutSide Campus"){
                    thisText.setText("THIS IS TAB 2");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }





}
