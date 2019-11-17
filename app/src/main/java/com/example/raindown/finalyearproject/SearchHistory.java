package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.SearchHistoryOB;
import Helper.Student;

public class SearchHistory extends Fragment {
    private View view;
    private final static List<SearchHistoryOB> arraySearchHistory = new ArrayList<>();
    private ProgressDialog pDialog = null;
    private Student s = null;
    private ImageView infoIcon, deleteHistory;
    private TextView notice;
    private JSONObject jsonObj;
    private FloatingActionButton deleteAllHistory;
    private String command, jsonURL;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;

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
        getActivity().setTitle("Search History");
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

        view = inflater.inflate(R.layout.searchhistory, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("SearchHistory");
        infoIcon = (ImageView) view.findViewById(R.id.noSearchHistory);
        notice = (TextView) view.findViewById(R.id.noSearchHistoryFound);
        deleteAllHistory = view.findViewById(R.id.deleteAllSearchHistory);

        getSearchHistory();

        deleteAllHistory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                command = "{\"command\": \"30303530307B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"StudentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                updateSearchHistory();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Remove All Search History ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });



        return view;
    }

    public void getSearchHistory() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSearchHistory.php?studentID=" + s.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.toString().equals("[]")) {
                                    infoIcon.setVisibility(view.VISIBLE);
                                    notice.setVisibility(view.VISIBLE);
                                    deleteAllHistory.setEnabled(false);
                                }
                                try {
                                    arraySearchHistory.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject searchHistoryResponse = (JSONObject) response.get(i);
                                        arraySearchHistory.add(new SearchHistoryOB(searchHistoryResponse.getString("SearchHistoryID"),
                                                new Student(searchHistoryResponse.getString("StudentID")),
                                                searchHistoryResponse.getString("Date"),
                                                searchHistoryResponse.getString("Time"),
                                                searchHistoryResponse.getString("SearchKeyword"),
                                                searchHistoryResponse.getString("status")));
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

        ArrayAdapter<SearchHistoryOB> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.SearchHistoryList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<SearchHistoryOB> {

        public MyListAdapter() {
            super(getActivity(), R.layout.searchhistorycardview, arraySearchHistory);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;

            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.searchhistorycardview, parent, false);
            }

            final SearchHistoryOB currentSearchHistory = arraySearchHistory.get(position);

            TextView searchKeyword = (TextView) itemView.findViewById(R.id.SearchKeyword);
            searchKeyword.setText(currentSearchHistory.getSearchKeyword());

            TextView searchTime = (TextView) itemView.findViewById(R.id.searchTime);
            searchTime.setText("Searched On " + currentSearchHistory.getDate() + " " + currentSearchHistory.getTime());

            ImageView deleteHistory = itemView.findViewById(R.id.deleteSearchHistory);

            deleteHistory.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:

                                    command = "{\"command\": \"30303530307A\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"SearchHistoryID\": " + "\"" + Conversion.asciiToHex(currentSearchHistory.getSearchHistoryID()) + "\"}";

                                    pahoMqttClient = new PahoMqttClient();
                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                    updateParticularSearchHistory();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Remove Search History ?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });

            return itemView;
        }
    }

    public void updateSearchHistory() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                try {
                    jsonObj = new JSONObject(command);
                    if (jsonObj.getString("command").equals("30303530307B")) {

                        jsonURL = Constant.serverFile + "hideAllSearchHistory.php?StudentID=" + Conversion.hexToAscii(jsonObj.getString("StudentID")) + "&status=" + "Hide";

                        RequestQueue queue = Volley.newRequestQueue(getActivity());
                        try {
                            StringRequest postRequest = new StringRequest(
                                    Request.Method.POST,
                                    jsonURL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            if (isAdded()) {
                                            }
                                            JSONObject jsonObject = null;
                                            try {
                                                jsonObject = new JSONObject(response);
                                                String success = jsonObject.getString("success");
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                if (success.equals("1")) {
                                                    SearchHistory searchHistory = new SearchHistory();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("SearchHistory", s);
                                                    searchHistory.setArguments(bundle);

                                                    getFragmentManager()
                                                            .beginTransaction()
                                                            .replace(R.id.update_fragmentHolder, searchHistory)
                                                            .commit();
                                                } else {
                                                    builder.setTitle("Oops !");
                                                    builder.setMessage("Something Wrong. Please Try Again.");
                                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            if (isAdded()) {
                                            }

                                        }
                                    }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<>();
                                    try {
                                        params.put("StudentID", Conversion.hexToAscii(jsonObj.getString("StudentID")));
                                        params.put("status", "Hide");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return params;
                                }

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("Content-Type", "application/x-www-form-urlencoded");
                                    return params;
                                }
                            };
                            queue.add(postRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAdded()) {
                    }
                }

            }

        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void updateParticularSearchHistory() {

        try {

            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                try {
                    jsonObj = new JSONObject(command);
                    if (jsonObj.getString("command").equals("30303530307A")) {

                        jsonURL = Constant.serverFile + "hideSearchHistory.php?SearchHistoryID=" + Conversion.hexToAscii(jsonObj.getString("SearchHistoryID")) + "&status=" + "Hide";

                        RequestQueue queue = Volley.newRequestQueue(getActivity());
                        try {
                            StringRequest postRequest = new StringRequest(
                                    Request.Method.POST,
                                    jsonURL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            if (isAdded()) {
                                            }
                                            JSONObject jsonObject = null;
                                            try {
                                                jsonObject = new JSONObject(response);
                                                String success = jsonObject.getString("success");
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                if (success.equals("1")) {
                                                    SearchHistory searchHistory = new SearchHistory();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("SearchHistory", s);
                                                    searchHistory.setArguments(bundle);

                                                    getFragmentManager()
                                                            .beginTransaction()
                                                            .replace(R.id.update_fragmentHolder, searchHistory)
                                                            .commit();
                                                } else {
                                                    builder.setTitle("Oops !");
                                                    builder.setMessage("Something Wrong. Please Try Again.");
                                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            if (isAdded()) {
                                            }

                                        }
                                    }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<>();
                                    try {
                                        params.put("SearchHistoryID", Conversion.hexToAscii(jsonObj.getString("SearchHistoryID")));
                                        params.put("status", "Hide");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return params;
                                }

                                @Override
                                public Map<String, String> getHeaders() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("Content-Type", "application/x-www-form-urlencoded");
                                    return params;
                                }
                            };
                            queue.add(postRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (isAdded()) {
                    }
                }

            }

        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
