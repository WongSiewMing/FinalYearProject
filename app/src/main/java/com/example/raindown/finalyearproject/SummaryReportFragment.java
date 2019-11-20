package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.util.ArrayList;
import java.util.List;

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
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private String command = "", report = "", retrieveUrl = "";
    private ArrayList<SummaryItem> itemList = new ArrayList<>();
    private ArrayList<Stuff> stuffList = new ArrayList<>();
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
        if (report.equals("Overall Sales")){

            txtSummaryTitle.setText(report);

            command = "{\"command\": \"303035303091\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            itemList.clear();
            itemList.add(new SummaryItem("1", R.mipmap.icon_book, "", "Books", "0.00"));
            itemList.add(new SummaryItem("2", R.mipmap.icon_electronics, "", "Electronics", "0.00"));
            itemList.add(new SummaryItem("3", R.mipmap.icon_furnitures, "", "Furnitures", "0.00"));
            itemList.add(new SummaryItem("4", R.mipmap.icon_miscellaneous, "", "Miscellaneous", "0.00"));

            getOverallSales();

        } else if (report.equals("Overall Purchase")){

            txtSummaryTitle.setText(report);

            command = "{\"command\": \"303035303092\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            itemList.clear();
            itemList.add(new SummaryItem("1", R.mipmap.icon_book, "", "Books", "0.00"));
            itemList.add(new SummaryItem("2", R.mipmap.icon_electronics, "", "Electronics", "0.00"));
            itemList.add(new SummaryItem("3", R.mipmap.icon_furnitures, "", "Furnitures", "0.00"));
            itemList.add(new SummaryItem("4", R.mipmap.icon_miscellaneous, "", "Miscellaneous", "0.00"));

        } else if (report.equals("Net Gross")){

        } else if (report.equals("Top Selling Stuff (Personal)")){

        } else if (report.equals("Top Selling Stuff (Overall)")){

        } else if (report.equals("Top Requested Stuff")){

        } else if (report.equals("Most Sold Stuff Category")){

        }
    }

    public void getOverallSales(){
        Log.d(TAG, "Enter Overall Sales");
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
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getOverallSales.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")),
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
                                        total = 0.0;
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
                                                itemList.set(0, new SummaryItem("1", R.mipmap.icon_book, "", "Books", String.format("%.2f", price)));
                                                total += price;

                                            } else if (stuffList.get(i).getStuffCategory().equals("Electronics")){
                                                price = Double.parseDouble(itemList.get(1).getItemAmount()) + stuffList.get(i).getStuffPrice();
                                                itemList.set(1, new SummaryItem("2", R.mipmap.icon_electronics, "", "Electronics", String.format("%.2f", price)));
                                                total += price;

                                            } else if (stuffList.get(i).getStuffCategory().equals("Furnitures")){
                                                price = Double.parseDouble(itemList.get(2).getItemAmount()) + stuffList.get(i).getStuffPrice();
                                                itemList.set(2, new SummaryItem("3", R.mipmap.icon_furnitures, "", "Furnitures",String.format("%.2f", price)));
                                                total += price;

                                            } else if (stuffList.get(i).getStuffCategory().equals("Miscellaneous")){
                                                price = Double.parseDouble(itemList.get(3).getItemAmount()) + stuffList.get(i).getStuffPrice();
                                                itemList.set(3, new SummaryItem("4", R.mipmap.icon_miscellaneous, "", "Miscellaneous", String.format("%.2f", price)));
                                                total += price;
                                            }

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

    public void populateItemAdapterView(){
        mRecyclerView = view.findViewById(R.id.summaryInfoList);
        txtSummaryTotal.setText("Total : RM " + String.format("%.2f", total));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new SummaryAdapter(itemList);

        if (report.equals("Overall Sales")){
            mAdapter.hideNumTextView(true);
            mAdapter.setGreenAmount(true);
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
