package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.URLEncoder;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Home extends Fragment {

    public final static List<HomeOption> arrayHomeOption = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;
    RecyclerView mRecyclerView;
    RecommendRecyclerViewAdapter mAdapter;
    ArrayList<Stuff> stuffList = new ArrayList<>();
    Set<Stuff> tempStuffList = new HashSet<>();
    Set<String> stuffIDList = new HashSet<>();
    Set<String> stuffCategoryList = new HashSet<>();
    ArrayList<SearchHistoryOB> searchList = new ArrayList<>();
    ArrayList<ViewHistoryOB> viewList = new ArrayList<>();

    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command = "", checkUrl = "", searchKeyword = "", stuffCategory = "";
    private JSONObject jsonObj;
    private ProgressDialog pDialog = null;
    private TextView txtRecommend;
    private static final String TAG = "Home";
    private boolean SearchHistoryExist, ViewHistoryExist;

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
        getActivity().setTitle("Home");
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
        view = inflater.inflate(R.layout.home, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("Home");
        pDialog = new ProgressDialog(getActivity());
        txtRecommend = (TextView) view.findViewById(R.id.txtRecommend);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recommendList);
        txtRecommend.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);

        populateRecommendStuffList();
        populateArrayHomeOption();

        registerClickCallBack();
        return view;
    }

    public void populateRecommendStuffList(){
        SearchHistoryExist = false;
        ViewHistoryExist = false;
        stuffList.clear();
        tempStuffList.clear();

        if (!checkSearchHistory()){
            checkViewHistory();
        }

        populateRecyclerView();
    }

    public boolean checkSearchHistory(){
        try{
            Log.d(TAG, "Enter checkSearchHistory 1");
            command = "{\"command\": \"30303530307B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + s.getStudentID() + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            if (isConnected){
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing()){
                    pDialog.setMessage("Sync with server...");
                }

                checkUrl = Constant.serverFile + "getSearchHistory.php?studentID=" + s.getStudentID();
                Log.d(TAG, "Check Search History Url = " + checkUrl);

                pDialog.show();
                jsonObj = new JSONObject(command);
                if (jsonObj.getString("command").equals("30303530307B")){
                    Log.d(TAG, "Enter checkSearchHistory 2");
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSearchHistory.php?studentID=" + s.getStudentID(),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        Log.d(TAG, "Enter checkSearchHistory 3");
                                        searchList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject existResponse = (JSONObject) response.get(i);
                                            searchList.add(new SearchHistoryOB(existResponse.getString("SearchHistoryID"),
                                                    new Student(existResponse.getString("StudentID")),
                                                    existResponse.getString("Date"),
                                                    existResponse.getString("Time"),
                                                    existResponse.getString("SearchKeyword"),
                                                    existResponse.getString("status")));
                                            Log.d(TAG, "Search ID = " + searchList.get(i).getSearchKeyword());
                                        }
                                        if (searchList.isEmpty()) {
                                            Log.d(TAG, "Check Search History = Not Exist");
                                            SearchHistoryExist = false;
                                        } else {
                                            Log.d(TAG, "Check Search History = Exist");
                                            SearchHistoryExist = true;
                                        }

                                        for (int i = 0; i < searchList.size(); i++){
                                            searchKeyword = searchList.get(i).getSearchKeyword();
                                            getStuffDetailBySearch();
                                        }
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();
                                    } catch (Exception e) {
                                        Log.d(TAG, "Check Search History Error : " + e.getMessage());
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
        return SearchHistoryExist;
    }

    public boolean checkViewHistory(){
        try {
            Log.d(TAG, "Enter checkViewHistory 1");
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing()){
                    pDialog.setMessage("Sync with server...");
                }

                checkUrl = Constant.serverFile + "getViewHistory.php?studentID=" + s.getStudentID();
                Log.d(TAG, "Check View History Url = " + checkUrl);

                pDialog.show();
                Log.d(TAG, "Enter checkViewHistory 2");
                try {
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getViewHistory.php?studentID=" + s.getStudentID(),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        Log.d(TAG, "Enter checkViewHistory 3");
                                        viewList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject existResponse = (JSONObject) response.get(i);
                                            viewList.add(new ViewHistoryOB(existResponse.getString("ViewHistoryID"),
                                                    new Student(existResponse.getString("StudentID")),
                                                    existResponse.getString("Date"),
                                                    existResponse.getString("Time"),
                                                    new Stuff(new Student(existResponse.getString("StuffSellerID"),
                                                            existResponse.getString("StuffSeller")),
                                                            existResponse.getString("stuffID"),
                                                            existResponse.getString("stuffName"),
                                                            existResponse.getString("stuffImage"),
                                                            existResponse.getDouble("stuffPrice"),
                                                            existResponse.getString("stuffStatus")),
                                                    existResponse.getString("status")));
                                            Log.d(TAG, "View ID = " + viewList.get(i).getViewHistoryID());

                                        }
                                        if (viewList.size() == 0) {
                                            Log.d(TAG, "Check View History = Not Exist");
                                            ViewHistoryExist = false;
                                        } else {
                                            Log.d(TAG, "Check View History = Exist");
                                            ViewHistoryExist = true;
                                        }
                                        for (int i = 0; i < viewList.size(); i++){
                                            searchKeyword = viewList.get(i).getStuffID().getStuffName();
                                            getStuffDetailBySearch();
                                        }

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

                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAdded()) {
                    }
                }
            }

        }catch (Exception e){
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        return ViewHistoryExist;
    }

    public void getStuffDetailByCategory(){
        try {
            Log.d(TAG, "Enter getStuffByCategory 1");
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing()){
                    pDialog.setMessage("Sync with server...");
                }

                checkUrl = Constant.serverFile + "getStuffListAccordingToCategory.php?stuffCategory=" + stuffCategory;
                Log.d(TAG, "Check Stuff Detail Url = " + checkUrl);

                pDialog.show();

                Log.d(TAG, "Enter getStuffByCategory 2");

                try {
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStuffListAccordingToCategory.php?stuffCategory=" + stuffCategory,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        Log.d(TAG, "Enter getStuffByCategory 3");
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject existResponse = (JSONObject) response.get(i);
                                            tempStuffList.add(new Stuff(existResponse.getString("stuffID"), new Student(existResponse.getString("studentID"),
                                                    existResponse.getString("clientID"), existResponse.getString("photo"), existResponse.getString("studentName"),
                                                    existResponse.getString("icNo"), existResponse.getString("studentProgramme"), existResponse.getString("studentFaculty"),
                                                    existResponse.getInt("yearOfStudy")), existResponse.getString("stuffName"), existResponse.getString("stuffImage"),
                                                    existResponse.getString("stuffDescription"), existResponse.getString("stuffCategory"), existResponse.getString("stuffCondition"),
                                                    existResponse.getDouble("stuffPrice"), existResponse.getInt("stuffQuantity"), existResponse.getString("validStartDate"),
                                                    existResponse.getString("validEndDate"), existResponse.getString("stuffStatus")));

                                            Log.d(TAG, "getStuffByCategory = " + existResponse.getString("stuffID"));
                                        }

                                        stuffList.clear();
                                        stuffList.addAll(tempStuffList);
                                        mAdapter.notifyDataSetChanged();
                                        if (!stuffList.isEmpty()){
                                            txtRecommend.setVisibility(View.VISIBLE);
                                            mRecyclerView.setVisibility(View.VISIBLE);
                                        }
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

                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAdded()) {
                    }
                }
            }
        } catch (Exception e){
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void getStuffDetailBySearch(){
        try {
            Log.d(TAG, "Enter getStuffBySearch 1");
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing()){
                    pDialog.setMessage("Sync with server...");
                }

                checkUrl = Constant.serverFile + "getStuffDetailByKeyword.php?searchKeyword=" + searchKeyword;
                Log.d(TAG, "Check Stuff Detail Url = " + checkUrl);

                pDialog.show();

                Log.d(TAG, "Enter getStuffBySearch 2");

                try {
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStuffDetailByKeyword.php?searchKeyword=" + searchKeyword,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        Log.d(TAG, "Enter getStuffBySearch 3");
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject existResponse = (JSONObject) response.get(i);
                                            tempStuffList.add(new Stuff(existResponse.getString("stuffID"), new Student(existResponse.getString("studentID"),
                                                    existResponse.getString("clientID"), existResponse.getString("photo"), existResponse.getString("studentName"),
                                                    existResponse.getString("icNo"), existResponse.getString("studentProgramme"), existResponse.getString("studentFaculty"),
                                                    existResponse.getInt("yearOfStudy")), existResponse.getString("stuffName"), existResponse.getString("stuffImage"),
                                                    existResponse.getString("stuffDescription"), existResponse.getString("stuffCategory"), existResponse.getString("stuffCondition"),
                                                    existResponse.getDouble("stuffPrice"), existResponse.getInt("stuffQuantity"), existResponse.getString("validStartDate"),
                                                    existResponse.getString("validEndDate"), existResponse.getString("stuffStatus")));

                                            Log.d(TAG, "getStuffBySearch = " + existResponse.getString("stuffID"));
                                        }
                                        for (Stuff stuff: tempStuffList){
                                            if (!stuffCategoryList.contains(stuff.getStuffCategory())){
                                                stuffCategory = stuff.getStuffCategory();
                                                stuffCategoryList.add(stuff.getStuffCategory());
                                                getStuffDetailByCategory();
                                            }
                                        }
                                        stuffList.clear();
                                        stuffList.addAll(tempStuffList);
                                        mAdapter.notifyDataSetChanged();

                                        if (!stuffList.isEmpty()){
                                            txtRecommend.setVisibility(View.VISIBLE);
                                            mRecyclerView.setVisibility(View.VISIBLE);
                                        }
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

                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAdded()) {
                    }
                }
            }

        }catch (Exception e){
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void populateArrayHomeOption() {
        arrayHomeOption.clear();
        arrayHomeOption.add(0, new HomeOption(R.mipmap.icon_sell, "Sell Stuffs"));
        arrayHomeOption.add(1, new HomeOption(R.mipmap.icon_buy, "Buy Stuffs"));
        arrayHomeOption.add(2, new HomeOption(R.mipmap.icon_search, "Search Stuffs"));
        arrayHomeOption.add(3, new HomeOption(R.mipmap.icon_request, "Request Stuffs List"));
        arrayHomeOption.add(4,new HomeOption(R.mipmap.icon_trading, "Trade Stuffs"));
        populateListView();
    }


    public void populateListView() {
        ArrayAdapter<HomeOption> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.homeOptionList);
        list.setAdapter(adapter);
    }

    public void populateRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecommendRecyclerViewAdapter(stuffList);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);



        mAdapter.setOnItemClickListener(new RecommendRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Stuff clickedStuff = stuffList.get(position);
                StuffDetails frag = new StuffDetails();
                Bundle bundle = new Bundle();
                bundle.putSerializable("ClickedStuff", clickedStuff);
                //For StuffDetails, need UserID for who click it. Lazy to retrieve the whole student info
                bundle.putSerializable("StudentClickedStuff", new Student(s.getStudentID(), "", "", "", "", "", 0));
                frag.setArguments(bundle);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });

    }


    public class MyListAdapter extends ArrayAdapter<HomeOption> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templatehome, arrayHomeOption);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatehome, parent, false);
            }
            HomeOption currentOption = arrayHomeOption.get(position);

            ImageView optionIcon = (ImageView) itemView.findViewById(R.id.homeOptionIcon);
            optionIcon.setImageResource(currentOption.getHomeOption());

            TextView optionName = (TextView) itemView.findViewById(R.id.homeOptionName);
            optionName.setText(currentOption.getHomeOptionName());

            return itemView;

        }
    }


    public void registerClickCallBack() {
        ListView list = (ListView) view.findViewById(R.id.homeOptionList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                if (arrayHomeOption.get(position).getHomeOptionName().equals("Sell Stuffs")) {
                    SellStuff frag = new SellStuff();
                    Bundle bundle1 = new Bundle();
                    bundle1.putSerializable("SellStuff", s);
                    frag.setArguments(bundle1);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else if (arrayHomeOption.get(position).getHomeOptionName().equals("Buy Stuffs")) {
                    BuyStuff frag = new BuyStuff();
                    Bundle bundle2 = new Bundle();
                    bundle2.putSerializable("BuyStuff", s);
                    frag.setArguments(bundle2);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                } else if (arrayHomeOption.get(position).getHomeOptionName().equals("Search Stuffs")) {
                    SearchStuff frag = new SearchStuff();
                    Bundle bundle3 = new Bundle();
                    bundle3.putSerializable("SearchStuff", s);
                    frag.setArguments(bundle3);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }else if (arrayHomeOption.get(position).getHomeOptionName().equals("Request Stuffs List")) {
                    RequestList  frag = new RequestList();
                    Bundle bundle3 = new Bundle();
                    bundle3.putSerializable("RequestStuffList", s);
                    frag.setArguments(bundle3);
                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }else if (arrayHomeOption.get(position).getHomeOptionName().equals("Trade Stuffs")) {
                Trade_Menu  frag = new Trade_Menu();
                Bundle bundle5 = new Bundle();
                    bundle5.putSerializable("TradeStuff", s);
                frag.setArguments(bundle5);
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
            }
        });
    }
}
