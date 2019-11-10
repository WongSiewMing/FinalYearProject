package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.SearchHistoryOB;
import Helper.Student;

public class SearchHistory extends Fragment {
    private View view;
    private final static List<SearchHistoryOB> arraySearchHistory = new ArrayList<>();
    private ProgressDialog pDialog = null;
    private Student s = null;
    private ImageView infoIcon;
    private TextView notice;
    private JSONObject jsonObj;

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

        getSearchHistory();

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

            SearchHistoryOB currentSearchHistory = arraySearchHistory.get(position);

            TextView searchKeyword = (TextView) itemView.findViewById(R.id.SearchKeyword);
            searchKeyword.setText(currentSearchHistory.getSearchKeyword());

            TextView searchTime = (TextView) itemView.findViewById(R.id.searchTime);
            searchTime.setText("Searched On " + currentSearchHistory.getDate() + " " + currentSearchHistory.getTime());

            return itemView;
        }
    }
}
