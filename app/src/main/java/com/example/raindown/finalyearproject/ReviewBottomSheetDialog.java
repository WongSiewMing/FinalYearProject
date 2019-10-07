package com.example.raindown.finalyearproject;

/*Author : Lee Thian Xin
   Programme : RSD3
   Year : 2018*/

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.StoreBasicInfoOB;
import Helper.StoreRatingOB;

public class ReviewBottomSheetDialog extends BottomSheetDialogFragment{
    List<StoreRatingOB> storeRatingList;
    private  BottomSheetListerner mListener;
    private String StoreID, formattedDate, formattedTime , userID, lastestReviewID, newReviewID, url, rateValue;
    private TextView tryName, leaveReview, commentText;
    private Dialog writeReview;
    private RatingBar giveRating;
    private EditText reviewComment;
    private Button submitReview, cancelReview;
    private static final String TAG = "ReviewDialog";
    private Date c;
    private SimpleDateFormat df;
    private String command;
    private JSONObject jsonObj;


    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.review_bottom_sheet_layout, container, false);
        Bundle bundle = getArguments();
        StoreID = bundle.getString("storeID");

        storeRatingList = new ArrayList<>();
        populateStoreRatingList();

        //gather data for insert later
        c = Calendar.getInstance().getTime();
        Log.d(TAG,"Current Time = " + c);

        df = new SimpleDateFormat("dd-MM-yyyy");
        formattedDate = df.format(c);
        Log.d(TAG,"Formatted Date = " + formattedDate);
        df = new SimpleDateFormat("HHmm");
        formattedTime = df.format(c);
        Log.d(TAG,"Formatted Time = " + formattedTime);
        userID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        Log.d(TAG,"User ID = " + userID);

        //writeReview
        writeReview = new Dialog(getActivity());
        leaveReview = (TextView) view.findViewById(R.id.leaveReview);
        leaveReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeReview.setContentView(R.layout.writereview);
                writeReview.setCancelable(false);
                giveRating = (RatingBar) writeReview.findViewById(R.id.giveRating);
                reviewComment = (EditText) writeReview.findViewById(R.id.reviewComment);
                submitReview = (Button) writeReview.findViewById(R.id.submitReview);
                cancelReview = (Button) writeReview.findViewById(R.id.cancelReview);
                writeReview.show();

                cancelReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        writeReview.dismiss();
                    }
                });

                submitReview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkSubmitField(reviewComment.getText().toString())) {
                            getStoreReviewID();
                        }
                    }
                });
            }
        });

        //leaveReview.setVisibility(View.GONE);


        return view;
    }

    private void getStoreReviewID() {
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreReviewID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for(int i = 0; i <response.length(); i++){
                                        JSONObject reviewResponse = (JSONObject) response.get(i);
                                        lastestReviewID = reviewResponse.getString("StoreRatID");
                                    }
                                    if(lastestReviewID.equals("0")){
                                        newReviewID = "srat1001";
                                        insertStoreReview(newReviewID);

                                    }else{
                                        String first = lastestReviewID.substring(0, 4);
                                        String last = lastestReviewID.substring(4);
                                        int number = Integer.parseInt(last) + 1;
                                        newReviewID = first + Integer.toString(number);
                                        insertStoreReview(newReviewID);
                                    }


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


    }

    public void insertStoreReview(final String newID){
        Log.d(TAG, "new ID = " + newID);
            rateValue = String.valueOf(giveRating.getRating());

            try {
                command = "{\"command\": \"303035303058\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"storeRatID\": " + "\"" + Conversion.asciiToHex(newID) + "\" ," +
                        "\"storeID\": " + "\"" + Conversion.asciiToHex(StoreID) + "\" ," +
                        "\"rateDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                        "\"rateTime\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                        "\"ratValue\": " + "\"" + Conversion.asciiToHex(rateValue) + "\" ," +
                        "\"comments\": " + "\"" + Conversion.asciiToHex(reviewComment.getText().toString().trim()) + "\" ," +
                        "\"studentID\": " + "\"" + Conversion.asciiToHex(userID) + "\"}";

                String encodedComments = "";
                jsonObj = new JSONObject(command);
                if (jsonObj.getString("command").equals("303035303058")){
                    String[] encodeComments = {Conversion.hexToAscii(jsonObj.getString("comments"))};
                    for (String s : encodeComments){
                        encodedComments += URLEncoder.encode(s, "UTF-8");
                    }
                }



            //http://192.168.0.107/raindown/insertStoreRating.php?storeRatID=srat1009&storeID=1&rateDate=2&rateTime=3&ratValue=4&comments=5&studentID=6
            url = Constant.serverFile + "insertStoreRating.php?storeRatID=" + newID + "&storeID=" + StoreID + "&rateDate=" + formattedDate + "&rateTime=" + formattedTime
                    + "&ratValue=" + rateValue + "&comments=" + encodedComments + "&studentID=" + userID;

                Log.d(TAG, "Insert review URL = " + url);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest reviewRequest = new StringRequest(
                        Request.Method.POST,
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if(isAdded()){

                                }
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("success");
                                    Log.d(TAG,"Success message = " + success);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    if (success.equals("1")) {
                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        break;
                                                }
                                            }
                                        };
                                        builder.setTitle("Feedback sent!").setMessage("Feedback is successfully sent")
                                                .setPositiveButton("Ok", dialogClickListener).show();
                                        writeReview.dismiss();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                if (isAdded()){

                                }
                            }
                        }
                ){
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                                        params.put("storeRatID", newID);
                                        params.put("storeID", StoreID);
                                        params.put("rateDate", formattedDate);
                                        params.put("rateTime", formattedTime);
                                        params.put("ratValue", rateValue);
                                        params.put("comments", reviewComment.getText().toString().trim());
                                        params.put("studentID", userID);
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/x-www-form-urlencoded");
                        return params;
                    }
                };
                queue.add(reviewRequest);
            } catch (Exception e){
                e.printStackTrace();
            }
            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                }
            }


    }

    private boolean checkSubmitField(String s) {
        if (TextUtils.isEmpty(s)){
            reviewComment.setError("Enter Something");
            return false;
        }
        return true;
    }

    private void populateStoreRatingList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreReviews.php?storeID=" + StoreID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                   // tryName.setText("Responded");
                                    storeRatingList.clear();
                                    for (int i = 0; i < response.length(); i++){
                                        JSONObject reviewResponse = (JSONObject) response.get(i);
                                        storeRatingList.add(new StoreRatingOB(reviewResponse.getString("StoreRatID"),
                                                reviewResponse.getString("StoreID"),
                                                reviewResponse.getString("RateDate"),
                                                reviewResponse.getString("RateTime"),
                                                reviewResponse.getString("RatValue"),
                                                reviewResponse.getString("Comments"),
                                                reviewResponse.getString("studentID"),
                                                reviewResponse.getString("studentName"),
                                                reviewResponse.getString("photo")));
                                    }
                                    populateRecycleView();
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

    private void populateRecycleView() {
        RecyclerView reviewListrv = (RecyclerView) view.findViewById(R.id.reviewListrv);
        ReviewListRecyclerViewAdapter reviewListAdapter = new ReviewListRecyclerViewAdapter(getActivity(), storeRatingList);
        reviewListrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        reviewListrv.setAdapter(reviewListAdapter);


    }

    public interface BottomSheetListerner{
        void onButtonClicked(String text);
    }


}
