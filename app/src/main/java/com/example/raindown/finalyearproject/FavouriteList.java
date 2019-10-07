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

public class FavouriteList extends Fragment {

    public final static List<Favourite> arrayFavouriteList = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;
    ProgressDialog pDialog = null;
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
        getActivity().setTitle("Favourite List");
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

        view = inflater.inflate(R.layout.favouritelist, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("FavouriteList");
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        registerClickCallBack();

        command = "{\"command\": \"303035303010\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl,command,"MY/TARUC/SSS/000000001/PUB");
        getFavouriteList();
        return view;
    }

    public void getFavouriteList() {

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
                if(jsonObj.getString("command").equals("303035303010")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getFavouriteList.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if(response.toString().equals("[]")){
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                    }
                                    try {
                                        arrayFavouriteList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject favouriteResponse = (JSONObject) response.get(i);
                                            arrayFavouriteList.add(new Favourite(favouriteResponse.getString("favouriteID"),
                                                    new Stuff(favouriteResponse.getString("stuffID"), new Student(favouriteResponse.getString("studentID"),
                                                            favouriteResponse.getString("clientID"), favouriteResponse.getString("photo"), favouriteResponse.getString("studentName"),
                                                            favouriteResponse.getString("icNo"), favouriteResponse.getString("studentProgramme"), favouriteResponse.getString("studentFaculty"),
                                                            favouriteResponse.getInt("yearOfStudy")), favouriteResponse.getString("stuffName"), favouriteResponse.getString("stuffImage"),
                                                            favouriteResponse.getString("stuffDescription"), favouriteResponse.getString("stuffCategory"), favouriteResponse.getString("stuffCondition"),
                                                            favouriteResponse.getDouble("stuffPrice"), favouriteResponse.getInt("stuffQuantity"), favouriteResponse.getString("validStartDate"),
                                                            favouriteResponse.getString("validEndDate"), favouriteResponse.getString("stuffStatus")),
                                                    new Student(favouriteResponse.getString("studentID"), favouriteResponse.getString("clientID"),
                                                            favouriteResponse.getString("photo"), favouriteResponse.getString("studentName"), favouriteResponse.getString("icNo"),
                                                            favouriteResponse.getString("studentProgramme"), favouriteResponse.getString("studentFaculty"),
                                                            favouriteResponse.getInt("yearOfStudy")), favouriteResponse.getString("favouriteDate"), favouriteResponse.getString("unfavouriteDate"),
                                                    favouriteResponse.getString("favouriteStatus")));

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

        ArrayAdapter<Favourite> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.favouriteList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Favourite> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatefavouritelist, arrayFavouriteList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatefavouritelist, parent, false);
            }
            Favourite currentFavourite = arrayFavouriteList.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(currentFavourite.getStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(currentFavourite.getStuffID().getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.stuffCategory);
            stuffCategory.setText(currentFavourite.getStuffID().getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f", currentFavourite.getStuffID().getStuffPrice()));
            return itemView;

        }
    }


    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.favouriteList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Favourite clickedFavourite = arrayFavouriteList.get(position);
                StuffDetails frag = new StuffDetails();
                Bundle bundles = new Bundle();
                bundles.putSerializable("ClickedStuff", clickedFavourite.getStuffID());
                bundles.putSerializable("StudentClickedStuff",s);
                frag.setArguments(bundles);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();


            }
        });
    }

}
