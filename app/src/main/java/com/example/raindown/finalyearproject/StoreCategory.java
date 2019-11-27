package com.example.raindown.finalyearproject;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import Helper.StoreOption;

/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

public class StoreCategory extends Fragment {


    List<StoreOption> lstStoreOption;
    View view;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Store");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_store_category, container, false);
        lstStoreOption = new ArrayList<>();
        lstStoreOption.add(new StoreOption(R.drawable.foodstore, "Food"));
        lstStoreOption.add(new StoreOption(R.drawable.grocery,"Grocery"));
        lstStoreOption.add(new StoreOption(R.drawable.stationary, "Stationary"));
        lstStoreOption.add(new StoreOption(R.drawable.stall, "Event Stall"));

        RecyclerView storerv = (RecyclerView) view.findViewById(R.id.storeCategoryrv);
        StoreRecyclerViewAdapter storeAdapter = new StoreRecyclerViewAdapter(getActivity(), lstStoreOption);
        storerv.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        storerv.setAdapter(storeAdapter);

        return view;
    }

    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
