package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import Helper.AppointmentOB;
import Helper.AvailableTimeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;
import Helper.SummaryItem;
import Helper.SummaryOption;

public class SummaryReportFragment extends Fragment {
    public final static List<SummaryOption> arraySummaryOption = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Student s = null;
    String stuffID = "", appointmentID = "";
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private String command = "", report = "", retrieveUrl = "";
    private ArrayList<SummaryItem> itemList = new ArrayList<>();
    private ArrayList<Stuff> stuffList = new ArrayList<>();
    private ArrayList<AppointmentOB> appointmentList = new ArrayList<>();
    private ProgressDialog pDialog = null;
    private RecyclerView mRecyclerView, summaryInfoList;
    private SummaryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView txtSummaryTitle, txtSummaryTotal, notice;
    private ImageView infoIcon;
    private JSONObject jsonObj;
    private Double total = 0.0;

    private static final String TAG = "SummaryReport";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Summary Report");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.summary_report,container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("Student");
        report = bundle.getString("ReportType");

        pDialog = new ProgressDialog(getActivity());

        infoIcon = view.findViewById(R.id.infoIcon);
        notice = view.findViewById(R.id.notice);

        txtSummaryTitle = view.findViewById(R.id.txtSummaryTitle);
        txtSummaryTotal = view.findViewById(R.id.txtSummaryTotal);
        summaryInfoList = view.findViewById(R.id.summaryInfoList);

        populateItemList();

