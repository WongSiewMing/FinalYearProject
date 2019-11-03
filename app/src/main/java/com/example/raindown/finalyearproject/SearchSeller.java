package com.example.raindown.finalyearproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.SearchOB;
import Helper.Student;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SearchSeller extends Fragment {

    View view;
    private List<SearchOB> sellerList = new ArrayList<>();
    private SearchView searchSeller;
    private Student s;
    private TextView sellerNotFound,startSearchSeller;
    private RelativeLayout sellerProgressField;
    private String command = "", searchSellerURL = "";
    private RecyclerView search_seller_result;
    private ImageView searchImage, infoImage;

    private JSONObject josnObj;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private SearchSellerRecyclerViewAdapter searchSellerAdapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Search Seller");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_seller_profile, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("createTrade");
        searchSeller = view.findViewById(R.id.search_seller);
        search_seller_result = view.findViewById(R.id.search_sellerList);
        sellerNotFound = view.findViewById(R.id.noSellerFound);
        startSearchSeller = view.findViewById(R.id.findSeller);
        sellerProgressField = view.findViewById(R.id.sellerProgressField);
        searchImage = view.findViewById(R.id.searchImage);
        infoImage = view.findViewById(R.id.noSeller);

        searchSeller.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                sellerProgressField.setVisibility(View.VISIBLE);
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
                    infoImage.setVisibility(View.GONE);
                    sellerNotFound.setVisibility(View.GONE);
                    search_seller_result.setVisibility(View.GONE);
                    searchImage.setVisibility(View.VISIBLE);
                    startSearchSeller.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        return view;
    }

    private void populateSearchList(String query) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                command = "{\"command\": \"303035303052\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"StudentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
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

                searchSellerURL = Constant.serverFile + "getSearchUser.php?searchText=" + encodedSearchText;

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(searchSellerURL,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    sellerList.clear();
                                    for (int i = 0; i< response.length(); i++){
                                        JSONObject searchResponse = (JSONObject) response.get(i);
                                        sellerList.add(new SearchOB(searchResponse.getString("photo"),
                                                searchResponse.getString("studentName"),
                                                searchResponse.getString("studentID")));
                                    }

                                    if (response.length() == 0){
                                        searchImage.setVisibility(View.GONE);
                                        startSearchSeller.setVisibility(View.GONE);
                                        sellerProgressField.setVisibility(View.GONE);
                                        sellerNotFound.setVisibility(View.VISIBLE);
                                        infoImage.setVisibility(View.VISIBLE);
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
        searchSellerAdapter = new SearchSellerRecyclerViewAdapter(getActivity(), sellerList, s);
        search_seller_result.setLayoutManager(new LinearLayoutManager(getActivity()));
        search_seller_result.setAdapter(searchSellerAdapter);
        startSearchSeller.setVisibility(View.GONE);
        searchImage.setVisibility(View.GONE);
        infoImage.setVisibility(View.GONE);
        sellerNotFound.setVisibility(View.GONE);
        sellerProgressField.setVisibility(View.GONE);
        search_seller_result.setVisibility(View.VISIBLE);

    }
}
