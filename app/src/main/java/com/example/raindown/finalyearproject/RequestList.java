package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Requests;
import Helper.Student;


public class RequestList extends Fragment {

    public final static List<Requests> arrayRequestList = new ArrayList<>();
    private static final String TAG = "Requestlist" ;
    View view;
    FragmentManager fragmentManager;
    ProgressDialog pDialog = null;
    ImageView infoIcon;
    TextView notice;
    Student s = null;
    String ID = null;


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
        getActivity().setTitle("Request List");

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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_requestlist, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("RequestStuffList");
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        ID = s.getStudentID();
        Log.d(TAG, "ID" + ID);
        getMyStuffs(getActivity(), Constant.serverFile + "getRequestStuffList.php?studentID=" + ID);
        registerClickCallBack();
        return view;
    }

    public void getMyStuffs(Context context, String url) {
        Log.d(TAG, "PHP run" );
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.toString().equals("[]")){
                            infoIcon.setVisibility(view.VISIBLE);
                            notice.setVisibility(view.VISIBLE);
                        }
                        try {

                            arrayRequestList.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject myStuffResponse = (JSONObject) response.get(i);
                                arrayRequestList.add(new Requests(myStuffResponse.getString("requeststuffID"), new Student(myStuffResponse.getString("studentID"),
                                        myStuffResponse.getString("clientID"),myStuffResponse.getString("photo"),myStuffResponse.getString("studentName"),
                                        myStuffResponse.getString("icNo"),myStuffResponse.getString("studentProgramme"),myStuffResponse.getString("studentFaculty"),
                                        myStuffResponse.getInt("yearOfStudy")), myStuffResponse.getString("stuffName"), myStuffResponse.getString("stuffImage"),
                                        myStuffResponse.getString("stuffDescription"), myStuffResponse.getString("stuffCategory"), myStuffResponse.getString("stuffCondition"),
                                        myStuffResponse.getDouble("stuffPrice"), myStuffResponse.getInt("stuffQuantity"), myStuffResponse.getString("validStartDate"),
                                        myStuffResponse.getString("validEndDate"), myStuffResponse.getString("stuffStatus")));
                            }
                            Log.d(TAG, "PHP corret" );
                            populateListView();
                            if (pDialog.isShowing())
                                pDialog.dismiss();
                        } catch (Exception e) {
                            Log.d(TAG, "PHP ERROR" + e );
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(TAG, "PHP ERROR2" );
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public class MyListAdapter extends ArrayAdapter<Requests> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templaterequeststuff, arrayRequestList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templaterequeststuff, parent, false);
            }
            Requests stuffList = arrayRequestList.get(position);
            ImageView stuffPhoto = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(stuffList.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffPhoto);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(stuffList.getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.stuffCategory);
            stuffCategory.setText(stuffList.getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f", stuffList.getStuffPrice()));

            return itemView;

        }
    }

    public void populateListView() {
        Log.d(TAG, "view ERROR" );
        ArrayAdapter<Requests> adapter = new RequestList.MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.requestList);
        list.setAdapter(adapter);
    }

    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.requestList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Requests clickedStuff = arrayRequestList.get(position);
                RequestStuffDetails frag = new RequestStuffDetails();
                Bundle bundles = new Bundle();
                bundles.putSerializable("ClickedStuff", clickedStuff);
                bundles.putSerializable("StudentClickedStuff", s);
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
