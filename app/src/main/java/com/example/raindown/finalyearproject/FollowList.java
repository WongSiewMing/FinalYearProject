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
import com.android.volley.toolbox.*;
import com.squareup.picasso.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;
import java.util.*;
import Helper.*;

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class FollowList extends Fragment {

    public final static List<Subscribe> arrayFollowList = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;
    ProgressDialog pDialog = null;
    ImageView infoIcon;
    TextView notice;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    String command = "", countFollowersCommand = "", countFollowingCommand = "",
            ratingCommand = "", sellerStuffCommand = "", checkSubscribeCommand = "";
    JSONObject jsonObj;
    Subscribe clickedFollow;

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
        getActivity().setTitle("Follow List");
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

        view = inflater.inflate(R.layout.followlist, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("FollowList");
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        registerClickCallBack();

        command = "{\"command\": \"303035303011\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"subscriberID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");
        getFollowList();

        return view;
    }

    public void getFollowList() {
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
                if(jsonObj.getString("command").equals("303035303011")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getFollowList.php?subscriberID="
                            + Conversion.hexToAscii(jsonObj.getString("subscriberID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if(response.toString().equals("[]")){
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                    }
                                    try {
                                        arrayFollowList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject followResponse = (JSONObject) response.get(i);
                                            arrayFollowList.add(new Subscribe(followResponse.getString("subscribeID"),
                                                    new Student(followResponse.getString("studentID"), followResponse.getString("clientID"),
                                                            followResponse.getString("photo"), followResponse.getString("studentName"), followResponse.getString("icNo"),
                                                            followResponse.getString("studentProgramme"), followResponse.getString("studentFaculty"),
                                                            followResponse.getInt("yearOfStudy")),
                                                    new Student(followResponse.getString("studentID"), followResponse.getString("clientID"),
                                                            followResponse.getString("photo"), followResponse.getString("studentName"), followResponse.getString("icNo"),
                                                            followResponse.getString("studentProgramme"), followResponse.getString("studentFaculty"),
                                                            followResponse.getInt("yearOfStudy")),
                                                    followResponse.getString("subscribeDate"),followResponse.getString("unsubscribeDate"),followResponse.getString("subscribeStatus")));

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
        ArrayAdapter<Subscribe> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.followingList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Subscribe> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatefollowlist, arrayFollowList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatefollowlist, parent, false);
            }
            Subscribe currentFollow = arrayFollowList.get(position);

            ImageView studentImage = (ImageView) itemView.findViewById(R.id.studentImage);
            Picasso.with(getActivity()).load(currentFollow.getSubscribeeID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(studentImage);

            TextView studentName = (TextView) itemView.findViewById(R.id.studentName);
            studentName.setText(currentFollow.getSubscribeeID().getStudentName());

            TextView programme = (TextView) itemView.findViewById(R.id.programme);
            programme.setText(currentFollow.getSubscribeeID().getStudentProgramme());

            TextView year = (TextView) itemView.findViewById(R.id.year);
            year.setText("Year " + String.valueOf(currentFollow.getSubscribeeID().getYearOfStudy()));

            return itemView;

        }
    }


    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.followingList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                clickedFollow = arrayFollowList.get(position);

                countFollowersCommand = "{\"command\": \"303035303038\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"subscribeeID\": " + "\"" + Conversion.asciiToHex(clickedFollow.getSubscribeeID().getStudentID()) + "\"}";

                pahoMqttClient = new PahoMqttClient();
                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,countFollowersCommand,"MY/TARUC/SSS/000000001/PUB");
                countFollowingMethod();

            }
        });
    }


    public void countFollowingMethod(){

        countFollowingCommand = "{\"command\": \"303035303039\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"subscriberID\": " + "\"" + Conversion.asciiToHex(clickedFollow.getSubscribeeID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,countFollowingCommand,"MY/TARUC/SSS/000000001/PUB");
        getRatingMethod();
    }


    public void getRatingMethod(){

        ratingCommand = "{\"command\": \"30303530300A\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(clickedFollow.getSubscribeeID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,ratingCommand,"MY/TARUC/SSS/000000001/PUB");
        getSellerDetailsMethod();
    }


    public void getSellerDetailsMethod(){

        sellerStuffCommand = "{\"command\": \"30303530300B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(clickedFollow.getSubscribeeID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,sellerStuffCommand,"MY/TARUC/SSS/000000001/PUB");
        checkSubscribeMethod();
    }


    public void checkSubscribeMethod(){

        checkSubscribeCommand = "{\"command\": \"30303530300C\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"subscriberID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                "\"subscribeeID\": " + "\"" + Conversion.asciiToHex(clickedFollow.getSubscribeeID().getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(),Constant.serverUrl,checkSubscribeCommand,"MY/TARUC/SSS/000000001/PUB");

        SellerProfile2 frag = new SellerProfile2();
        Bundle bundles = new Bundle();
        bundles.putSerializable("ClickedFollow", clickedFollow);
        bundles.putSerializable("Me", s);
        bundles.putSerializable("Followers", countFollowersCommand);
        bundles.putSerializable("Following", countFollowingCommand);
        bundles.putSerializable("Rating", ratingCommand);
        bundles.putSerializable("SellerStuff",sellerStuffCommand);
        bundles.putSerializable("CheckSubscribe",checkSubscribeCommand);
        frag.setArguments(bundles);



        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.update_fragmentHolder, frag)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }


}
