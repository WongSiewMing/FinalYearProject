package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;

import static android.app.Activity.RESULT_OK;



public class StoreStuffFragment extends Fragment {
    public final static List<Stuff> stuffList = new ArrayList<>();
    private FragmentManager fragmentManager;
    private View view;
    private String userID;
    private ProgressDialog pDialog = null;
    private ImageView infoIcon;
    private TextView notice;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private String command = "";
    private JSONObject jsonObj;

    private static final String TAG = "testData";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("My Stuff List");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_storestuff_list, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        userID = bundle.getString("selectStuff");
        infoIcon = view.findViewById(R.id.infoIcon);
        notice = view.findViewById(R.id.notice);

        command = "{\"command\": \"303035303085\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(userID) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

        onClickCallBack();
        getStuffList();

        return view;
    }

    private void getStuffList(){
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
                if (jsonObj.getString("command").equals("303035303085")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getMyStuffs.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")), new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            if (response.toString().equals("[]")) {
                                infoIcon.setVisibility(view.VISIBLE);
                                notice.setVisibility(view.VISIBLE);
                            }

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
                                    Log.d(TAG, "Stuff ID fetched =" + stuffList.get(i).getStuffID());
                                }

                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                                populateStuffList();
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

    public void onClickCallBack() {
        ListView list = (ListView)view.findViewById(R.id.myStuffList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id){
                Stuff selectedStuff = stuffList.get(position);
                Intent intent = new Intent(getContext(), StoreStuffFragment.class);
                intent.putExtra("selectedStuff", selectedStuff);
                Log.d(TAG, "Stuff ID fetched =" + selectedStuff.getStuffID());
                getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, intent);
                getFragmentManager().popBackStack();
            }
        });
    }

    public void populateStuffList(){
        ArrayAdapter<Stuff> adapter = new MyStuffListAdapter();
        ListView list = view.findViewById(R.id.myStuffList);
        list.setAdapter(adapter);
        Log.d(TAG, "Entered populateStuffList");
    }

    public class MyStuffListAdapter extends ArrayAdapter<Stuff> {

        public MyStuffListAdapter() {
            super(getActivity(), R.layout.templatesellerstuff, stuffList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatesellerstuff, parent, false);
            }
            Stuff currentStuff = stuffList.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(currentStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(currentStuff.getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.stuffCategory);
            stuffCategory.setText(currentStuff.getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f",currentStuff.getStuffPrice()));

            return itemView;

        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity){
            a = (Activity) context;
        }

    }
}
