package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
   Programme : RSD3
   Year : 2018*/
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StoreOB;
import Helper.Student;
import Helper.Stuff;

import static android.app.Activity.RESULT_OK;


public class StoreProfile extends Fragment{
    View view;
    List<StoreOB> storeProfile;
    private TextView shopName, avgRating, openTime, closeTime, condition, storeDescription, ratDrscription, popRating, ratSummary, storeLocation;
    private ImageView shopImage, back;
    private Button editProfile, viewReview;
    private ProgressDialog pDialog = null;
    private String selectedStoreID, ratingTotalNum, currentTime, UserID;
    private Dialog popUpRating;
    FragmentManager fragmentManager;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command;
    private JSONObject jsonObj;
    private Double totalRate, avgRate;
    private ProgressBar progressBar;
    private RelativeLayout body;

    private RecyclerView mRecycleView;
    private StoreStuffAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<Stuff> stuffList = new ArrayList<>();

    private static final String TAG = "StoreProfile";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getActivity().setTitle("Shop Profile");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_store_profile, container, false);
        shopName = (TextView) view.findViewById(R.id.shopName);
        avgRating = (TextView) view.findViewById(R.id.avgRating);
        openTime = (TextView) view.findViewById(R.id.OpenTime);
        closeTime = (TextView) view.findViewById(R.id.CloseTime);
        condition = (TextView) view.findViewById(R.id.storeStatus);
        storeDescription = (TextView) view.findViewById(R.id.storeDescription);
        shopImage = (ImageView) view.findViewById(R.id.shopImage);
        editProfile = (Button) view.findViewById(R.id.editProfile);
        storeLocation = view.findViewById(R.id.storeLocation);
        back = view.findViewById(R.id.back);
        progressBar = view.findViewById(R.id.progressBar);
        pDialog = new ProgressDialog(getActivity());
        body = view.findViewById(R.id.body);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        Log.d(TAG, "User ID =" + UserID);

        shopName.setText(UserSharedPreferences.read(UserSharedPreferences.userName, null));
        storeProfile = new ArrayList<>();
        Bundle bundle = getArguments();
        selectedStoreID = bundle.getString("storeID");
        shopName.setText(selectedStoreID);

        final Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        currentTime = sdf.format(d);
        //make changes here (populate)
        populateStoreInfo();
        populateStoreStuffListInfo();


        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d(TAG, "Mqtt Connected");

                populateStoreInfo();
                populateStoreStuffListInfo();
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



        //review bottom sheet
        viewReview = (Button) view.findViewById(R.id.viewReview);
        viewReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("storeID", storeProfile.get(0).getStoreID());

                ReviewBottomSheetDialog bottomSheet = new ReviewBottomSheetDialog();
                bottomSheet.setArguments(bundle);
                bottomSheet.show(getFragmentManager(), "reviewBottomSheet");

            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditStoreProfile frag = new EditStoreProfile();
                Bundle bundle = new Bundle();
