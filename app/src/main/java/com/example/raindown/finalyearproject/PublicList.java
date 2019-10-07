package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.graphics.Color;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.squareup.picasso.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class PublicList extends Fragment {

    public final static List<Room> arrayRoomList = new ArrayList<>();
    public static List<Message> arrayPreviousMsg = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;
    ProgressDialog pDialog = null;
    Room clickedRoom;
    public static String participateCount = "";
    ImageView infoIcon;
    TextView notice;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    String command = "";
    JSONObject jsonObj;

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
        getActivity().setTitle("Public Chat");
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

        view = inflater.inflate(R.layout.publiclist, container, false);
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("PublicList");

        registerClickCallBack();

		//Create MQTT command
        command = "{\"command\": \"30303530301A\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"participant\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

		//Connect and publish messages to broker
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

        getRoomList();

        Button createPublicChat = (Button) view.findViewById(R.id.createPublicChat);
        createPublicChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreatePublicChat frag = new CreatePublicChat();
                Bundle bundle = new Bundle();
                bundle.putSerializable("Me", s);
                frag.setArguments(bundle);
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });

        return view;
    }

    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.publicChatList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                clickedRoom = arrayRoomList.get(position);
                command = "{\"command\": \"30303530301B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"participant\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                        "\"roomID\": " + "\"" + Conversion.asciiToHex(clickedRoom.getRoomID()) + "\"}";

                pahoMqttClient = new PahoMqttClient();
                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
                getParticipation();

            }
        });
    }


    public void getRoomList() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(command);
                if(jsonObj.getString("command").equals("30303530301A")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getRoomList.php?participant=" + Conversion.hexToAscii(jsonObj.getString("participant")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if(response.toString().equals("[]")){
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                    }
                                    try {
                                        arrayRoomList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject roomListResponse = (JSONObject) response.get(i);
                                            arrayRoomList.add(new Room(roomListResponse.getString("roomID"),roomListResponse.getString("creator"),
                                                    roomListResponse.getString("subject"),roomListResponse.getString("photo"),
                                                    roomListResponse.getString("createDate"),roomListResponse.getString("createTime"),
                                                    roomListResponse.getString("status"),roomListResponse.getString("checkparticipant")));
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


    public void populateListView() {

        ArrayAdapter<Room> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.publicChatList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Room> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatepubliclist, arrayRoomList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatepubliclist, parent, false);
            }

            Room currentRoom = arrayRoomList.get(position);

            ImageView chatPhoto = (ImageView) itemView.findViewById(R.id.chatPhoto);
            Picasso.with(getActivity()).load(currentRoom.getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(chatPhoto);

            TextView subject = (TextView) itemView.findViewById(R.id.subject);
            subject.setText(currentRoom.getSubject());

            TextView participating = (TextView) itemView.findViewById(R.id.participationStatus);
            if(!currentRoom.getCheckParticipant().equals("0")){
                participating.setText("Joining");
                participating.setTextColor(Color.parseColor("#70a354"));
            }

            return itemView;

        }
    }


    public void getParticipation() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(command);
                if(jsonObj.getString("command").equals("30303530301B")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getParticipation.php?participant="
                            + Conversion.hexToAscii(jsonObj.getString("participant")) + "&roomID=" + Conversion.hexToAscii(jsonObj.getString("roomID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject participatingResponse = (JSONObject) response.get(i);
                                            participateCount = participatingResponse.getString("Participating");
                                        }
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();

                                        command = "{\"command\": \"30303530301C\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"roomID\": " + "\"" + Conversion.asciiToHex(clickedRoom.getRoomID()) + "\"}";

                                        pahoMqttClient = new PahoMqttClient();
                                        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                        jsonObj = new JSONObject(command);
                                        getPreviousPublicChat(getActivity(), Constant.serverFile + "getPreviousPublicChat.php?roomID=" + Conversion.hexToAscii(jsonObj.getString("roomID")));

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
                Toast.makeText(getActivity(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void getPreviousPublicChat(Context context, String url) {

        RequestQueue queue = Volley.newRequestQueue(context);
        if (!pDialog.isShowing())
            pDialog.setMessage("Sync with server...");
        pDialog.show();

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            arrayPreviousMsg.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject previousChatResponse = (JSONObject) response.get(i);
                                arrayPreviousMsg.add(new Message(previousChatResponse.getString("sender"), previousChatResponse.getString("roomID"),
                                        previousChatResponse.getString("message"), previousChatResponse.getString("postDate"),
                                        previousChatResponse.getString("postTime"), previousChatResponse.getString("studentName")));
                            }
                            PublicChatRoom frag = new PublicChatRoom();
                            Bundle bundles = new Bundle();
                            bundles.putSerializable("PublicList", clickedRoom);
                            bundles.putSerializable("Me", s);
                            frag.setArguments(bundles);
                            fragmentManager = getFragmentManager();
                            fragmentManager.beginTransaction()
                                    .replace(R.id.update_fragmentHolder, frag)
                                    .addToBackStack(null)
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .commit();
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
    }

}
