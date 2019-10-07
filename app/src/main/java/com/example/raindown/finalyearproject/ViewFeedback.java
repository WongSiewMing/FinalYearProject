package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.json.*;
import java.util.*;
import Helper.*;

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class ViewFeedback extends Fragment {

    public final static List<Rating> arrayFeedback = new ArrayList<>();
    FragmentManager fragmentManager;
    RatingBar overallRatingBar;
    View view;
    Student s = null;
    String overallRating = "", ratingListCommand = "", overallRatingCommand = "";
    ProgressDialog pDialog = null;
    ImageView infoIcon;
    TextView notice;
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
        getActivity().setTitle("View Feedbacks");
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

        view = inflater.inflate(R.layout.viewfeedback, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("MyProfileFeedback");
        overallRatingCommand = (String) bundle.getSerializable("OverallRating");
        ratingListCommand = (String) bundle.getSerializable("RatingList");

        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);
        overallRatingBar = (RatingBar) view.findViewById(R.id.overallRatingBar);

        getOverallRating();
        return view;
    }


    public void getOverallRating() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(overallRatingCommand);
                if(jsonObj.getString("command").equals("303035303013")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getOverallRating.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject overallRatingResponse = (JSONObject) response.get(i);
                                            overallRating = overallRatingResponse.getString("OverallRating");
                                        }
                                        overallRatingBar.setRating(Float.valueOf(overallRating));
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();
                                        jsonObj = new JSONObject(ratingListCommand);
                                        if(jsonObj.getString("command").equals("303035303014")){
                                            getRatingList(getActivity(), Constant.serverFile + "getRatingList.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")));
                                        }

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


    public void getRatingList(Context context, String url) {

        RequestQueue queue = Volley.newRequestQueue(context);
        if (!pDialog.isShowing())
            pDialog.setMessage("Sync with server...");
        pDialog.show();
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.toString().equals("[]")){
                            infoIcon.setVisibility(view.VISIBLE);
                            notice.setVisibility(view.VISIBLE);
                        }
                        try {
                            arrayFeedback.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject ratingListResponse = (JSONObject) response.get(i);
                                arrayFeedback.add(new Rating(ratingListResponse.getString("ratingID"),
                                        new Student(ratingListResponse.getString("studentID"), ratingListResponse.getString("clientID"),
                                                ratingListResponse.getString("photo"), ratingListResponse.getString("studentName"), ratingListResponse.getString("icNo"),
                                                ratingListResponse.getString("studentProgramme"), ratingListResponse.getString("studentFaculty"),
                                                ratingListResponse.getInt("yearOfStudy")),
                                        new Student(ratingListResponse.getString("studentID"), ratingListResponse.getString("clientID"),
                                                ratingListResponse.getString("photo"), ratingListResponse.getString("studentName"), ratingListResponse.getString("icNo"),
                                                ratingListResponse.getString("studentProgramme"), ratingListResponse.getString("studentFaculty"),
                                                ratingListResponse.getInt("yearOfStudy")),
                                        ratingListResponse.getString("ratingValue"),
                                        ratingListResponse.getString("experience"), ratingListResponse.getString("comments"),
                                        ratingListResponse.getString("ratingDate"), ratingListResponse.getString("ratingTime")));

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
    }


    public void populateListView() {
        ArrayAdapter<Rating> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.feedbackList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Rating> {
        public MyListAdapter() {
            super(getActivity(), R.layout.templateviewfeedback, arrayFeedback);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templateviewfeedback, parent, false);
            }
            Rating currentFeedback = arrayFeedback.get(position);

            TextView studentName = (TextView) itemView.findViewById(R.id.studentName);
            studentName.setText(currentFeedback.getRatingGiver().getStudentName());

            TextView date = (TextView) itemView.findViewById(R.id.date);
            date.setText(currentFeedback.getRatingDate());

            TextView time = (TextView) itemView.findViewById(R.id.time);
            time.setText(currentFeedback.getRatingTime());

            TextView performance = (TextView) itemView.findViewById(R.id.performance);
            performance.setText(currentFeedback.getExperience());

            RatingBar ratingBar = (RatingBar) itemView.findViewById(R.id.ratingBar);
            ratingBar.setRating(Float.valueOf(currentFeedback.getRatingValue()));

            TextView comments = (TextView) itemView.findViewById(R.id.comments);
            comments.setText(currentFeedback.getComments());

            return itemView;

        }
    }

}
