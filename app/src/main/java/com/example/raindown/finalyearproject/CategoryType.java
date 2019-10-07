package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.squareup.picasso.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class CategoryType extends Fragment {

    public final static List<Stuff> arrayCategoryType = new ArrayList<>();
    FragmentManager fragmentManager;
    View view;
    Category categoryTitle = null;
    Student s = null;
    ProgressDialog pDialog = null;
    ImageView infoIcon;
    TextView notice;
    JSONObject jsonObj;
    String category = "";

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
        Bundle bundle = getArguments();
        categoryTitle = (Category) bundle.getSerializable("ClickedCategory");
        getActivity().setTitle(categoryTitle.getCategoryName());

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

        view = inflater.inflate(R.layout.categorytype, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("StudentClickedCategory");
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        registerClickCallBack();
        getStuffListAccordingToCategory((String)bundle.getSerializable("JSON"));

        return view;
    }


    public void getStuffListAccordingToCategory(String msg) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(msg);
                if (jsonObj.getString("stuffCategory").equals("303031")) {
                    category = "Books";
                } else if (jsonObj.getString("stuffCategory").equals("303032")) {
                    category = "Electronics";
                } else if (jsonObj.getString("stuffCategory").equals("303033")) {
                    category = "Furnitures";
                } else if (jsonObj.getString("stuffCategory").equals("303034")) {
                    category = "Miscellaneous";
                }

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStuffListAccordingToCategory.php?stuffCategory=" + category,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response.toString().equals("[]")) {
                                    infoIcon.setVisibility(view.VISIBLE);
                                    notice.setVisibility(view.VISIBLE);
                                }
                                try {
                                    arrayCategoryType.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject stuffResponse = (JSONObject) response.get(i);
                                        arrayCategoryType.add(new Stuff(stuffResponse.getString("stuffID"), new Student(stuffResponse.getString("studentID"),
                                                stuffResponse.getString("clientID"), stuffResponse.getString("photo"), stuffResponse.getString("studentName"),
                                                stuffResponse.getString("icNo"), stuffResponse.getString("studentProgramme"), stuffResponse.getString("studentFaculty"),
                                                stuffResponse.getInt("yearOfStudy")), stuffResponse.getString("stuffName"), stuffResponse.getString("stuffImage"),
                                                stuffResponse.getString("stuffDescription"), stuffResponse.getString("stuffCategory"), stuffResponse.getString("stuffCondition"),
                                                stuffResponse.getDouble("stuffPrice"), stuffResponse.getInt("stuffQuantity"), stuffResponse.getString("validStartDate"),
                                                stuffResponse.getString("validEndDate"), stuffResponse.getString("stuffStatus")));

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

        ArrayAdapter<Stuff> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.categoryTypeList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Stuff> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatecategorytype, arrayCategoryType);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatecategorytype, parent, false);
            }
            Stuff currentCategoryType = arrayCategoryType.get(position);
            ImageView stuffPhoto = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(currentCategoryType.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffPhoto);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(currentCategoryType.getStuffName());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f", currentCategoryType.getStuffPrice()));

            return itemView;

        }
    }


    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.categoryTypeList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Stuff clickedStuff = arrayCategoryType.get(position);
                StuffDetails frag = new StuffDetails();
                Bundle bundles = new Bundle();
                bundles.putSerializable("ClickedStuff", clickedStuff);
                bundles.putSerializable("StudentClickedStuff", s); //me
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
