package com.example.raindown.finalyearproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.SearchView;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.PrivateChatOB;
import Helper.Room;
import Helper.SearchOB;
import Helper.StoreBasicInfoOB;
import Helper.Student;

import static android.content.Context.INPUT_METHOD_SERVICE;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/


public class ChatRoomList_V2 extends Fragment {

    View view;
    private List<SearchOB> searchList = new ArrayList<>();
    private List<Room> arrayRoomList = new ArrayList<>();
    private List<PrivateChatOB> privateChatList = new ArrayList<>();
    private String UserID, command= "", searchUserUrl, roomCommand = "";

    private SearchView searchView;
    private RecyclerView search_userListrv;
    private RelativeLayout progressField;
    private TextView noMatchText;
    private ProgressBar chatListProgressBar;
    private RecyclerView chatListrv;
    private RelativeLayout chatField;



    private JSONObject josnObj;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private SearchUserRecyclerViewAdapter searchUserAdapter;
    private PrivateChatRoomList_Adapter chatRoomListAdapter;
    private Student s;
    private JSONObject jsonObj;


    private static final String TAG = "Chat Room";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Private Chat");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat_room_list__v2, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("chatRoomList_v2");
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        searchView = view.findViewById(R.id.search_user);
        search_userListrv = view.findViewById(R.id.search_userList);
        progressField = view.findViewById(R.id.progressField);
        noMatchText = view.findViewById(R.id.noMatchText);
        chatListProgressBar = view.findViewById(R.id.chatListProgressBar);
        chatListrv = view.findViewById(R.id.previousChatList);
        chatField = view.findViewById(R.id.chatField);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                Log.d(TAG, "Message =" + query);
                progressField.setVisibility(View.VISIBLE);
                chatField.setVisibility(View.INVISIBLE);
                populateSearchList(query);
                try {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                } catch (Exception e) {

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()){
                    noMatchText.setVisibility(View.GONE);
                    search_userListrv.setVisibility(View.GONE);
                    chatField.setVisibility(View.VISIBLE);
                }
//                if (newText != null && !newText.isEmpty()){
//                    Log.d(TAG, "Message =" + newText);
//                    //populateSearchList();
//
//                }else {
//                    Log.d(TAG, "Search Bar is empty");
//                }

                return true;
            }
        });


        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {


            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d(TAG,"Mqtt responded!");
                populatePrivateChatList();

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

    private void populatePrivateChatList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                command = "{\"command\": \"303035303018\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                jsonObj = new JSONObject(command);
                if(jsonObj.getString("command").equals("303035303018")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getPrivateList.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        privateChatList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject privatelistResponse = (JSONObject) response.get(i);
                                            privateChatList.add(new PrivateChatOB(privatelistResponse.getString("message"),
                                                    privatelistResponse.getString("image"),
                                                    privatelistResponse.getString("postDate"),
                                                    privatelistResponse.getString("postTime"),
                                                    new Student(privatelistResponse.getString("studentID"),
                                                            privatelistResponse.getString("clientID"),
                                                            privatelistResponse.getString("photo"),
                                                            privatelistResponse.getString("studentName"),
                                                            privatelistResponse.getString("icNo"),
                                                            privatelistResponse.getString("studentProgramme"),
                                                            privatelistResponse.getString("studentFaculty"),
                                                            privatelistResponse.getInt("yearOfStudy"))));

                                        }
                                        if (response.length() == 0){

                                        }else{
                                            Log.d(TAG,"Respond length = " + response.length());
                                            populatePrivateListView();
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

    private void populatePrivateListView() {
        chatRoomListAdapter = new PrivateChatRoomList_Adapter(getActivity(), privateChatList, s);
        chatListrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        chatListrv.setAdapter(chatRoomListAdapter);
        chatListProgressBar.setVisibility(View.GONE);
        chatField.setVisibility(View.VISIBLE);

    }


    private void populateSearchList(String query) {
        try {
            Log.d(TAG, "Hi commentID responded");
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                command = "{\"command\": \"303035303052\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                        "\"SearchText\": " + "\"" + Conversion.asciiToHex(query) + "\" }";
                pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                String encodedSearchText = "";
                josnObj = new JSONObject(command);
                if (josnObj.getString("command").equals("303035303052")){
                    String[] encodeSearchText = {Conversion.hexToAscii(josnObj.getString("SearchText"))};
                    for (String s : encodeSearchText){
                        encodedSearchText += URLEncoder.encode(s, "UTF-8");
                    }
                }

                searchUserUrl = Constant.serverFile + "getSearchUser.php?searchText=" + encodedSearchText;
                Log.d(TAG, "Search user url = " + searchUserUrl);

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(searchUserUrl,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    searchList.clear();
                                    for (int i = 0; i< response.length(); i++){
                                        JSONObject searchResponse = (JSONObject) response.get(i);
                                        searchList.add(new SearchOB(searchResponse.getString("photo"),
                                                searchResponse.getString("studentName"),
                                                searchResponse.getString("studentID")));
                                    }
                                    Log.d(TAG, "Response length =" + response.length());
                                    if (response.length() == 0){
                                        progressField.setVisibility(View.GONE);
                                        noMatchText.setVisibility(View.VISIBLE);
                                    }else {
                                        populateSearchRecycleView();
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

    private void populateSearchRecycleView() {
        searchUserAdapter = new SearchUserRecyclerViewAdapter(getActivity(), searchList, s);
        search_userListrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        search_userListrv.setAdapter(searchUserAdapter);
        noMatchText.setVisibility(View.GONE);
        progressField.setVisibility(View.GONE);
        search_userListrv.setVisibility(View.VISIBLE);

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "You leaved");
        try {
//            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
        try {
//            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }
}
