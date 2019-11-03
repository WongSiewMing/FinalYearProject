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

public class Appointment_Menu extends Fragment {

    public final static List<AppointmentOption> arrayAppointmentOption = new ArrayList<>();
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
        getActivity().setTitle("Appointment Menu");
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
        view = inflater.inflate(R.layout.appointment_menu, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("AppointmentMenu");
        populateArrayAppointmentOption();
        registerClickCallBack();
        return view;
    }

    public void populateArrayAppointmentOption() {
        arrayAppointmentOption.clear();
        arrayAppointmentOption.add(0, new AppointmentOption(R.mipmap.icon_appoinment_request, "Appointment Request"));
        arrayAppointmentOption.add(1, new AppointmentOption(R.mipmap.icon_appointment, "My Appointment"));
        populateListView();
    }

    public void populateListView() {
        ArrayAdapter<AppointmentOption> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.appointmentOptionList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<AppointmentOption> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templateappointment, arrayAppointmentOption);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templateappointment, parent, false);
            }

            AppointmentOption currentOption = arrayAppointmentOption.get(position);

            ImageView optionIcon = (ImageView) itemView.findViewById(R.id.appointmentOptionIcon);
            optionIcon.setImageResource(currentOption.getAppointmentOption());

            TextView optionName = (TextView) itemView.findViewById(R.id.appointmentOptionName);
            optionName.setText(currentOption.getAppointmentOptionName());

            return itemView;

        }
    }

    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.appointmentOptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (arrayAppointmentOption.get(position).getAppointmentOptionName().equals("Appointment Request")) {
                    ManageAppointment manageAppointment = new ManageAppointment();
                    Bundle bundle8 = new Bundle();
                    bundle8.putSerializable("manageAppointment", s);
                    manageAppointment.setArguments(bundle8);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, manageAppointment)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();

                } else if (arrayAppointmentOption.get(position).getAppointmentOptionName().equals("My Appointment")) {
                    ManageRequest manageRequest = new ManageRequest();
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, manageRequest)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            }
        });
    }
}
