package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import Helper.MenuOption;
import Helper.Student;


public class MainMenu extends Fragment {
    private List<MenuOption> lstMenu;
    private FragmentManager fragmentManager;
    private static final String TAG = "MainMenu";
    private View view;
    private Student s;
    private RecyclerView menurv;
    private MenuRecycleViewAdapter menuAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Main Menu");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("MainMenu");
        Log.d(TAG, "Student ID = " +s.getStudentID());
        menurv = view.findViewById(R.id.menurv);


        lstMenu = new ArrayList<>();
        lstMenu.add(new MenuOption(R.drawable.ic_home, "Home"));
        lstMenu.add(new MenuOption(R.drawable.ic_profile, "Profile"));
        lstMenu.add(new MenuOption(R.drawable.ic_favorite, "Favorite"));
        lstMenu.add(new MenuOption(R.drawable.ic_follow, "Following"));
        lstMenu.add(new MenuOption(R.drawable.ic_privatechat, "Chat Room"));
        lstMenu.add(new MenuOption(R.drawable.ic_groupchat, "Public Chat"));
        lstMenu.add(new MenuOption(R.drawable.ic_useractivity, "User Activity"));
        lstMenu.add(new MenuOption(R.drawable.ic_appointment, "Appointment"));
        lstMenu.add(new MenuOption(R.drawable.ic_request, "Request"));
        lstMenu.add(new MenuOption(R.drawable.ic_store, "Store"));
        lstMenu.add(new MenuOption(R.drawable.ic_feedback, "Feedback"));
        //lstMenu.add(new MenuOption(R.drawable.ic_about, "About"));
        //lstMenu.add(new MenuOption(R.drawable.ic_groupchat, "Public Group"));


        populateMenulist();
        return view;
    }

    private void populateMenulist() {
        menuAdapter = new MenuRecycleViewAdapter(getActivity(),lstMenu, s);
        menurv.setLayoutManager(new GridLayoutManager(getActivity(),3));
        menurv.setAdapter(menuAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
