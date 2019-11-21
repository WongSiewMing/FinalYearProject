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
import android.support.v4.app.FragmentManager;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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
import Helper.Student;
import Helper.Stuff;
import Helper.Trade;
import Helper.TradeHistoryOB;
import Helper.ViewHistoryOB;

public class TradeHistory extends Fragment {
    private View view;
    private Student s = null;
    private FloatingActionButton deleteAllHistory;
    private final static List<TradeHistoryOB> arrayTradeHistory = new ArrayList<>();
    private ProgressDialog pDialog = null;
    private String command, jsonURL;
    private ImageView infoIcon;
    private TextView notice;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private FragmentManager fragmentManager;
    private JSONObject jsonObj;

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
        getActivity().setTitle("Trade History");
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.tradehistory, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("TradeHistory");
        infoIcon = (ImageView) view.findViewById(R.id.noTradeHistory);
        notice = (TextView) view.findViewById(R.id.noTradeHistoryFound);
        deleteAllHistory = view.findViewById(R.id.deleteAllTradeHistory);

        getTradeHistory();

        deleteAllHistory.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                command = "{\"command\": \"303035303081\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"StudentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                updateTradeHistory();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Remove all trade history  ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        return view;
    }

    public void getTradeHistory() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getTradeHistory.php?studentID=" + s.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.toString().equals("[]")) {
                                    infoIcon.setVisibility(view.VISIBLE);
                                    notice.setVisibility(view.VISIBLE);
                                    deleteAllHistory.setEnabled(false);
                                }
                                try {
                                    arrayTradeHistory.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject tradeHistoryResponse = (JSONObject) response.get(i);
                                        arrayTradeHistory.add(new TradeHistoryOB(tradeHistoryResponse.getString("TradeHistoryID"),
                                                new Student(tradeHistoryResponse.getString("StudentID")),
                                                tradeHistoryResponse.getString("Date"),
                                                tradeHistoryResponse.getString("Time"),
                                                new Trade(tradeHistoryResponse.getString("TradeID"),
                                                        new Stuff(tradeHistoryResponse.getString("OfferStuffImage")),
                                                        new Stuff(tradeHistoryResponse.getString("RequestStuffImage")),
                                                        new Student(tradeHistoryResponse.getString("requesterID"),
                                                                tradeHistoryResponse.getString("requesterphoto"),
                                                                tradeHistoryResponse.getString("requesterName"),
                                                                tradeHistoryResponse.getString("requesterProgramme"),
                                                                tradeHistoryResponse.getString("requesterFaculty"),
                                                                tradeHistoryResponse.getInt("requesterYOS")),
                                                        new Student(tradeHistoryResponse.getString("sellerID"),
                                                                tradeHistoryResponse.getString("sellerphoto"),
                                                                tradeHistoryResponse.getString("sellerName"),
                                                                tradeHistoryResponse.getString("sellerProgramme"),
                                                                tradeHistoryResponse.getString("sellerFaculty"),
                                                                tradeHistoryResponse.getInt("sellerYOS")),
                                                        tradeHistoryResponse.getString("TradeStatus"),
                                                        tradeHistoryResponse.getString("TradeDate"),
                                                        tradeHistoryResponse.getString("TradeTime")),
                                                tradeHistoryResponse.getString("status")));
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

    public void populateListView(){
        ArrayAdapter<TradeHistoryOB> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.TradeHistoryList);
        list.setAdapter(adapter);
    }

    public class MyListAdapter extends ArrayAdapter<TradeHistoryOB> {

        public MyListAdapter() {
            super(getActivity(), R.layout.tradehistory_cardview, arrayTradeHistory);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;

            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.tradehistory_cardview, parent, false);
            }

            final TradeHistoryOB currentTradeHistory = arrayTradeHistory.get(position);

            ImageView studentAvatar = (ImageView) itemView.findViewById(R.id.tradeStudentAvatar_history);
            TextView studentName = (TextView) itemView.findViewById(R.id.tradeRequesterName_history);
            TextView studentFaculty = (TextView) itemView.findViewById(R.id.tradeRequesterFaculty_history);
            TextView studentProgramme = (TextView) itemView.findViewById(R.id.tradeRequesterProgramme_history);
            TextView studentYear = (TextView) itemView.findViewById(R.id.tradeRequesterYear_history);

            if (currentTradeHistory.getTradeID().getStudentID().getStudentID().equals(s.getStudentID())){
                Picasso.with(getActivity()).load(currentTradeHistory.getTradeID().getSellerID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(studentAvatar);
                studentName.setText(currentTradeHistory.getTradeID().getSellerID().getStudentName());
                studentFaculty.setText(currentTradeHistory.getTradeID().getSellerID().getStudentFaculty());
                studentProgramme.setText(currentTradeHistory.getTradeID().getSellerID().getStudentProgramme());
                studentYear.setText("Year " + currentTradeHistory.getTradeID().getSellerID().getYearOfStudy());
            }
            else {
                Picasso.with(getActivity()).load(currentTradeHistory.getTradeID().getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(studentAvatar);
                studentName.setText(currentTradeHistory.getTradeID().getStudentID().getStudentName());
                studentFaculty.setText(currentTradeHistory.getTradeID().getStudentID().getStudentFaculty());
                studentProgramme.setText(currentTradeHistory.getTradeID().getStudentID().getStudentProgramme());
                studentYear.setText("Year " + currentTradeHistory.getTradeID().getStudentID().getYearOfStudy());
            }

            ImageView offerStuff = (ImageView) itemView.findViewById(R.id.tradeOfferItem_history);
            Picasso.with(getActivity()).load(currentTradeHistory.getTradeID().getUserStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(offerStuff);

            ImageView requestStuff = (ImageView) itemView.findViewById(R.id.tradeRequestItem_history);
            Picasso.with(getActivity()).load(currentTradeHistory.getTradeID().getRequestStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(requestStuff);

            TextView tradeDate = (TextView) itemView.findViewById(R.id.tradeDate_history);
            tradeDate.setText(currentTradeHistory.getDate());

            TextView tradeTime = (TextView) itemView.findViewById(R.id.tradeTime_history);
            tradeTime.setText(currentTradeHistory.getTime());

            TextView tradeStatus = (TextView) itemView.findViewById(R.id.tradeStatus_history);
            tradeStatus.setText(currentTradeHistory.getTradeID().getTradeStatus());
            String status = currentTradeHistory.getTradeID().getTradeStatus();

            if (status.equals("Rejected") || status.equals("Canceled")){
                tradeStatus.setText(status);
                tradeStatus.setTextColor(getResources().getColor(R.color.red));
            } else if (status.equals("Completed")) {
                tradeStatus.setText(status);
                tradeStatus.setTextColor(getResources().getColor(R.color.forest));
            }

            ImageView deleteHistory = itemView.findViewById(R.id.deleteTradeHistory);

            deleteHistory.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:

                                    command = "{\"command\": \"303035303080\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"TradeHistoryID\": " + "\"" + Conversion.asciiToHex(currentTradeHistory.getTradeHistoryID()) + "\"}";

                                    pahoMqttClient = new PahoMqttClient();
                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                    updateParticularTradeHistory();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Remove trade history ?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            });

            return itemView;
        }
    }

    public void updateParticularTradeHistory() {

        try {

            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                try {
                    jsonObj = new JSONObject(command);
                    if (jsonObj.getString("command").equals("303035303080")) {

                        jsonURL = Constant.serverFile + "hideTradeHistory.php?TradeHistoryID=" + Conversion.hexToAscii(jsonObj.getString("TradeHistoryID")) + "&status=" + "Hide";

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
                                                    TradeHistory tradeHistory = new TradeHistory();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("TradeHistory", s);
                                                    tradeHistory.setArguments(bundle);

                                                    getFragmentManager()
                                                            .beginTransaction()
                                                            .replace(R.id.update_fragmentHolder, tradeHistory)
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
                                        params.put("TradeHistoryID", Conversion.hexToAscii(jsonObj.getString("TradeHistoryID")));
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

    public void updateTradeHistory() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                try {
                    jsonObj = new JSONObject(command);
                    if (jsonObj.getString("command").equals("303035303081")) {

                        jsonURL = Constant.serverFile + "hideAllTradeHistory.php?StudentID=" + Conversion.hexToAscii(jsonObj.getString("StudentID")) + "&status=" + "Hide";

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
                                                    TradeHistory tradeHistory = new TradeHistory();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putSerializable("TradeHistory", s);
                                                    tradeHistory.setArguments(bundle);

                                                    getFragmentManager()
                                                            .beginTransaction()
                                                            .replace(R.id.update_fragmentHolder, tradeHistory)
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
}
