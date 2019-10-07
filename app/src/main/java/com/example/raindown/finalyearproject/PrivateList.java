package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;
import java.util.*;
import Helper.*;

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class PrivateList extends Fragment {

    public final static List<PrivateChat> arrayPrivateChatList = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;
    ProgressDialog pDialog = null;
    public static String confirmedTopic = "";
    String reci = "";
    public final static List<PrivateChat> arrayPrivateChat = new ArrayList<>();
    PrivateChat clickedPrivateChat;
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
        getActivity().setTitle("Private Chat");
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

        view =  inflater.inflate(R.layout.privatelist, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("PrivateList");
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        pDialog = new ProgressDialog(getActivity());
        registerClickCallBack();

        command = "{\"command\": \"303035303018\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
        getPrivateList();
        return view;
    }

    public void getPrivateList() {

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
                if(jsonObj.getString("command").equals("303035303018")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getPrivateList.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if(response.toString().equals("[]")){
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                    }
                                    try {
                                        arrayPrivateChatList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject privatelistResponse = (JSONObject) response.get(i);
                                            arrayPrivateChatList.add(new PrivateChat(new Student(privatelistResponse.getString("studentID"),
                                                    privatelistResponse.getString("clientID"),privatelistResponse.getString("photo"),
                                                    privatelistResponse.getString("studentName"),privatelistResponse.getString("icNo"),
                                                    privatelistResponse.getString("studentProgramme"),privatelistResponse.getString("studentFaculty"),
                                                    privatelistResponse.getInt("yearOfStudy")),privatelistResponse.getString("message"),
                                                    privatelistResponse.getString("postDate"),privatelistResponse.getString("postTime")));
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

        ArrayAdapter<PrivateChat> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.privateChatList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<PrivateChat> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templateprivatelist, arrayPrivateChatList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templateprivatelist, parent, false);
            }
            PrivateChat currentPrivateChat = arrayPrivateChatList.get(position);

            ImageView studentImage = (ImageView) itemView.findViewById(R.id.studentImage);
            Picasso.with(getActivity()).load(currentPrivateChat.getRecipient2().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(studentImage);

            TextView studentName = (TextView) itemView.findViewById(R.id.studentName);
            studentName.setText(currentPrivateChat.getRecipient2().getStudentName());

            TextView date = (TextView) itemView.findViewById(R.id.date);
            date.setText(currentPrivateChat.getPostDate());

            TextView message = (TextView) itemView.findViewById(R.id.message);
            message.setText(currentPrivateChat.getMessage());

            return itemView;

        }
    }


    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.privateChatList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                clickedPrivateChat = arrayPrivateChatList.get(position);
                reci = clickedPrivateChat.getRecipient2().getStudentID();

                command = "{\"command\": \"303035303036\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                        "\"recipient\": " + "\"" + Conversion.asciiToHex(reci) + "\"}";

                pahoMqttClient = new PahoMqttClient();
                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                getTopic();

            }
        });
    }


    public void getTopic() {

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

                if(jsonObj.getString("command").equals("303035303036")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getTopic.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID"))
                            + "&recipient=" + Conversion.hexToAscii(jsonObj.getString("recipient")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {

                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject topicResponse = (JSONObject) response.get(i);
                                            confirmedTopic = topicResponse.getString("ChatGroupID");
                                        }

                                        if (pDialog.isShowing())
                                            pDialog.dismiss();

                                        command = "{\"command\": \"303035303037\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                                                "\"recipient\": " + "\"" + Conversion.asciiToHex(reci) + "\"}";

                                        pahoMqttClient = new PahoMqttClient();
                                        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");
                                        getPreviousPrivateChat();

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


    public void getPreviousPrivateChat() {

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
                if(jsonObj.getString("command").equals("303035303037")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getPreviousPrivateChat.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")) + "&recipient=" + Conversion.hexToAscii(jsonObj.getString("recipient")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        arrayPrivateChat.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject privateChatResponse = (JSONObject) response.get(i);
                                            arrayPrivateChat.add(new PrivateChat(
                                                    privateChatResponse.getString("studentID"), privateChatResponse.getString("studentName"),
                                                    privateChatResponse.getString("recipient"), privateChatResponse.getString("message"),
                                                    privateChatResponse.getString("postDate"), privateChatResponse.getString("postTime")));
                                        }
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();

                                        PrivateChatRoom2 frag = new PrivateChatRoom2();
                                        Bundle bundles = new Bundle();
                                        bundles.putSerializable("Recipient", clickedPrivateChat.getRecipient2());
                                        bundles.putSerializable("Me", s);
                                        frag.setArguments(bundles);
                                        fragmentManager = getFragmentManager();
                                        fragmentManager.beginTransaction()
                                                .replace(R.id.update_fragmentHolder, frag)
                                                .addToBackStack(null)
                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                .commit();

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

}
