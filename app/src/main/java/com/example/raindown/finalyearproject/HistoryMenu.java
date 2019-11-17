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

import Helper.HistoryOption;
import Helper.Student;

public class HistoryMenu extends Fragment {

    public final static List<HistoryOption> arrayHistoryOption = new ArrayList<>();
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
        getActivity().setTitle("History Menu");
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
        view = inflater.inflate(R.layout.history_menu, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("HistoryMenu");
        populateArrayHistoryOption();
        registerClickCallBack();
        return view;
    }

    public void populateArrayHistoryOption() {
        arrayHistoryOption.clear();
        arrayHistoryOption.add(0, new HistoryOption(R.mipmap.icon_view_history, "Recently Viewed Stuffs"));
        arrayHistoryOption.add(1, new HistoryOption(R.mipmap.icon_search, "Search History"));
        arrayHistoryOption.add(2, new HistoryOption(R.mipmap.icon_trade_history, "Trade History"));
        populateListView();
    }

    public void populateListView() {
        ArrayAdapter<HistoryOption> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.historyOptionList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<HistoryOption> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templatehistory, arrayHistoryOption);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatehistory, parent, false);
            }

            HistoryOption currentOption = arrayHistoryOption.get(position);

            ImageView optionIcon = (ImageView) itemView.findViewById(R.id.historyOptionIcon);
            optionIcon.setImageResource(currentOption.getHistoryOption());

            TextView optionName = (TextView) itemView.findViewById(R.id.historyOptionName);
            optionName.setText(currentOption.getHistoryOptionName());

            return itemView;

        }
    }

    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.historyOptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (arrayHistoryOption.get(position).getHistoryOptionName().equals("Recently Viewed Stuffs")) {
                    ViewHistory viewHistory = new ViewHistory();
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("ViewHistory", s);
                    viewHistory.setArguments(bundle1);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, viewHistory)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();

                } else if (arrayHistoryOption.get(position).getHistoryOptionName().equals("Search History")) {
                    SearchHistory searchHistory = new SearchHistory();
                    Bundle bundle2 = new Bundle();
                    bundle2.putSerializable("SearchHistory", s);
                    searchHistory.setArguments(bundle2);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, searchHistory)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();

                } else if (arrayHistoryOption.get(position).getHistoryOptionName().equals("Trade History")) {
//                    PublicList publicList = new PublicList();
//                    Bundle bundle2 = new Bundle();
//                    bundle2.putSerializable("PublicList", s);
//                    publicList.setArguments(bundle2);
//                    fragmentManager = getFragmentManager();
//                    fragmentManager.beginTransaction()
//                            .replace(R.id.update_fragmentHolder, publicList)
//                            .addToBackStack(null)
//                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                            .commit();
                }

            }
        });
    }
}

