package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;

import java.text.SimpleDateFormat;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class SearchStuff extends Fragment {

    EditText minimumPrice, maximumPrice;
    RadioButton radioNew, radioUsed, radioBoth, radioSortPrice, radioSortName;
    RadioGroup radioGroupCondition, radioGroupSort;
    SearchView searchStuffInput;
    Button filterSearch;
    Switch btnEnableFilter;
    View view;

    String error = "", query = "",sort = "", command = "", category = "", condition = "", insertcommand = "", currentSearchID = "", SearchID = "", jsonURL = "", formattedDate, formattedTime;

    LinearLayout filterLinearLayout;

    Spinner categorySpinner;
    ProgressDialog pDialog = null;
    public static List<Stuff> searchStuffList = new ArrayList<>();
    Student s = null;
    FragmentManager fragmentManager;
    double minPrice = 0.0, maxPrice = 0.0;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private Date date;
    private SimpleDateFormat dateFormat;
    JSONObject jsonObj;
    private static final String TAG = "SearchStuff";

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
        getActivity().setTitle("Search Stuffs");
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

        view = inflater.inflate(R.layout.searchstuff, container, false);
        pDialog = new ProgressDialog(getActivity());

        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("SearchStuff");

        categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapterCategoryName = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.categorySpinner,
                android.R.layout.simple_spinner_item);
        adapterCategoryName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapterCategoryName);

        minimumPrice = (EditText) view.findViewById(R.id.minimumPrice);
        maximumPrice = (EditText) view.findViewById(R.id.maximumPrice);

        radioGroupCondition = (RadioGroup) view.findViewById(R.id.radiogroupCondition);
        radioNew = (RadioButton) view.findViewById(R.id.radioNew);
        radioUsed = (RadioButton) view.findViewById(R.id.radioUsed);
        radioBoth = (RadioButton) view.findViewById(R.id.radioBoth);

        radioGroupSort = (RadioGroup) view.findViewById(R.id.radiogroupSort);
        radioSortPrice = (RadioButton) view.findViewById(R.id.sortPrice);
        radioSortName = (RadioButton) view.findViewById(R.id.sortName);

        searchStuffInput = (SearchView) view.findViewById(R.id.SearchStuffInput);

        filterLinearLayout = (LinearLayout) view.findViewById(R.id.filterLinearLayout);

        btnEnableFilter = (Switch) view.findViewById(R.id.btnEnableFilter);
        btnEnableFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnEnableFilter.isChecked()){
                    filterLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    filterLinearLayout.setVisibility(View.GONE);
                }
            }
        });

        filterSearch = (Button) view.findViewById(R.id.filterSearch);
        filterSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validateInput()) {

                    date = Calendar.getInstance().getTime();
                    dateFormat = new SimpleDateFormat("d MMM yyyy");
                    formattedDate = dateFormat.format(date);
                    dateFormat = new SimpleDateFormat("h:mm a");
                    formattedTime = dateFormat.format(date);

                    if (!searchStuffInput.getQuery().toString().isEmpty()){
                        query = "303031";
                    }

                    if (categorySpinner.getSelectedItem().toString().equals("Books")) {
                        category = "303031";
                    } else if (categorySpinner.getSelectedItem().toString().equals("Electronics")) {
                        category = "303032";
                    } else if (categorySpinner.getSelectedItem().toString().equals("Furnitures")) {
                        category = "303033";
                    } else if (categorySpinner.getSelectedItem().toString().equals("Miscellaneous")) {
                        category = "303034";
                    }

                    if (btnEnableFilter.isChecked()){
                        if (radioGroupCondition.getCheckedRadioButtonId() == R.id.radioNew) {
                            condition = "303031";
                        } else if (radioGroupCondition.getCheckedRadioButtonId() == R.id.radioUsed) {
                            condition = "303032";
                        } else if (radioGroupCondition.getCheckedRadioButtonId() == R.id.radioBoth) {
                            condition = "303033";
                        }

                        if (radioGroupSort.getCheckedRadioButtonId() == R.id.sortPrice) {
                            sort = "303031";
                        } else if (radioGroupSort.getCheckedRadioButtonId() == R.id.sortName) {
                            sort = "303032";
                        }

                        if (!maximumPrice.getText().toString().equals("") && !minimumPrice.getText().toString().equals("")){
                            minPrice = Double.parseDouble(minimumPrice.getText().toString().trim());
                            maxPrice = Double.parseDouble(maximumPrice.getText().toString().trim());
                        } else {
                            minPrice = 0;
                            maxPrice = 0;
                        }
                    } else {
                        condition = "303033";
                        sort = "303031";
                        minPrice = 0;
                        maxPrice = 0;
                    }
                    //Create MQTT command
                    command = "{\"command\": \"30303530301F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"stuffQuery\": " + "\"" + query + "\" ," +
                            "\"stuffCategory\": " + "\"" + category + "\" ," +
                            "\"stuffPriceMin\": " + "\"" + Conversion.asciiToHex(String.format("%.2f", minPrice)) + "\" ," +
                            "\"stuffPriceMax\": " + "\"" + Conversion.asciiToHex(String.format("%.2f", maxPrice)) + "\" ," +
                            "\"stuffCondition\": " + "\"" + condition + "\" ," +
                            "\"stuffSorting\": " + "\"" + sort + "\"}";

                    //Connect and publish messages to broker
                    pahoMqttClient = new PahoMqttClient();
                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                    insertcommand = "{\"command\": \"303035303079\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                            "\"Date\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                            "\"Time\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                            "\"SearchKeyword\": " + "\"" + Conversion.asciiToHex(searchStuffInput.getQuery().toString()) + "\"}";

                    pahoMqttClient = new PahoMqttClient();
                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, insertcommand, "MY/TARUC/SSS/000000001/PUB");

                    getSearchID();

                    applyFilterSearch();
                }
            }
        });

        return view;
    }

    public boolean validateInput() {

        error = "";
        boolean indicator = true;
        if (searchStuffInput.getQuery().toString().isEmpty()){
            error += "- Search Field must be typed.\n";
            indicator = false;
        }

        if (categorySpinner.getSelectedItem().toString().equals("--Category--")){
            error += "- Category must be specified.\n";
            indicator = false;
        }

        if (minimumPrice.getText().toString().trim().equals("") && !maximumPrice.getText().toString().trim().equals("")) {
            error += "- Minimum price is missing.\n";
            indicator = false;
        } else if (maximumPrice.getText().toString().trim().equals("") && !minimumPrice.getText().toString().trim().equals("")){
            error += "- Maximum price is missing.\n";
            indicator = false;
        } else if(!minimumPrice.getText().toString().trim().equals("") && !maximumPrice.getText().toString().trim().equals("")) {
            if ((minimumPrice.getText().toString().trim().matches("^[0-9]\\d*(\\.(\\d{1}|\\d{2}))?$")) &&
                    (maximumPrice.getText().toString().trim().matches("^[0-9]\\d*(\\.(\\d{1}|\\d{2}))?$"))) {
                minPrice = Double.parseDouble(minimumPrice.getText().toString().trim());
                maxPrice = Double.parseDouble(maximumPrice.getText().toString().trim());
                if (minPrice > 0 && maxPrice > 0) {
                    if (minPrice >= maxPrice) {
                        error += "- Minimum price cannot be greater than or equal maximum price.\n";
                        indicator = false;
                    }
                }
            }
        }
        /*
        else {
            if (minimumPrice.getText().toString().trim().matches("^[0-9]\\d*(\\.(\\d{1}|\\d{2}))?$")) {
                minPrice = Double.parseDouble(minimumPrice.getText().toString().trim());
                if (minPrice <= 0) {
                    error += "- Invalid minimum price.\n";
                    indicator = false;
                }

            } else {
                error += "- Invalid minimum price.\n";
                indicator = false;
            }
        }


        if (maximumPrice.getText().toString().trim().equals("")) {
            error += "- Maximum price is missing.\n";
            indicator = false;
        } else {
            if (maximumPrice.getText().toString().trim().matches("^[0-9]\\d*(\\.(\\d{1}|\\d{2}))?$")) {
                maxPrice = Double.parseDouble(maximumPrice.getText().toString().trim());
                if (maxPrice <= 0) {
                    error += "- Invalid maximum price.\n";
                    indicator = false;
                }
            } else {
                error += "- Invalid maximum price.\n";
                indicator = false;
            }
        }

        if (!minimumPrice.getText().toString().trim().equals("") && !maximumPrice.getText().toString().trim().equals("")) {
            if ((minimumPrice.getText().toString().trim().matches("^[0-9]\\d*(\\.(\\d{1}|\\d{2}))?$")) &&
                    (maximumPrice.getText().toString().trim().matches("^[0-9]\\d*(\\.(\\d{1}|\\d{2}))?$"))) {
                minPrice = Double.parseDouble(minimumPrice.getText().toString().trim());
                maxPrice = Double.parseDouble(maximumPrice.getText().toString().trim());
                if (minPrice > 0 && maxPrice > 0) {
                    if (minPrice > maxPrice) {
                        error += "- Minimum price cannot be greater than maximum price.\n";
                        indicator = false;
                    }
                }
            }
        } */


        if (indicator == false) {
            AlertDialog.Builder sent = new AlertDialog.Builder(getActivity());
            sent.setTitle("Invalid input");
            sent.setMessage(error);
            sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            sent.show();
        }
        return indicator;
    }


    public void applyFilterSearch() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                jsonObj = new JSONObject(command);
                if(jsonObj.getString("command").equals("30303530301F")){
                    if (jsonObj.getString("stuffQuery").equals("303031")){
                        query = searchStuffInput.getQuery().toString();
                    }

                    if (jsonObj.getString("stuffCategory").equals("303031")) {
                        category = "Books";
                    } else if (jsonObj.getString("stuffCategory").equals("303032")) {
                        category = "Electronics";
                    } else if (jsonObj.getString("stuffCategory").equals("303033")) {
                        category = "Furnitures";
                    } else if (jsonObj.getString("stuffCategory").equals("303034")) {
                        category = "Miscellaneous";
                    }

                    if(jsonObj.getString("stuffCondition").equals("303031")){
                        condition = "New";
                    }else if(jsonObj.getString("stuffCondition").equals("303032")){
                        condition = "Used";
                    }else if(jsonObj.getString("stuffCondition").equals("303033")){
                        condition = "Both";
                    }

                    if (jsonObj.getString("stuffSorting").equals("303031")) {
                        sort = "stuffPrice asc";
                    } else if (jsonObj.getString("stuffSorting").equals("303032")) {
                        sort = "stuffName asc";
                    }

                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    if (!pDialog.isShowing())
                        pDialog.setMessage("Sync with server...");
                    pDialog.show();

                    String url = "applyFilterSearch.php?stuffQuery=" + query
                            + "&stuffCategory=" + category
                            + "&stuffPriceMin=" + Conversion.hexToAscii(jsonObj.getString("stuffPriceMin"))
                            + "&stuffPriceMax=" + Conversion.hexToAscii(jsonObj.getString("stuffPriceMax"))
                            + "&stuffCondition=" + condition + "&stuffSorting=" + sort;

                    Log.d(TAG, url);

                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + url,
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        searchStuffList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject searchResponse = (JSONObject) response.get(i);
                                            searchStuffList.add(new Stuff(searchResponse.getString("stuffID"), new Student(searchResponse.getString("studentID"),
                                                    searchResponse.getString("clientID"), searchResponse.getString("photo"), searchResponse.getString("studentName"),
                                                    searchResponse.getString("icNo"), searchResponse.getString("studentProgramme"), searchResponse.getString("studentFaculty"),
                                                    searchResponse.getInt("yearOfStudy")), searchResponse.getString("stuffName"), searchResponse.getString("stuffImage"),
                                                    searchResponse.getString("stuffDescription"), searchResponse.getString("stuffCategory"), searchResponse.getString("stuffCondition"),
                                                    searchResponse.getDouble("stuffPrice"), searchResponse.getInt("stuffQuantity"), searchResponse.getString("validStartDate"),
                                                    searchResponse.getString("validEndDate"), searchResponse.getString("stuffStatus")));
                                        }
                                        displayStuff();
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();
                                    } catch (Exception e) {
                                        Toast.makeText(getActivity().getApplication(), e.getMessage(),
                                                Toast.LENGTH_LONG).show();
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


    public void displayStuff() {

        SearchResult frag = new SearchResult();
        Bundle bundle = new Bundle();
        bundle.putSerializable("SearchStuff", s);
        bundle.putSerializable("SearchStuffCategoryTitle", categorySpinner.getSelectedItem().toString());
        frag.setArguments(bundle);
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.update_fragmentHolder, frag)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }


    public void getSearchID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generateSearchHistoryID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject tradeIDResponse = (JSONObject) response.get(i);
                                        currentSearchID = tradeIDResponse.getString("CurrentSearchHistoryID");
                                    }
                                    if (currentSearchID.equals("0")) {
                                        SearchID = "SH0001";
                                    } else {
                                        SearchID = String.format("SH%04d", (Integer.parseInt(currentSearchID.substring(3, 6)) + 1));
                                    }
                                    insertSearchHistory();

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

    public void insertSearchHistory() {
        try {
            jsonObj = new JSONObject(insertcommand);
            if(jsonObj.getString("command").equals("303035303079")){

                jsonURL = Constant.serverFile + "insertSearchHistory.php?SearchHistoryID=" + SearchID
                        + "&StudentID=" + Conversion.hexToAscii(jsonObj.getString("StudentID"))
                        + "&Date=" + Conversion.hexToAscii(jsonObj.getString("Date"))
                        + "&Time=" + Conversion.hexToAscii(jsonObj.getString("Time"))
                        + "&SearchKeyword=" + Conversion.hexToAscii(jsonObj.getString("SearchKeyword"))
                        + "&status=Show";

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
                                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                                        if (success.equals("1")) {

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
                                params.put("SearchHistoryID", SearchID);
                                params.put("StudentID", Conversion.hexToAscii(jsonObj.getString("StudentID")));
                                params.put("Date", Conversion.hexToAscii(jsonObj.getString("Date")));
                                params.put("Time", Conversion.hexToAscii(jsonObj.getString("Time")));
                                params.put("SearchKeyword", Conversion.hexToAscii(jsonObj.getString("SearchKeyword")));
                                params.put("status", "Show");
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
}
