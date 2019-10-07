package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Home extends Fragment {

    public final static List<HomeOption> arrayHomeOption = new ArrayList<>();
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
        getActivity().setTitle("Home");
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
        view = inflater.inflate(R.layout.home, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("Home");
        populateArrayHomeOption();
        registerClickCallBack();
        return view;
    }


    public void populateArrayHomeOption() {
        arrayHomeOption.clear();
        arrayHomeOption.add(0, new HomeOption(R.mipmap.icon_sell, "Sell Stuffs"));
        arrayHomeOption.add(1, new HomeOption(R.mipmap.icon_buy, "Buy Stuffs"));
        arrayHomeOption.add(2, new HomeOption(R.mipmap.icon_search, "Search Stuffs"));
        arrayHomeOption.add(3, new HomeOption(R.mipmap.icon_request, "Request Stuffs List"));
        arrayHomeOption.add(4,new HomeOption(R.mipmap.icon_trading, "Trade Stuffs"));
        populateListView();
    }


    public void populateListView() {
        ArrayAdapter<HomeOption> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.homeOptionList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<HomeOption> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templatehome, arrayHomeOption);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatehome, parent, false);
            }
            HomeOption currentOption = arrayHomeOption.get(position);

            ImageView optionIcon = (ImageView) itemView.findViewById(R.id.homeOptionIcon);
            optionIcon.setImageResource(currentOption.getHomeOption());

            TextView optionName = (TextView) itemView.findViewById(R.id.homeOptionName);
            optionName.setText(currentOption.getHomeOptionName());

            return itemView;

        }
    }


    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.homeOptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (arrayHomeOption.get(position).getHomeOptionName().equals("Sell Stuffs")) {
                    SellStuff frag = new SellStuff();
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("SellStuff", s);
                    frag.setArguments(bundle1);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else if (arrayHomeOption.get(position).getHomeOptionName().equals("Buy Stuffs")) {
                    BuyStuff frag = new BuyStuff();
                    Bundle bundle2 = new Bundle();
                    bundle2.putSerializable("BuyStuff", s);
                    frag.setArguments(bundle2);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else if (arrayHomeOption.get(position).getHomeOptionName().equals("Search Stuffs")) {
                    SearchStuff frag = new SearchStuff();
                    Bundle bundle3 = new Bundle();
                    bundle3.putSerializable("SearchStuff", s);
                    frag.setArguments(bundle3);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }else if (arrayHomeOption.get(position).getHomeOptionName().equals("Request Stuffs List")) {
                    RequestList  frag = new RequestList();
                    Bundle bundle3 = new Bundle();
                    bundle3.putSerializable("RequestStuffList", s);
                    frag.setArguments(bundle3);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            }
        });
    }
}
