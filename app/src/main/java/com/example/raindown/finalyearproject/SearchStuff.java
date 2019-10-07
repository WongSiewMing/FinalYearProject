package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class SearchStuff extends Fragment {

    EditText minimumPrice, maximumPrice;
    RadioButton radioNew, radioUsed, radioBoth, radioSortHighToLow, radioSortLowToHigh;
    RadioGroup radioGroupCondition, radioGroupSort;
    Button filterSearch;
    View view;
    String error = "", sort = "", command = "", category = "", condition = "";
    Spinner categorySpinner;
    ProgressDialog pDialog = null;
    public static List<Stuff> searchStuffList = new ArrayList<>();
    Student s = null;
    FragmentManager fragmentManager;
    double minPrice = 0.0, maxPrice = 0.0;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
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
        radioSortHighToLow = (RadioButton) view.findViewById(R.id.sortHighToLow);
        radioSortLowToHigh = (RadioButton) view.findViewById(R.id.sortLowToHigh);

        filterSearch = (Button) view.findViewById(R.id.filterSearch);
        filterSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {

                    if (categorySpinner.getSelectedItem().toString().equals("Books")) {
                        category = "303031";
                    } else if (categorySpinner.getSelectedItem().toString().equals("Electronics")) {
                        category = "303032";
                    } else if (categorySpinner.getSelectedItem().toString().equals("Furnitures")) {
                        category = "303033";
                    } else if (categorySpinner.getSelectedItem().toString().equals("Miscellaneous")) {
                        category = "303034";
                    }

                    if (radioGroupCondition.getCheckedRadioButtonId() == R.id.radioNew) {
                        condition = "303031";
                    } else if (radioGroupCondition.getCheckedRadioButtonId() == R.id.radioUsed) {
                        condition = "303032";
                    } else if (radioGroupCondition.getCheckedRadioButtonId() == R.id.radioBoth) {
                        condition = "303033";
                    }

                    if (radioGroupSort.getCheckedRadioButtonId() == R.id.sortHighToLow) {
                        sort = "303031";
                    } else if (radioGroupSort.getCheckedRadioButtonId() == R.id.sortLowToHigh) {
                        sort = "303032";
                    }

					//Create MQTT command
                    command = "{\"command\": \"30303530301F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"stuffCategory\": " + "\"" + category + "\" ," +
                            "\"stuffPriceMin\": " + "\"" + Conversion.asciiToHex(minimumPrice.getText().toString().trim()) + "\" ," +
                            "\"stuffPriceMax\": " + "\"" + Conversion.asciiToHex(maximumPrice.getText().toString().trim()) + "\" ," +
                            "\"stuffCondition\": " + "\"" + condition + "\" ," +
                            "\"stuffSorting\": " + "\"" + sort + "\"}";

					//Connect and publish messages to broker
                    pahoMqttClient = new PahoMqttClient();
                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
                    applyFilterSearch();
                }

            }
        });

        return view;
    }

    public boolean validateInput() {

        error = "";
        boolean indicator = true;
        if (categorySpinner.getSelectedItem().equals("--Category--")) {
            error += "- Category type is not selected.\n";
            indicator = false;
        }

        if (minimumPrice.getText().toString().trim().equals("")) {
            error += "- Minimum price is missing.\n";
            indicator = false;
        } else {
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
        }


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
                        sort = "desc";
                    } else if (jsonObj.getString("stuffSorting").equals("303032")) {
                        sort = "asc";
                    }

                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    if (!pDialog.isShowing())
                        pDialog.setMessage("Sync with server...");
                    pDialog.show();

                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "applyFilterSearch.php?stuffCategory=" + category
                            + "&stuffPriceMin=" + Conversion.hexToAscii(jsonObj.getString("stuffPriceMin"))
                            + "&stuffPriceMax=" + Conversion.hexToAscii(jsonObj.getString("stuffPriceMax"))
                            + "&stuffCondition=" + condition + "&stuffSorting=" + sort,
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


}