        return view;
    }

    public void populateItemList(){
        txtSummaryTitle.setText(report);
        if (report.equals("Overall Sales")){

            command = "{\"command\": \"303035303091\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            itemList.clear();
            itemList.add(new SummaryItem("1", getURLForResource(R.mipmap.icon_book), "", "Books", "0.00"));
            itemList.add(new SummaryItem("2", getURLForResource(R.mipmap.icon_electronics), "", "Electronics", "0.00"));
            itemList.add(new SummaryItem("3", getURLForResource(R.mipmap.icon_furnitures), "", "Furnitures", "0.00"));
            itemList.add(new SummaryItem("4", getURLForResource(R.mipmap.icon_miscellaneous), "", "Miscellaneous", "0.00"));

            stuffList.clear();
            total = 0.0;
            getOverallSales();

        } else if (report.equals("Overall Purchase")){

            command = "{\"command\": \"303035303092\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            itemList.clear();
            itemList.add(new SummaryItem("1", getURLForResource(R.mipmap.icon_book), "", "Books", "0.00"));
            itemList.add(new SummaryItem("2", getURLForResource(R.mipmap.icon_electronics), "", "Electronics", "0.00"));
            itemList.add(new SummaryItem("3", getURLForResource(R.mipmap.icon_furnitures), "", "Furnitures", "0.00"));
            itemList.add(new SummaryItem("4", getURLForResource(R.mipmap.icon_miscellaneous), "", "Miscellaneous", "0.00"));

            stuffList.clear();
            total = 0.0;
            appointmentList.clear();
            getOverallPurchase();


        } else if (report.equals("Net Gross")){

            itemList.clear();
            itemList.add(new SummaryItem("1", getURLForResource(R.mipmap.icon_book), "", "Books", "0.00"));
            itemList.add(new SummaryItem("2", getURLForResource(R.mipmap.icon_electronics), "", "Electronics", "0.00"));
            itemList.add(new SummaryItem("3", getURLForResource(R.mipmap.icon_furnitures), "", "Furnitures", "0.00"));
            itemList.add(new SummaryItem("4", getURLForResource(R.mipmap.icon_miscellaneous), "", "Miscellaneous", "0.00"));

            stuffList.clear();
            total = 0.0;
            appointmentList.clear();

            command = "{\"command\": \"303035303091\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            getOverallSales();

            command = "{\"command\": \"303035303092\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            getOverallPurchase();


        } else if (report.equals("Top Selling Stuff (Personal)")){

            command = "{\"command\": \"303035303091\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            getTopSellingSelf();

        } else if (report.equals("Top Selling Stuff (Overall)")){

        } else if (report.equals("Top Requested Stuff")){

        } else if (report.equals("Most Sold Stuff Category")){

        }
    }

    public void getOverallSales(){
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
                if (jsonObj.getString("command").equals("303035303091")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSoldStuff.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if (response.toString().equals("[]") && !report.equals("Net Gross")) {
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                        txtSummaryTotal.setVisibility(View.GONE);
                                        summaryInfoList.setVisibility(View.GONE);
                                    }
                                    try {
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

                                            Double price = 0.0;
                                            if (stuffList.get(i).getStuffCategory().equals("Books")){
                                                price = Double.parseDouble(itemList.get(0).getItemAmount()) + stuffList.get(i).getStuffPrice();
                                                itemList.set(0, new SummaryItem("1", getURLForResource(R.mipmap.icon_book), "", "Books", String.format("%.2f", price)));
                                                total += price;

                                            } else if (stuffList.get(i).getStuffCategory().equals("Electronics")){
                                                price = Double.parseDouble(itemList.get(1).getItemAmount()) + stuffList.get(i).getStuffPrice();
                                                itemList.set(1, new SummaryItem("2", getURLForResource(R.mipmap.icon_electronics), "", "Electronics", String.format("%.2f", price)));
                                                total += price;

                                            } else if (stuffList.get(i).getStuffCategory().equals("Furnitures")){
                                                price = Double.parseDouble(itemList.get(2).getItemAmount()) + stuffList.get(i).getStuffPrice();
                                                itemList.set(2, new SummaryItem("3", getURLForResource(R.mipmap.icon_furnitures), "", "Furnitures",String.format("%.2f", price)));
                                                total += price;

                                            } else if (stuffList.get(i).getStuffCategory().equals("Miscellaneous")){
                                                price = Double.parseDouble(itemList.get(3).getItemAmount()) + stuffList.get(i).getStuffPrice();
                                                itemList.set(3, new SummaryItem("4", getURLForResource(R.mipmap.icon_miscellaneous), "", "Miscellaneous", String.format("%.2f", price)));
                                                total += price;
                                            }

                                        }
                                        populateItemAdapterView();

                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }


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

    public void getOverallPurchase(){
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
                if (jsonObj.getString("command").equals("303035303092")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getBroughtStuff.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if (response.toString().equals("[]") && !report.equals("Net Gross")) {
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                        txtSummaryTotal.setVisibility(View.GONE);
                                        summaryInfoList.setVisibility(View.GONE);
                                    }
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject appointmentListResponse = (JSONObject) response.get(i);
                                            appointmentList.add(new AppointmentOB(appointmentListResponse.getString("appointmentID"),
                                                    new Student(appointmentListResponse.getString("studentID"),
                                                            appointmentListResponse.getString("photo"),
                                                            appointmentListResponse.getString("studentName"),
                                                            appointmentListResponse.getString("studentProgramme")),
                                                    new AvailableTimeOB(appointmentListResponse.getString("availableID"),
                                                            appointmentListResponse.getString("availableStudentID"),
                                                            appointmentListResponse.getString("availableDate"),
                                                            appointmentListResponse.getString("startTime"),
                                                            appointmentListResponse.getString("endTime"),
                                                            appointmentListResponse.getString("availableStatus")),
                                                    new Stuff(appointmentListResponse.getString("stuffID"),
                                                            appointmentListResponse.getString("stuffName"),
                                                            appointmentListResponse.getString("stuffImage"),
                                                            appointmentListResponse.getString("stuffDescription"),
                                                            appointmentListResponse.getString("stuffStudentID")),
                                                    appointmentListResponse.getString("opponentID"),
                                                    appointmentListResponse.getString("appointmentStatus"),
                                                    appointmentListResponse.getString("appointmentDate")));

                                            stuffID = appointmentList.get(i).getStuffID().getStuffID();
                                            appointmentID = appointmentList.get(i).getAppointmentID();
                                            getStuffDetail();
                                        }


                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }


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

    public void getStuffDetail(){
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                command = "{\"command\": \"303035303061\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"studentID\": " + "\"" + s.getStudentID() + "\" ," +
                        "\"appointmentID\": " + "\"" + Conversion.asciiToHex(appointmentID) + "\" ," +
                        "\"stuffID\": " + "\"" + Conversion.asciiToHex(stuffID) + "\" }";

                pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                //http://192.168.0.106/raindown/getStuffDetail.php?stuffID=STF0001
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStuffDetail.php?stuffID=" + stuffID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject stuffResponse = (JSONObject) response.get(i);
                                        stuffList.add(new Stuff(stuffResponse.getString("stuffID"),
                                                new Student(stuffResponse.getString("studentID"),
                                                        stuffResponse.getString("photo"),
                                                        stuffResponse.getString("studentName"),
                                                        stuffResponse.getString("icNo"),
                                                        stuffResponse.getString("studentProgramme"),
                                                        stuffResponse.getString("studentFaculty"),
                                                        stuffResponse.getInt("yearOfStudy")),
                                                stuffResponse.getString("stuffName"),
                                                stuffResponse.getString("stuffImage"),
                                                stuffResponse.getString("stuffDescription"),
                                                stuffResponse.getString("stuffCategory"),
                                                stuffResponse.getString("stuffCondition"),
                                                stuffResponse.getDouble("stuffPrice"),
                                                stuffResponse.getInt("stuffQuantity"),
                                                stuffResponse.getString("validStartDate"),
                                                stuffResponse.getString("validEndDate"),
                                                stuffResponse.getString("stuffStatus")));

                                        Double price = 0.0;
                                        if (stuffList.get(i).getStuffCategory().equals("Books")){
                                            price = Double.parseDouble(itemList.get(0).getItemAmount()) - stuffList.get(i).getStuffPrice();
                                            itemList.set(0, new SummaryItem("1", getURLForResource(R.mipmap.icon_book), "", "Books", String.format("%.2f", price)));
                                            total += price;

                                        } else if (stuffList.get(i).getStuffCategory().equals("Electronics")){
                                            price = Double.parseDouble(itemList.get(1).getItemAmount()) - stuffList.get(i).getStuffPrice();
                                            itemList.set(1, new SummaryItem("2", getURLForResource(R.mipmap.icon_electronics), "", "Electronics", String.format("%.2f", price)));
                                            total += price;

                                        } else if (stuffList.get(i).getStuffCategory().equals("Furnitures")){
                                            price = Double.parseDouble(itemList.get(2).getItemAmount()) - stuffList.get(i).getStuffPrice();
                                            itemList.set(2, new SummaryItem("3", getURLForResource(R.mipmap.icon_furnitures), "", "Furnitures",String.format("%.2f", price)));
                                            total += price;

                                        } else if (stuffList.get(i).getStuffCategory().equals("Miscellaneous")){
                                            price = Double.parseDouble(itemList.get(3).getItemAmount()) - stuffList.get(i).getStuffPrice();
                                            itemList.set(3, new SummaryItem("4", getURLForResource(R.mipmap.icon_miscellaneous), "", "Miscellaneous", String.format("%.2f", price)));
                                            total += price;
                                        }
                                    }
                                    populateItemAdapterView();


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

    public void getTopSellingSelf(){
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
                if (jsonObj.getString("command").equals("303035303091")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSoldStuff.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if (response.toString().equals("[]")) {
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                        txtSummaryTotal.setVisibility(View.GONE);
                                        summaryInfoList.setVisibility(View.GONE);
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
                                            Log.d(TAG, "Stuff ID fetched (stuffList) =" + stuffList.get(i).getStuffID());

                                            itemList.add(new SummaryItem(String.format("%d", i + 1),stuffList.get(i).getStuffImage(), stuffList.get(i).getStuffID(), stuffList.get(i).getStuffName(), ""));

                                            populateItemAdapterView();
                                        }

                                        if (pDialog.isShowing()) {
                                            pDialog.dismiss();
                                        }


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

    public String getURLForResource (int resourceId) {
        //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceId).toString();
    }

    public void populateItemAdapterView(){
        mRecyclerView = view.findViewById(R.id.summaryInfoList);
        txtSummaryTotal.setText("Total : RM " + String.format("%.2f", total));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new SummaryAdapter(itemList);

        if (report.equals("Overall Sales")){
            mAdapter.hideNumTextView(true);
            mAdapter.setGreenAmount(true);
        } else if (report.equals("Overall Purchase")){
            mAdapter.hideNumTextView(true);
            mAdapter.setRedAmount(true);
        } else if (report.equals("Top Selling Stuff (Personal)")){
            mAdapter.hideAmountTextView(true);
            txtSummaryTotal.setVisibility(View.GONE);
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
