package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.squareup.picasso.*;
import java.util.List;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class SearchResult extends Fragment {

    List<Stuff> searchStuff = SearchStuff.searchStuffList;
    FragmentManager fragmentManager;
    View view;
    String categoryTitle = null;
    Student s = null;
    ProgressDialog pDialog = null;
    ImageView infoIcon;
    TextView notice;

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
        Bundle bundle = getArguments();
        categoryTitle = (String) bundle.getSerializable("SearchStuffCategoryTitle");
        getActivity().setTitle(categoryTitle);
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

        view = inflater.inflate(R.layout.searchresult, container, false);
        pDialog = new ProgressDialog(getActivity());
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        if(searchStuff.size() == 0){
            infoIcon.setVisibility(view.VISIBLE);
            notice.setVisibility(view.VISIBLE);
        }
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("SearchStuff");
        populateListView();
        registerClickCallBack();
        return view;
    }

    public void populateListView() {
        ArrayAdapter<Stuff> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.categoryTypeList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Stuff> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatecategorytype, searchStuff);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatecategorytype, parent, false);
            }
            Stuff currentCategoryType = searchStuff.get(position);

            ImageView stuffPhoto = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(currentCategoryType.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffPhoto);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(currentCategoryType.getStuffName());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f", currentCategoryType.getStuffPrice()));

            return itemView;

        }
    }


    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.categoryTypeList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Stuff clickedStuff = searchStuff.get(position);
                StuffDetails frag = new StuffDetails();
                Bundle bundles = new Bundle();
                bundles.putSerializable("ClickedStuff", clickedStuff);
                bundles.putSerializable("StudentClickedStuff", s); //me
                frag.setArguments(bundles);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();


            }
        });
    }


}
