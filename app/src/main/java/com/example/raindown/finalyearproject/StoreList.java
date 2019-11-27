package com.example.raindown.finalyearproject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StoreBasicInfoOB;
import Helper.Student;

/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

public class StoreList extends Fragment {
    List<StoreBasicInfoOB> storeInfoList;
    View view;
    private String category, categoryType;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command;
    private TextView noRecordIndicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Store");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_store_list, container,false);

        storeInfoList = new ArrayList<>();

        Bundle bundle = getArguments();
        category = bundle.getString("Category");
        noRecordIndicator = view.findViewById(R.id.noRecordIndicator);

        if (category == "Food"){
            categoryType = "food";
        }else if (category == "Grocery"){
            categoryType = "grocery";
        }else if (category == "Stationary"){
            categoryType = "stationary";
        }else if (category == "Event Stall"){
            categoryType = "stall";
        }

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                populateStoreInfoList();
            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

            populateStoreInfoList();

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);


        return view;
    }

    private void populateStoreInfoList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreList.php?storeCategory=" + categoryType,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    storeInfoList.clear();
                                    for (int i = 0; i < response.length(); i++){
                                        JSONObject storeResponse = (JSONObject) response.get(i);
                                        storeInfoList.add(new StoreBasicInfoOB(storeResponse.getString("StoreID"),
                                                storeResponse.getString("StoreImage"),
                                                storeResponse.getString("StoreName"),
                                                storeResponse.getString("StoreDescription"),
                                                new Student(storeResponse.getString("StudentID"),
                                                        storeResponse.getString("StudentName"))));
                                    }
                                    if (response.length() == 0){
                                        noRecordIndicator.setVisibility(View.VISIBLE);
                                    }
                                    else {
                                        noRecordIndicator.setVisibility(View.INVISIBLE);
                                        populateRecyclerView();
                                    }


                                    try {
                                        command = "{\"command\": \"30303530304F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"storeCategory\": " + "\"" + Conversion.asciiToHex(categoryType) + "\" }";

                                        pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                                    }catch (MqttException e){
                                        e.printStackTrace();
                                    }catch (UnsupportedEncodingException e){
                                        e.printStackTrace();
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
    }


    private void populateRecyclerView(){
        RecyclerView storelistrv = (RecyclerView) view.findViewById(R.id.storeListrv);
        StoreListRecyclerViewAdapter storeListAdapter = new StoreListRecyclerViewAdapter(getActivity(), storeInfoList);
        storelistrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        storelistrv.setAdapter(storeListAdapter);
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
