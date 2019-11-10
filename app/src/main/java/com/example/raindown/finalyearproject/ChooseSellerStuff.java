package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ChooseSellerStuff extends Fragment {

    View view;
    private Student myID;
    private String sellerID, sellerStuffCommand = "";
    private ImageView sellerPhoto, infoIcon;
    private TextView sellerName, sellerFaculty, sellerProgramme, sellerYear, notice;
    private List<Student> sellerInfo = new ArrayList<>();
    FragmentManager fragmentManager;
    ProgressDialog pDialog = null;
    JSONObject jsonObj;
    List<Stuff> sellerDetailsList = new ArrayList<>();

    private static final String TAG = "Seller";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        myID = (Student) bundle.getSerializable("UserData");
        sellerID = bundle.getString("ClickedUserID");
        sellerStuffCommand = (String) bundle.getSerializable("SellerStuff");
        getActivity().setTitle("Select Trade Stuff");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.choose_seller_stuff, container, false);
        sellerPhoto = view.findViewById(R.id.sellerPhoto);
        sellerName = view.findViewById(R.id.sellerName);
        sellerFaculty = view.findViewById(R.id.sellerFaculty);
        sellerProgramme = view.findViewById(R.id.sellerProgramme);
        sellerYear = view.findViewById(R.id.sellerYear);

        infoIcon = (ImageView) view.findViewById(R.id.tradeInfoIcon);
        notice = (TextView) view.findViewById(R.id.tradeNotice);

        getSellerInfo();
        registerClickCallBack();
        return view;
    }

    private void getSellerInfo() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSellerData.php?username=" + sellerID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {

                                try {
                                    sellerInfo.clear();
                                    JSONObject infoResponse = (JSONObject) response.get(0);
                                    sellerInfo.add(new Student(infoResponse.getString("studentID"),
                                            infoResponse.getString("photo"),
                                            infoResponse.getString("studentName"),
                                            infoResponse.getString("icNo"),
                                            infoResponse.getString("studentProgramme"),
                                            infoResponse.getString("studentFaculty"),
                                            infoResponse.getInt("yearOfStudy")));


                                    if (infoResponse.length() == 0) {
                                        Toast.makeText(getActivity().getApplication(), "Error retrieve user data...",
                                                Toast.LENGTH_LONG).show();

                                        getFragmentManager().popBackStack();
                                    } else {
                                        populateSellerInfo();
                                    }
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

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        getSellerDetails();
    }

    public void getSellerDetails(){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());

                jsonObj = new JSONObject(sellerStuffCommand);
                if(jsonObj.getString("command").equals("30303530300B")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSellerDetails.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if(response.toString().equals("[]")){
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                    }
                                    try {
                                        sellerDetailsList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject sellerResponse = (JSONObject) response.get(i);
                                            sellerDetailsList.add(new Stuff(sellerResponse.getString("stuffID"), new Student(sellerResponse.getString("studentID"),
                                                    sellerResponse.getString("clientID"), sellerResponse.getString("photo"), sellerResponse.getString("studentName"),
                                                    sellerResponse.getString("icNo"), sellerResponse.getString("studentProgramme"), sellerResponse.getString("studentFaculty"),
                                                    sellerResponse.getInt("yearOfStudy")), sellerResponse.getString("stuffName"), sellerResponse.getString("stuffImage"),
                                                    sellerResponse.getString("stuffDescription"), sellerResponse.getString("stuffCategory"), sellerResponse.getString("stuffCondition"),
                                                    sellerResponse.getDouble("stuffPrice"), sellerResponse.getInt("stuffQuantity"), sellerResponse.getString("validStartDate"),
                                                    sellerResponse.getString("validEndDate"), sellerResponse.getString("stuffStatus")));

                                        }
                                        populateListView();
                                    } catch (Exception e) {

                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {

                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                }
                            });
                    queue.add(jsonObjectRequest);
                }

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void populateSellerInfo(){

        Picasso.with(getActivity()).load(sellerInfo.get(0).getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(sellerPhoto);
        sellerName.setText(sellerInfo.get(0).getStudentName());
        sellerFaculty.setText(sellerInfo.get(0).getStudentFaculty());
        sellerProgramme.setText(sellerInfo.get(0).getStudentProgramme());
        sellerYear.setText(sellerInfo.get(0).getYearOfStudy());
    }

    public void populateListView() {
        ArrayAdapter<Stuff> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.sellerTradeList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<Stuff> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatetradestuff, sellerDetailsList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatetradestuff, parent, false);
            }
            Stuff currentSellerStuffs = sellerDetailsList.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.tradeStuffImage);
            Picasso.with(getActivity()).load(currentSellerStuffs.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.tradeStuffName);
            stuffName.setText(currentSellerStuffs.getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.tradeStuffCategory);
            stuffCategory.setText(currentSellerStuffs.getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.tradeStuffPrice);
            stuffPrice.setText(String.format("RM %.2f", currentSellerStuffs.getStuffPrice()));

            return itemView;

        }
    }

    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.sellerTradeList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Stuff clickedStuff = sellerDetailsList.get(position);
                ChooseOfferStuff frag = new ChooseOfferStuff();
                Bundle bundles = new Bundle();
                bundles.putSerializable("ClickedStuff", clickedStuff);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }
}