//                bundle.putString("storeID", selectedStoreID);
                bundle.putSerializable("storeInfo", storeProfile.get(0));
                bundle.putSerializable("stuffList", stuffList);
                frag.setArguments(bundle);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
            }
        });



        popUpRating = new Dialog(getActivity());

        avgRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popUpRating.setContentView(R.layout.ratingpopup);
                ratDrscription = popUpRating.findViewById(R.id.ratingDescription);
                popRating = popUpRating.findViewById(R.id.popRating);
                ratSummary = popUpRating.findViewById(R.id.ratingsummary);

                try {
                    ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
                    if (isConnected) {
                        RequestQueue queue = Volley.newRequestQueue(getActivity());
                        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreReviewCount.php?storeID=" + selectedStoreID,
                                new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try {

                                            JSONObject storeReviewResponse = (JSONObject) response.get(0);
                                            ratingTotalNum = storeReviewResponse.getString("reviewCount");
                                            if (ratingTotalNum.equals("0")){
                                                ratDrscription.setText("No review yet...");
                                            }else {
                                                ratDrscription.setText("Number of review received: " + ratingTotalNum);
                                                popRating.setText(String.format("%.2f", avgRate));
                                                ratSummary.setText(String.format("%.2f", avgRate) + " out of 5.");
                                                //make changes here (avgRating)
                                                avgRating.setText(String.format("%.2f", avgRate));
                                            }

                                        }catch (Exception e){

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

                //make changes here (popRating & ratSummary)
                 popRating.setText(avgRating.getText().toString());
                ratSummary.setText(avgRating.getText() + " out of 5.");

                popUpRating.show();

            }
        });



        return view;
    }

    public void getAvgRating() {
        command = "{\"command\": \"303035303057\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                "\"storeID\": " + "\"" + Conversion.asciiToHex(selectedStoreID) + "\"}";

        try {
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            //http://192.168.0.107/raindown/getStoreAvgRating.php?storeID=str1003

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303057")) {
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreAvgRating.php?storeID="
                        + Conversion.hexToAscii(jsonObj.getString("storeID")),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d(TAG, "Chat responded!");
                                try {
                                    totalRate = 0.0;
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject ratingResponse = (JSONObject) response.get(i);
                                        totalRate += Double.parseDouble(ratingResponse.getString("RatValue"));

                                    }
                                    if (response.length() == 0) {
                                        Log.d(TAG, "Empty Rating");
                                    } else {
                                        Log.d(TAG, "Rating sum up =" + totalRate);
                                        avgRate = totalRate / response.length();
                                        Log.d(TAG, "Avg Rating =" + avgRate);
                                        //make changes here (avgRating)
                                        avgRating.setText(String.valueOf(avgRate));
                                        avgRating.setText(String.format("%.2f", avgRate));


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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void populateStoreInfo() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreProfile.php?storeID=" + selectedStoreID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    storeProfile.clear();
                                    for (int i = 0; i < response.length(); i++){
                                        JSONObject storeResponse = (JSONObject) response.get(i);
                                        storeProfile.add(new StoreOB(storeResponse.getString("StoreID"),
                                                storeResponse.getString("StudentID"),
                                                storeResponse.getString("StoreName"),
                                                storeResponse.getString("StoreImage"),
                                                storeResponse.getString("StoreDescription"),
                                                storeResponse.getString("StoreCategory"),
                                                storeResponse.getString("OpenTime"),
                                                storeResponse.getString("CloseTime"),
                                                storeResponse.getString("StoreStatus"),
                                                storeResponse.getString("StoreLocation")));

                                    }
                                    populateView();
                                }catch (Exception e){

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

    private void populateStoreStuffListInfo() {
        command = "{\"command\": \"303035303087\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"storeID\": " + "\"" + Conversion.asciiToHex(selectedStoreID) + "\"}";
        try{
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            if (isConnected){
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing()){
                    pDialog.setMessage("Sync with server...");
                }
                pDialog.show();
                jsonObj = new JSONObject(command);
                if (jsonObj.getString("command").equals("303035303087")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreStuffList.php?storeID=" + selectedStoreID,
                            new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                stuffList.clear();
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject myStuffResponse = (JSONObject) response.get(i);
                                    stuffList.add(new Stuff(myStuffResponse.getString("stuffID"), new Student(myStuffResponse.getString("studentID"),
                                            myStuffResponse.getString("clientID"), myStuffResponse.getString("photo"), myStuffResponse.getString("studentName"),
                                            myStuffResponse.getString("icNo"), myStuffResponse.getString("studentProgramme"), myStuffResponse.getString("studentFaculty"),
                                            myStuffResponse.getInt("yearOfStudy")), myStuffResponse.getString("stuffName"), myStuffResponse.getString("stuffImage"),
                                            myStuffResponse.getString("stuffDescription"), myStuffResponse.getString("stuffCategory"), myStuffResponse.getString("stuffCondition"),
                                            myStuffResponse.getDouble("stuffPrice"), myStuffResponse.getInt("stuffQuantity"), myStuffResponse.getString("validStartDate"),
                                            myStuffResponse.getString("validEndDate"), myStuffResponse.getString("stuffStatus")));
                                    Log.d(TAG, "Stuff ID fetched (stuffList) =" + stuffList.get(i).getStuffID());
                                }

                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                                populateStuffAdapterView();
                            } catch (Exception e) {

                            }
                        }
                    },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (pDialog.isShowing()){
                                        pDialog.dismiss();
                                    }
                                }
                            });
                    queue.add(jsonObjectRequest);
                }

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e){
            Toast.makeText(getActivity().getApplication(), "Error Reading Record : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void populateStuffAdapterView(){
        mRecycleView = view.findViewById(R.id.storeProfileStuffLL);

        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new StoreStuffAdapter(stuffList);
        mAdapter.hideButton(true);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setLayoutManager(mLayoutManager);

        mAdapter.setOnItemClickListener(new StoreStuffAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Stuff clickedStuff = stuffList.get(position);
                StuffDetails frag = new StuffDetails();
                Bundle bundle = new Bundle();
                bundle.putSerializable("ClickedStuff", clickedStuff);
                //For StuffDetails, need UserID for who click it. Lazy to retrieve the whole student info
                bundle.putSerializable("StudentClickedStuff", new Student(UserID, "", "", "", "", "", 0));
                frag.setArguments(bundle);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }

            @Override
            public void onDeleteClick(int position) {
                //Leave here empty
            }

            @Override
            public void onEditClick(int position){
                //Leave here empty
            }
        });
    }



    private void populateView() {
        String a = storeProfile.get(0).getStudentID();
        String b = UserID;

        Log.d(TAG,"ID GET = " + a);
        Log.d(TAG,"ID Original = " + b);
        if (a.equals(b)){
            Log.d(TAG,"Owner");
            editProfile.setVisibility(View.VISIBLE);
        }else {
            Log.d(TAG, "Not Owner");

        }

        shopName.setText(storeProfile.get(0).getStoreName());
        Picasso.with(getActivity()).load(storeProfile.get(0).getStoreImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(shopImage);
        getAvgRating();
        avgRating.setText(String.format("%.2f", avgRate));


        openTime.setText(storeProfile.get(0).getOpenTime());
        closeTime.setText(storeProfile.get(0).getCloseTime());
        storeDescription.setText(storeProfile.get(0).getStoreDescription());
        storeLocation.setText(storeProfile.get(0).getStoreLocation());

        if (Integer.parseInt(currentTime) < Integer.parseInt(storeProfile.get(0).getCloseTime()) && Integer.parseInt(currentTime) >=
                Integer.parseInt(storeProfile.get(0).getOpenTime())){
            condition.setText("Open now");
        }else {
            condition.setText("Closed");
            condition.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }

        progressBar.setVisibility(View.GONE);
        body.setVisibility(View.VISIBLE);


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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "This is onResume");
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "This is onStop");
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "You leaved");
        try {
           pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
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
