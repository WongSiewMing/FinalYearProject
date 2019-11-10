package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import Helper.AppointmentOption;
import Helper.Student;
import Helper.TradeOption;

public class Trade_Menu extends Fragment {

    public final static List<TradeOption> arrayTradeOption = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Trade Menu");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.trade_menu, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("TradeStuff");
        populateArrayAppointmentOption();
        registerClickCallBack();
        return view;
    }

    public void populateArrayAppointmentOption() {
        arrayTradeOption.clear();
        arrayTradeOption.add(0, new TradeOption(R.mipmap.icon_add, "Create Trade"));
        arrayTradeOption.add(1, new TradeOption(R.mipmap.icon_my_trade, "Sent Trade Request"));
        arrayTradeOption.add(2, new TradeOption(R.mipmap.icon_trade, "Trading Request"));
        populateListView();
    }

    public void populateListView() {
        ArrayAdapter<TradeOption> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.tradeOptionList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<TradeOption> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templatetrade, arrayTradeOption);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatetrade, parent, false);
            }

            TradeOption currentOption = arrayTradeOption.get(position);

            ImageView optionIcon = (ImageView) itemView.findViewById(R.id.tradeOptionIcon);
            optionIcon.setImageResource(currentOption.getTradeOption());

            TextView optionName = (TextView) itemView.findViewById(R.id.tradeOptionName);
            optionName.setText(currentOption.getTradeOptionName());

            return itemView;

        }
    }

    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.tradeOptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (arrayTradeOption.get(position).getTradeOptionName().equals("Create Trade")) {
                    SearchSeller searchSeller = new SearchSeller();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("createTrade", s);
                    searchSeller.setArguments(bundle);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, searchSeller)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();

                } else if (arrayTradeOption.get(position).getTradeOptionName().equals("Sent Trade Request")) {
                    ManageSentTrade manageSentTrade = new ManageSentTrade();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("manageSentTrade", s);
                    manageSentTrade.setArguments(bundle);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, manageSentTrade)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else if (arrayTradeOption.get(position).getTradeOptionName().equals("Trading Request")) {
                    ManageTradeRequest manageTradeRequest = new ManageTradeRequest();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("manageTradeRequest", s);
                    manageTradeRequest.setArguments(bundle);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, manageTradeRequest)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            }
        });
    }
}
