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
import Helper.Conversion;
import Helper.Student;
import Helper.Stuff;

public class ChooseOfferStuff extends Fragment {

    View view;
    private Student myID;
    private Stuff requestStuff = null;
    private ImageView buyerPhoto, infoIcon;
    private TextView buyerName, buyerFaculty, buyerProgramme, buyerYear, notice;
    public final static List<Stuff> arrayMyStuff = new ArrayList<>();
    FragmentManager fragmentManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        myID = (Student) bundle.getSerializable("MyInfo");
        requestStuff = (Stuff) bundle.getSerializable("ClickedStuff");
        getActivity().setTitle("Select Offer Stuff");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.choose_offer_stuff, container, false);
        buyerPhoto = view.findViewById(R.id.buyerPhoto);
        buyerName = view.findViewById(R.id.buyerName);
        buyerFaculty = view.findViewById(R.id.buyerFaculty);
        buyerProgramme = view.findViewById(R.id.buyerProgramme);
        buyerYear = view.findViewById(R.id.buyerYear);

        infoIcon = (ImageView) view.findViewById(R.id.buyerInfoIcon);
        notice = (TextView) view.findViewById(R.id.buyerNotice);

        Picasso.with(getActivity()).load(myID.getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(buyerPhoto);
        buyerName.setText(myID.getStudentName());
        buyerFaculty.setText(myID.getStudentFaculty());
        buyerProgramme.setText(myID.getStudentProgramme());
        buyerYear.setText("Year " + myID.getYearOfStudy());
        getMyStuffs(getActivity(), Constant.serverFile + "getMyStuffs.php?studentID=" + myID.getStudentID());

        registerClickCallBack();
        return view;
    }

    public void getMyStuffs(Context context, String url) {

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
                            arrayMyStuff.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject myStuffResponse = (JSONObject) response.get(i);
                                arrayMyStuff.add(new Stuff(myStuffResponse.getString("stuffID"), new Student(myStuffResponse.getString("studentID"),
                                        myStuffResponse.getString("clientID"),myStuffResponse.getString("photo"),myStuffResponse.getString("studentName"),
                                        myStuffResponse.getString("icNo"),myStuffResponse.getString("studentProgramme"),myStuffResponse.getString("studentFaculty"),
                                        myStuffResponse.getInt("yearOfStudy")), myStuffResponse.getString("stuffName"), myStuffResponse.getString("stuffImage"),
                                        myStuffResponse.getString("stuffDescription"), myStuffResponse.getString("stuffCategory"), myStuffResponse.getString("stuffCondition"),
                                        myStuffResponse.getDouble("stuffPrice"), myStuffResponse.getInt("stuffQuantity"), myStuffResponse.getString("validStartDate"),
                                        myStuffResponse.getString("validEndDate"), myStuffResponse.getString("stuffStatus")));
                            }
                            populateListView();

                        } catch (Exception e) {
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
        queue.add(jsonObjectRequest);
    }

    public void populateListView() {

        ArrayAdapter<Stuff> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.buyerOfferList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<Stuff> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatebuystuff, arrayMyStuff);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatetradestuff, parent, false);
            }
            Stuff currentStuff = arrayMyStuff.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.tradeStuffImage);
            Picasso.with(getActivity()).load(currentStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.tradeStuffName);
            stuffName.setText(currentStuff.getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.tradeStuffCategory);
            stuffCategory.setText(currentStuff.getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.tradeStuffPrice);
            stuffPrice.setText(String.format("RM %.2f",currentStuff.getStuffPrice()));

            return itemView;

        }
    }

    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.buyerOfferList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Stuff offerStuff = arrayMyStuff.get(position);
                TradeConfirm frag = new TradeConfirm();
                Bundle bundles = new Bundle();
                bundles.putSerializable("RequestStuff", requestStuff);
                bundles.putSerializable("OfferStuff", offerStuff);
                bundles.putSerializable("MyInfo", myID);
                frag.setArguments(bundles);
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });
    }

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
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }
}
