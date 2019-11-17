package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.SearchHistoryOB;
import Helper.Student;
import Helper.Stuff;
import Helper.ViewHistoryOB;

public class ViewHistory extends Fragment {
    private View view;
    private final static List<ViewHistoryOB> arrayViewHistory = new ArrayList<>();
    private ProgressDialog pDialog = null;
    private Student s = null;
    private ImageView infoIcon, deleteHistory;
    private TextView notice;
    private JSONObject jsonObj;
    private FloatingActionButton deleteAllHistory;
    private String command, jsonURL;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;

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
        getActivity().setTitle("Recently Viewed Stuffs");
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

        view = inflater.inflate(R.layout.viewhistory, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("ViewHistory");
        infoIcon = (ImageView) view.findViewById(R.id.noViewHistory);
        notice = (TextView) view.findViewById(R.id.noViewHistoryFound);
        deleteAllHistory = view.findViewById(R.id.deleteAllViewHistory);

        getViewHistory();

        return view;
    }

    public void getViewHistory() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getViewHistory.php?studentID=" + s.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.toString().equals("[]")) {
                                    infoIcon.setVisibility(view.VISIBLE);
                                    notice.setVisibility(view.VISIBLE);
                                    deleteAllHistory.setEnabled(false);
                                }
                                try {
                                    arrayViewHistory.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject viewHistoryResponse = (JSONObject) response.get(i);
                                        arrayViewHistory.add(new ViewHistoryOB(viewHistoryResponse.getString("ViewHistoryID"),
                                                new Student(viewHistoryResponse.getString("StudentID")),
                                                viewHistoryResponse.getString("Date"),
                                                viewHistoryResponse.getString("Time"),
                                                new Stuff(new Student(viewHistoryResponse.getString("StuffSellerID"),
                                                        viewHistoryResponse.getString("StuffSeller")),
                                                        viewHistoryResponse.getString("stuffName"),
                                                        viewHistoryResponse.getString("stuffImage"),
                                                        viewHistoryResponse.getDouble("stuffPrice")),
                                                viewHistoryResponse.getString("status")));
                                    }

                                    populateListView();
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
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

    public void populateListView() {

        ArrayAdapter<ViewHistoryOB> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.ViewHistoryList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<ViewHistoryOB> {

        public MyListAdapter() {
            super(getActivity(), R.layout.viewhistory_cardview, arrayViewHistory);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;

            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.viewhistory_cardview, parent, false);
            }

            final ViewHistoryOB currentViewHistory = arrayViewHistory.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.viewedProductImage);
            Picasso.with(getActivity()).load(currentViewHistory.getStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.viewedProductName);
            stuffName.setText(currentViewHistory.getStuffID().getStuffName());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.viewedProductPrice);
            stuffPrice.setText(String.format("RM %.2f", currentViewHistory.getStuffID().getStuffPrice()));

            TextView viewTime = (TextView) itemView.findViewById(R.id.viewedTime);
            viewTime.setText("Viewed On " + currentViewHistory.getDate() + " " + currentViewHistory.getTime());

            TextView stuffSeller = (TextView) itemView.findViewById(R.id.viewedProductSeller);
            stuffSeller.setText(currentViewHistory.getStuffID().getStudentID().getStudentName());

//                ImageView deleteHistory = itemView.findViewById(R.id.deleteSearchHistory);
//
//                deleteHistory.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                switch (which) {
//                                    case DialogInterface.BUTTON_POSITIVE:
//
//                                        command = "{\"command\": \"30303530307A\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
//                                                "\"SearchHistoryID\": " + "\"" + Conversion.asciiToHex(currentSearchHistory.getSearchHistoryID()) + "\"}";
//
//                                        pahoMqttClient = new PahoMqttClient();
//                                        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
//
//                                        //updateParticularSearchHistory();
//                                        break;
//                                    case DialogInterface.BUTTON_NEGATIVE:
//                                        break;
//                                }
//                            }
//                        };
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
//                        builder.setMessage("Remove Search History ?").setPositiveButton("Yes", dialogClickListener)
//                                .setNegativeButton("No", dialogClickListener).show();
//                    }
//                });

            return itemView;
        }
    }
}
