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

import Helper.Student;
import Helper.SummaryOption;

/*Author : Wong Qing Li
Programme : RSD3
Year : 2019*/


public class SummaryMenu extends Fragment {

    public final static List<SummaryOption> arraySummaryOption = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Summary Report");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.summary_menu,container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("SummaryMenu");
        populateArraySummaryOption();
        registerClickCallBack();
        return view;
    }

    public void populateArraySummaryOption(){
        arraySummaryOption.clear();
        arraySummaryOption.add(0, new SummaryOption(R.mipmap.icon_personal_report, "Personal Summary Report"));
        arraySummaryOption.add(1, new SummaryOption(R.mipmap.icon_overall_report, "Overall Summary Report"));
        populateListView();
    }

    public void populateListView(){
        ArrayAdapter<SummaryOption> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.summaryOptionList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<SummaryOption> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templatesummary, arraySummaryOption);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatesummary, parent, false);
            }

            SummaryOption currentOption = arraySummaryOption.get(position);

            ImageView optionIcon = (ImageView) itemView.findViewById(R.id.summaryOptionIcon);
            optionIcon.setImageResource(currentOption.getSummaryOption());

            TextView optionName = (TextView) itemView.findViewById(R.id.summaryOptionName);
            optionName.setText(currentOption.getSummaryOptionName());

            return itemView;

        }
    }

    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.summaryOptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (arraySummaryOption.get(position).getSummaryOptionName().equals("Personal Summary Report")) {
                    /*
                    ChatRoomList_V2 chatRoomList_v2 = new ChatRoomList_V2();
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("chatRoomList_v2", s);
                    chatRoomList_v2.setArguments(bundle1);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, chatRoomList_v2)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
                       */

                } else if (arraySummaryOption.get(position).getSummaryOptionName().equals("Overall Summary Report")) {
                    /*
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

                     */

                }

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
