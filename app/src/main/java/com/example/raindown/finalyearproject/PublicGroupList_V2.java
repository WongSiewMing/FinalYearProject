package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.content.Context;
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
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.PrivateChatOB;
import Helper.Room;
import Helper.Student;


public class PublicGroupList_V2 extends Fragment {
    View view;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private static final String TAG = "PublicGroupList";
    private RecyclerView publicGroupList;
    private List<Room> arrayGroupList = new ArrayList<>();
    private String command = "";
    private JSONObject jsonObj;
    private Student s;
    private PublicChatListAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_public_group_list__v2, container, false);
        publicGroupList = view.findViewById(R.id.publicGroupRecycleView);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("publicGroupList_v2");



        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d(TAG,"MQTT Connect");
                populatePublicGroupList();

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

        return view;

    }

    private void populatePublicGroupList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                command = "{\"command\": \"30303530301A\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"participant\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                jsonObj = new JSONObject(command);
                if(jsonObj.getString("command").equals("30303530301A")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getRoomList.php?participant="
                            + Conversion.hexToAscii(jsonObj.getString("participant")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        arrayGroupList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject roomListResponse = (JSONObject) response.get(i);
                                            arrayGroupList.add(new Room(roomListResponse.getString("roomID"),roomListResponse.getString("creator"),
                                                    roomListResponse.getString("subject"),roomListResponse.getString("photo"),
                                                    roomListResponse.getString("createDate"),roomListResponse.getString("createTime"),
                                                    roomListResponse.getString("status"),roomListResponse.getString("checkparticipant")));

                                        }
                                        if (response.length() == 0){

                                        }else{
                                            Log.d(TAG,"Respond length = " + response.length());
                                            populateGroupRecyclerView();
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
                }


            } else {
                Toast.makeText(getActivity(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void populateGroupRecyclerView() {
        adapter = new PublicChatListAdapter(getActivity(), arrayGroupList, s);
        publicGroupList.setLayoutManager(new LinearLayoutManager(getActivity()));
        publicGroupList.setAdapter(adapter);
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
        Log.d(TAG, "You leaved");
        try {
           // pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
        try {
          //  pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
